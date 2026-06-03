from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, Field


class PlayerSnapshot(BaseModel):
    player_id: str
    player_name: str
    position: list[float]
    velocity: list[float]
    yaw: float
    pitch: float
    health: float
    hunger: int
    light_level: int
    looking_at: str
    is_sprinting: bool
    is_sneaking: bool
    is_inventory_open: bool
    rapid_look_count: int
    backpedal_count: int
    freeze_duration_ms: int
    defensive_block_placements: int
    chat_message_count: int
    cycle_number: int
    time_in_loop_ms: int
    fear_profile: Optional[str] = "UNKNOWN"
    desync_level: int = 0


class SocialGraph(BaseModel):
    proximity_matrix: dict[str, float] = Field(default_factory=dict)
    voice_activity: dict[str, int] = Field(default_factory=dict)
    trust_index: dict[str, float] = Field(default_factory=dict)


class GlobalState(BaseModel):
    session_id: str
    cycle: int
    menace_budget_remaining: int
    current_act: str
    time_in_loop_ms: int


class DirectorRequest(BaseModel):
    session_id: str
    cycle: int
    players: list[PlayerSnapshot]
    social_graph: Optional[SocialGraph] = None
    global_state: GlobalState


class BlockOverride(BaseModel):
    pos: str
    fake_state: str
    target: str


class DirectedAudio(BaseModel):
    sound: str
    target: str
    volume: float
    pitch: float


class FakeChat(BaseModel):
    from_player: str
    message: str
    target: str


class FakeEntity(BaseModel):
    entity_type: str
    mimic_target: Optional[str] = None
    visible_to: list[str]
    position: str
    behavior: str


class PlayerAction(BaseModel):
    desync_delta: int = 0
    asymmetric_blocks: Optional[list[BlockOverride]] = None
    directed_audio: Optional[DirectedAudio] = None
    fake_chat: Optional[FakeChat] = None
    fake_entity: Optional[FakeEntity] = None
    shader_intensity: float = 0.0


class ActionPlan(BaseModel):
    cycle: int
    global_strategy: str
    menace_budget: dict[str, int]
    per_player: dict[str, PlayerAction]
