from __future__ import annotations

import json
import logging
import re
from pathlib import Path
from typing import Optional

import httpx

from config import settings
from models import (
    ActionPlan,
    BlockOverride,
    DirectorRequest,
    DirectedAudio,
    FakeChat,
    FakeEntity,
    PlayerAction,
)

logger = logging.getLogger("director")

PROMPT_DIR = Path(__file__).resolve().parent / "prompts"
SYSTEM_PROMPT_PATH = PROMPT_DIR / "system.txt"


def _load_system_prompt() -> str:
    try:
        return SYSTEM_PROMPT_PATH.read_text(encoding="utf-8")
    except FileNotFoundError:
        return "You are THE DIVORCE. Return only valid JSON ActionPlan."


SYSTEM_PROMPT = _load_system_prompt()


def _strip_to_json(raw: str) -> str:
    raw = raw.strip()
    fence = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", raw, re.DOTALL)
    if fence:
        return fence.group(1)
    start = raw.find("{")
    end = raw.rfind("}")
    if start != -1 and end != -1 and end > start:
        return raw[start : end + 1]
    return raw


def _build_user_prompt(request: DirectorRequest) -> str:
    return (
        "INPUT — Respond with VALID JSON only. No prose, no markdown.\n\n"
        f"Request:\n{json.dumps(request.model_dump(), indent=2)}\n\n"
        "Output JSON (strict schema above):"
    )


class AIDirector:
    def __init__(self) -> None:
        self.provider = settings.default_model.lower()
        self.timeout = settings.timeout_seconds
        self.max_menace = settings.max_menace_budget_per_cycle
        self.openai_client = None
        self.anthropic_client = None

        if self.provider == "openai" and settings.openai_api_key:
            try:
                from openai import OpenAI
                self.openai_client = OpenAI(api_key=settings.openai_api_key)
            except Exception as exc:
                logger.warning("OpenAI init failed: %s", exc)
        if self.provider == "anthropic" and settings.anthropic_api_key:
            try:
                from anthropic import Anthropic
                self.anthropic_client = Anthropic(api_key=settings.anthropic_api_key)
            except Exception as exc:
                logger.warning("Anthropic init failed: %s", exc)

    def build_prompt(self, request: DirectorRequest) -> str:
        return _build_user_prompt(request)

    def generate_plan(self, request: DirectorRequest) -> ActionPlan:
        user_prompt = self.build_prompt(request)
        raw: Optional[str] = None
        provider_used = "fallback"

        try:
            if self.provider == "openai" and self.openai_client is not None:
                raw = self._call_openai(user_prompt)
                provider_used = "openai"
            elif self.provider == "anthropic" and self.anthropic_client is not None:
                raw = self._call_anthropic(user_prompt)
                provider_used = "anthropic"
            elif self.provider == "ollama":
                raw = self._call_ollama(user_prompt)
                provider_used = "ollama"
            else:
                logger.info("Provider '%s' unavailable, using procedural fallback", self.provider)
                return self._validate_budget(ProceduralFallback.generate(request), provider_used)
        except Exception as exc:
            logger.warning("LLM call failed (%s): %s", self.provider, exc)
            return self._validate_budget(ProceduralFallback.generate(request), provider_used)

        if not raw:
            return self._validate_budget(ProceduralFallback.generate(request), provider_used)

        try:
            cleaned = _strip_to_json(raw)
            data = json.loads(cleaned)
            plan = ActionPlan.model_validate(data)
        except Exception as exc:
            logger.warning("LLM JSON parse/validate failed: %s; raw=%r", exc, raw[:200])
            return self._validate_budget(ProceduralFallback.generate(request), provider_used)

        return self._validate_budget(plan, provider_used)

    def _validate_budget(self, plan: ActionPlan, provider: str) -> ActionPlan:
        spend = plan.menace_budget.get("spend", 0)
        reserve = plan.menace_budget.get("reserve", 0)
        if spend < 0:
            spend = 0
            plan.menace_budget["spend"] = 0
        if spend > self.max_menace:
            logger.info("Clamping menace spend %d -> %d", spend, self.max_menace)
            plan.menace_budget["spend"] = self.max_menace
            spend = self.max_menace
        if reserve < 0:
            plan.menace_budget["reserve"] = 0

        total_blocks = 0
        for pid, action in plan.per_player.items():
            if action.asymmetric_blocks:
                remaining = max(0, self.max_menace - total_blocks)
                if len(action.asymmetric_blocks) > remaining:
                    action.asymmetric_blocks = action.asymmetric_blocks[:remaining]
                total_blocks += len(action.asymmetric_blocks or [])
        plan.global_strategy = plan.global_strategy or "DESCENT"
        plan.cycle = plan.cycle or 0
        return plan

    def _call_openai(self, prompt: str) -> str:
        client = self.openai_client
        assert client is not None
        resp = client.chat.completions.create(
            model=settings.default_openai_model,
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": prompt},
            ],
            timeout=self.timeout,
            response_format={"type": "json_object"},
        )
        return resp.choices[0].message.content or ""

    def _call_anthropic(self, prompt: str) -> str:
        client = self.anthropic_client
        assert client is not None
        resp = client.messages.create(
            model=settings.default_anthropic_model,
            max_tokens=1024,
            system=SYSTEM_PROMPT,
            messages=[{"role": "user", "content": prompt}],
            timeout=self.timeout,
        )
        parts = []
        for block in resp.content:
            if hasattr(block, "text"):
                parts.append(block.text)
        return "\n".join(parts)

    def _call_ollama(self, prompt: str) -> str:
        url = settings.ollama_url.rstrip("/") + "/api/chat"
        payload = {
            "model": settings.default_ollama_model,
            "stream": False,
            "format": "json",
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": prompt},
            ],
        }
        with httpx.Client(timeout=self.timeout) as client:
            r = client.post(url, json=payload)
            r.raise_for_status()
            data = r.json()
        return data.get("message", {}).get("content", "")


class ProceduralFallback:
    @staticmethod
    def generate(request: DirectorRequest) -> ActionPlan:
        cycle = request.cycle
        per_player: dict[str, PlayerAction] = {}
        spend = 1
        reserve = 2

        for p in request.players:
            action = PlayerAction()
            if cycle <= 2:
                action.directed_audio = DirectedAudio(
                    sound="minecraft:block.note_block.bell",
                    target=p.player_id,
                    volume=0.3,
                    pitch=0.8,
                )
                action.shader_intensity = 0.15
            elif cycle <= 4:
                action.asymmetric_blocks = [
                    BlockOverride(
                        pos="0 61 30",
                        fake_state="minecraft:barrier",
                        target=p.player_id,
                    )
                ]
                action.shader_intensity = 0.35
            else:
                if not any(a.fake_entity is not None for a in per_player.values()):
                    action.fake_entity = FakeEntity(
                        entity_type="devorcification:watcher",
                        mimic_target=p.player_id,
                        visible_to=[p.player_id],
                        position="0 62 30",
                        behavior="STARE",
                    )
                if not any(a.fake_chat is not None for a in per_player.values()):
                    action.fake_chat = FakeChat(
                        from_player=p.player_id,
                        message="Did you hear that?",
                        target=p.player_id,
                    )
                action.desync_delta = 1
                action.shader_intensity = 0.5
            per_player[p.player_id] = action

        return ActionPlan(
            cycle=cycle,
            global_strategy="DESCENT" if cycle <= 2 else ("HAUNTING" if cycle <= 4 else "BREAKDOWN"),
            menace_budget={"spend": spend, "reserve": reserve},
            per_player=per_player,
        )
