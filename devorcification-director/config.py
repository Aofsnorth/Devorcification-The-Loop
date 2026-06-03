from __future__ import annotations

import os
from pathlib import Path

from dotenv import load_dotenv
from pydantic_settings import BaseSettings, SettingsConfigDict

BACKEND_ROOT = Path(__file__).resolve().parent
load_dotenv(BACKEND_ROOT / ".env", override=False)


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(BACKEND_ROOT / ".env"),
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    openai_api_key: str = ""
    anthropic_api_key: str = ""
    ollama_url: str = "http://localhost:11434"
    default_model: str = "openai"
    default_openai_model: str = "gpt-4o"
    default_anthropic_model: str = "claude-3-5-sonnet-20241022"
    default_ollama_model: str = "llama3"
    timeout_seconds: float = 4.0
    max_menace_budget_per_cycle: int = 3
    cors_origins: list[str] = ["http://localhost:3000", "http://localhost:8000"]


settings = Settings()
