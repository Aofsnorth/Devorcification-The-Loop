from __future__ import annotations

import logging
import time
from typing import Optional

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import ValidationError

from config import settings
from director import AIDirector, ProceduralFallback
from models import ActionPlan, DirectorRequest

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(name)s] %(levelname)s: %(message)s",
)
logger = logging.getLogger("devorcification-director")

app = FastAPI(
    title="Devorcification: The Loop — AI Director",
    description="Behavioral director backend for the Devorcification mod.",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

director = AIDirector()


@app.get("/")
def root() -> dict:
    return {
        "service": "devorcification-director",
        "version": app.version,
        "endpoints": ["/director/plan", "/director/health"],
    }


@app.get("/director/health")
def health() -> dict:
    return {
        "status": "ok",
        "model": settings.default_model,
        "provider_ready": bool(
            (settings.default_model == "openai" and settings.openai_api_key)
            or (settings.default_model == "anthropic" and settings.anthropic_api_key)
            or settings.default_model == "ollama"
        ),
        "latency_ms": 0.0,
    }


@app.post("/director/plan", response_model=ActionPlan)
def plan(request: DirectorRequest) -> ActionPlan:
    started = time.perf_counter()
    logger.info(
        "POST /director/plan session=%s cycle=%d players=%d",
        request.session_id,
        request.cycle,
        len(request.players),
    )

    if not request.players:
        raise HTTPException(status_code=400, detail="players must not be empty")

    try:
        result = director.generate_plan(request)
    except ValidationError as exc:
        logger.warning("Plan validation failed: %s", exc)
        result = ProceduralFallback.generate(request)
    except Exception as exc:
        logger.exception("Unhandled error in /director/plan: %s", exc)
        result = ProceduralFallback.generate(request)

    elapsed_ms = (time.perf_counter() - started) * 1000.0
    logger.info(
        "Plan ready session=%s cycle=%d strategy=%s spend=%d reserve=%d latency=%.1fms",
        request.session_id,
        result.cycle,
        result.global_strategy,
        result.menace_budget.get("spend", 0),
        result.menace_budget.get("reserve", 0),
        elapsed_ms,
    )
    return result


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.exception("Unhandled exception on %s: %s", request.url.path, exc)
    fallback = ActionPlan(
        cycle=0,
        global_strategy="DESCENT",
        menace_budget={"spend": 0, "reserve": 3},
        per_player={},
    )
    return JSONResponse(status_code=500, content=fallback.model_dump())


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
