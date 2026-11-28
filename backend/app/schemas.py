from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, EmailStr, Field


# ========================== AUTENTICACIÓN Y USUARIO ==========================
class RegisterRequest(BaseModel):
    name: str = Field(..., min_length=1, max_length=120)
    email: EmailStr
    password: str = Field(..., min_length=6, max_length=128)


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1)


class UserDto(BaseModel):
    id: int
    name: str
    email: str
    budget: float = 150.0
    phone: str = ""
    created_at: datetime

    model_config = {"from_attributes": True}


class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserDto


class ProfileUpdateRequest(BaseModel):
    name: Optional[str] = None
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    budget: Optional[float] = None


# ========================== RECETAS DINÁMICAS ==========================
class IngredientLineInput(BaseModel):
    name_with_quantity: str = Field(
        ...,
        description="Ej. '3 piezas de tortilla de maíz' o '150 g de pollo'",
    )
    estimated_cost: Optional[float] = Field(
        default=None,
        description="Costo estimado en MXN. Si se omite, el servidor lo calcula automáticamente.",
    )


class RecipeCreateRequest(BaseModel):
    title: str = Field(..., min_length=2, max_length=200)
    description: str = ""
    prep_time_minutes: int = Field(default=25, ge=1, le=600)
    difficulty: str = "Fácil"
    meal_type: str = Field(default="Comida", description="Desayuno, Comida o Cena")
    day_of_week: str = Field(default="Lunes", description="Lunes a Domingo")
    ingredients: List[IngredientLineInput] = Field(default_factory=list)
    raw_ingredients_text: Optional[str] = Field(
        default=None,
        description="Alternativa directa en formato 'Ingrediente|Costo' separado por saltos de línea",
    )
    steps: List[str] = Field(default_factory=list)
    raw_steps_text: Optional[str] = None
    image_url: str = ""
    source: str = "ADMIN_CLOUD"
    external_id: Optional[str] = None


class RecipeResponse(BaseModel):
    id: int
    external_id: Optional[str] = None
    title: str
    estimated_price: float
    prep_time_minutes: int
    difficulty: str
    description: str
    meal_type: str
    day_of_week: str
    ingredients: str
    steps: str
    image_url: str = ""
    source: str = "SEED_MEXICANO"
    is_favorite: bool = False
    updated_at: datetime

    model_config = {"from_attributes": True}


class DynamicSyncResult(BaseModel):
    imported_count: int
    updated_count: int
    total_catalog_count: int
    sources_queried: List[str]
    details: List[str]


# ========================== DESPENSA POR USUARIO ==========================
class PantryItemRequest(BaseModel):
    name: str = Field(..., min_length=1, max_length=140)
    category: str = "Abarrotes / General"
    user_cost: float = Field(..., ge=0.0)
    is_in_pantry: bool = True


class PantryItemResponse(BaseModel):
    id: int
    user_id: int
    name: str
    category: str
    user_cost: float
    is_in_pantry: bool
    updated_at: datetime

    model_config = {"from_attributes": True}
