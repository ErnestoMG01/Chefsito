import os
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from typing import List, Optional

from fastapi import Depends, FastAPI, HTTPException, Query, Request, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from .database import Base, SessionLocal, engine, get_db
from .models import Recipe, User, UserFavorite, UserPantryItem
from .recipe_ingestor import (
    estimate_cost_for_ingredient,
    seed_initial_catalog,
    sync_external_recipes_into_db,
)
from .schemas import (
    AuthResponse,
    DynamicSyncResult,
    LoginRequest,
    PantryItemRequest,
    PantryItemResponse,
    ProfileUpdateRequest,
    RecipeCreateRequest,
    RecipeResponse,
    RegisterRequest,
    UserDto,
)
from .security import (
    check_rate_limit,
    create_access_token,
    get_current_user,
    get_optional_user,
    hash_password,
    verify_admin_key,
    verify_password,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Crear las tablas en PostgreSQL (AWS RDS) o SQLite al iniciar
    Base.metadata.create_all(bind=engine)
    if os.getenv("AUTO_SEED_ON_STARTUP", "true").lower() == "true":
        db = SessionLocal()
        try:
            seed_initial_catalog(db)
        finally:
            db.close()
    yield


app = FastAPI(
    title="Chefsito Cloud API (AWS Deployment - Rama 243873)",
    description=(
        "Backend multiusuario simultáneo para Chefsito Android con base de datos "
        "PostgreSQL desplegada en AWS RDS, autenticación JWT + Bcrypt y motor "
        "dinámico de ingesta y actualización continua de recetas."
    ),
    version="2.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==============================================================================
# 1. HEALTHCHECK Y ESTADO DEL SERVIDOR AWS
# ==============================================================================
@app.get("/health", tags=["AWS Health & Status"])
def health_check(db: Session = Depends(get_db)):
    recipe_count = db.query(Recipe).count()
    user_count = db.query(User).count()
    return {
        "status": "ok",
        "service": "chefsito-aws-backend",
        "branch": "feat/243873",
        "database_connected": True,
        "total_recipes_in_catalog": recipe_count,
        "registered_users": user_count,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


# ==============================================================================
# 2. AUTENTICACIÓN MULTIUSUARIO (JWT + BCRYPT + RATE LIMITING)
# ==============================================================================
@app.post(
    "/api/v1/auth/register",
    response_model=AuthResponse,
    status_code=status.HTTP_201_CREATED,
    tags=["Autenticación Multiusuario"],
)
def register_user(
    payload: RegisterRequest,
    request: Request,
    db: Session = Depends(get_db),
):
    check_rate_limit(request)
    clean_email = payload.email.strip().lower()
    clean_name = payload.name.strip()

    existing = db.query(User).filter(User.email == clean_email).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Ya existe una cuenta con ese correo",
        )

    user = User(
        name=clean_name,
        email=clean_email,
        password_hash=hash_password(payload.password),
        budget=150.0,
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token(user.id, user.email)
    return AuthResponse(access_token=token, user=UserDto.model_validate(user))


@app.post(
    "/api/v1/auth/login",
    response_model=AuthResponse,
    tags=["Autenticación Multiusuario"],
)
def login_user(
    payload: LoginRequest,
    request: Request,
    db: Session = Depends(get_db),
):
    check_rate_limit(request)
    clean_email = payload.email.strip().lower()

    user = db.query(User).filter(User.email == clean_email).first()
    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo o contraseña incorrectos",
        )

    token = create_access_token(user.id, user.email)
    return AuthResponse(access_token=token, user=UserDto.model_validate(user))


@app.get(
    "/api/v1/auth/me",
    response_model=UserDto,
    tags=["Autenticación Multiusuario"],
)
def get_my_profile(current_user: User = Depends(get_current_user)):
    return UserDto.model_validate(current_user)


@app.put(
    "/api/v1/auth/me",
    response_model=UserDto,
    tags=["Autenticación Multiusuario"],
)
def update_my_profile(
    payload: ProfileUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if payload.name is not None and payload.name.strip():
        current_user.name = payload.name.strip()
    if payload.email is not None:
        new_email = payload.email.strip().lower()
        if new_email != current_user.email:
            exists = db.query(User).filter(User.email == new_email).first()
            if exists:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="Ese correo ya está registrado por otro usuario",
                )
            current_user.email = new_email
    if payload.phone is not None:
        current_user.phone = payload.phone.strip()
    if payload.budget is not None and payload.budget >= 0:
        current_user.budget = payload.budget

    db.commit()
    db.refresh(current_user)
    return UserDto.model_validate(current_user)


# ==============================================================================
# 3. CATÁLOGO DINÁMICO DE RECETAS (SINCRONIZACIÓN E INGESTA CONTINUA)
# ==============================================================================
@app.get(
    "/api/v1/recipes",
    response_model=List[RecipeResponse],
    tags=["Catálogo Dinámico de Recetas"],
)
def list_recipes(
    meal_type: Optional[str] = Query(default=None, description="Desayuno, Comida o Cena"),
    day_of_week: Optional[str] = Query(default=None, description="Lunes..Domingo"),
    source: Optional[str] = Query(default=None),
    updated_since: Optional[datetime] = Query(
        default=None,
        description="Timestamp ISO para sincronización incremental desde Android",
    ),
    current_user: Optional[User] = Depends(get_optional_user),
    db: Session = Depends(get_db),
):
    query = db.query(Recipe)
    if meal_type:
        query = query.filter(Recipe.meal_type.ilike(meal_type.strip()))
    if day_of_week:
        query = query.filter(Recipe.day_of_week.ilike(day_of_week.strip()))
    if source:
        query = query.filter(Recipe.source == source.strip())
    if updated_since:
        query = query.filter(Recipe.updated_at >= updated_since)

    recipes = query.order_by(Recipe.id.asc()).all()

    favorite_ids = set()
    if current_user is not None:
        fav_rows = (
            db.query(UserFavorite.recipe_id)
            .filter(UserFavorite.user_id == current_user.id)
            .all()
        )
        favorite_ids = {row[0] for row in fav_rows}

    result: List[RecipeResponse] = []
    for r in recipes:
        dto = RecipeResponse.model_validate(r)
        dto.is_favorite = r.id in favorite_ids
        result.append(dto)
    return result


@app.post(
    "/api/v1/recipes",
    response_model=RecipeResponse,
    status_code=status.HTTP_201_CREATED,
    tags=["Catálogo Dinámico de Recetas"],
)
def create_or_update_dynamic_recipe(
    payload: RecipeCreateRequest,
    db: Session = Depends(get_db),
    _: None = Depends(verify_admin_key),
):
    """
    Permite agregar o actualizar nuevas recetas en caliente en la base de datos de AWS
    sin necesidad de recompilar la app Android. Calcula automáticamente el costo
    estimado si no se especifica por ingrediente.
    """
    if payload.raw_ingredients_text and payload.raw_ingredients_text.strip():
        lines = []
        total_price = 0.0
        for raw_line in payload.raw_ingredients_text.strip().splitlines():
            if not raw_line.strip():
                continue
            if "|" in raw_line:
                name_part, cost_part = raw_line.split("|", 1)
                try:
                    cost = float(cost_part)
                except ValueError:
                    cost = estimate_cost_for_ingredient(name_part)
                lines.append(f"{name_part.strip()}|{cost}")
                total_price += cost
            else:
                cost = estimate_cost_for_ingredient(raw_line)
                lines.append(f"{raw_line.strip()}|{cost}")
                total_price += cost
        ingredients_str = "\n".join(lines)
    else:
        lines = []
        total_price = 0.0
        for ing in payload.ingredients:
            cost = (
                ing.estimated_cost
                if ing.estimated_cost is not None
                else estimate_cost_for_ingredient(ing.name_with_quantity)
            )
            lines.append(f"{ing.name_with_quantity.strip()}|{cost}")
            total_price += cost
        ingredients_str = "\n".join(lines)

    if payload.raw_steps_text and payload.raw_steps_text.strip():
        steps_str = payload.raw_steps_text.strip()
    else:
        steps_str = "\n".join(
            f"{idx}. {step.strip()}" for idx, step in enumerate(payload.steps, start=1)
        )

    existing = None
    if payload.external_id:
        existing = (
            db.query(Recipe)
            .filter(Recipe.external_id == payload.external_id)
            .first()
        )
    if existing is None:
        existing = (
            db.query(Recipe)
            .filter(Recipe.title.ilike(payload.title.strip()))
            .first()
        )

    if existing:
        existing.title = payload.title.strip()
        existing.description = payload.description.strip()
        existing.prep_time_minutes = payload.prep_time_minutes
        existing.difficulty = payload.difficulty
        existing.meal_type = payload.meal_type
        existing.day_of_week = payload.day_of_week
        existing.ingredients = ingredients_str
        existing.steps = steps_str
        existing.estimated_price = round(total_price, 2)
        existing.image_url = payload.image_url
        existing.source = payload.source
        existing.updated_at = datetime.now(timezone.utc)
        db.commit()
        db.refresh(existing)
        return RecipeResponse.model_validate(existing)

    new_recipe = Recipe(
        external_id=payload.external_id,
        title=payload.title.strip(),
        description=payload.description.strip(),
        prep_time_minutes=payload.prep_time_minutes,
        difficulty=payload.difficulty,
        meal_type=payload.meal_type,
        day_of_week=payload.day_of_week,
        ingredients=ingredients_str,
        steps=steps_str,
        estimated_price=round(total_price, 2),
        image_url=payload.image_url,
        source=payload.source,
    )
    db.add(new_recipe)
    db.commit()
    db.refresh(new_recipe)
    return RecipeResponse.model_validate(new_recipe)


@app.post(
    "/api/v1/recipes/bulk",
    response_model=List[RecipeResponse],
    status_code=status.HTTP_201_CREATED,
    tags=["Catálogo Dinámico de Recetas"],
)
def bulk_upload_recipes(
    payload: List[RecipeCreateRequest],
    db: Session = Depends(get_db),
    _: None = Depends(verify_admin_key),
):
    """Carga un lote JSON de múltiples recetas nuevas en una sola petición."""
    created_or_updated = []
    for item in payload:
        created_or_updated.append(create_or_update_dynamic_recipe(item, db, None))
    return created_or_updated


@app.post(
    "/api/v1/recipes/sync-external",
    response_model=DynamicSyncResult,
    tags=["Catálogo Dinámico de Recetas"],
)
async def trigger_external_recipe_ingestion(
    max_to_import: int = Query(default=8, ge=1, le=25),
    db: Session = Depends(get_db),
):
    """
    Conecta el backend de AWS con fuentes externas de recetas (TheMealDB Mexican + DummyJSON),
    traduce ingredientes, calcula sus costos estimados en MXN y los persiste en PostgreSQL
    para que todos los usuarios de la app Android reciban nuevas recetas actualizadas.
    """
    result = await sync_external_recipes_into_db(db, max_to_import=max_to_import)
    return DynamicSyncResult(**result)


# ==============================================================================
# 4. DESPENSA AISLADA POR USUARIO (CONCURRENCIA MULTIUSUARIO)
# ==============================================================================
@app.get(
    "/api/v1/pantry",
    response_model=List[PantryItemResponse],
    tags=["Despensa por Usuario"],
)
def get_user_pantry(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    items = (
        db.query(UserPantryItem)
        .filter(UserPantryItem.user_id == current_user.id)
        .order_by(UserPantryItem.name.asc())
        .all()
    )
    return [PantryItemResponse.model_validate(i) for i in items]


@app.post(
    "/api/v1/pantry",
    response_model=PantryItemResponse,
    status_code=status.HTTP_201_CREATED,
    tags=["Despensa por Usuario"],
)
def upsert_pantry_item(
    payload: PantryItemRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    clean_name = payload.name.strip()
    existing = (
        db.query(UserPantryItem)
        .filter(
            UserPantryItem.user_id == current_user.id,
            UserPantryItem.name.ilike(clean_name),
        )
        .first()
    )
    if existing:
        existing.name = clean_name
        existing.category = payload.category.strip()
        existing.user_cost = payload.user_cost
        existing.is_in_pantry = payload.is_in_pantry
        existing.updated_at = datetime.now(timezone.utc)
        db.commit()
        db.refresh(existing)
        return PantryItemResponse.model_validate(existing)

    item = UserPantryItem(
        user_id=current_user.id,
        name=clean_name,
        category=payload.category.strip(),
        user_cost=payload.user_cost,
        is_in_pantry=payload.is_in_pantry,
    )
    db.add(item)
    db.commit()
    db.refresh(item)
    return PantryItemResponse.model_validate(item)


@app.delete(
    "/api/v1/pantry/{item_name}",
    status_code=status.HTTP_204_NO_CONTENT,
    tags=["Despensa por Usuario"],
)
def delete_pantry_item_by_name(
    item_name: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    existing = (
        db.query(UserPantryItem)
        .filter(
            UserPantryItem.user_id == current_user.id,
            UserPantryItem.name.ilike(item_name.strip()),
        )
        .first()
    )
    if existing:
        db.delete(existing)
        db.commit()
    return None


# ==============================================================================
# 5. FAVORITOS AISLADOS POR USUARIO
# ==============================================================================
@app.get(
    "/api/v1/favorites",
    response_model=List[int],
    tags=["Favoritos por Usuario"],
)
def get_user_favorite_ids(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    rows = (
        db.query(UserFavorite.recipe_id)
        .filter(UserFavorite.user_id == current_user.id)
        .all()
    )
    return [r[0] for r in rows]


@app.post(
    "/api/v1/favorites/{recipe_id}/toggle",
    tags=["Favoritos por Usuario"],
)
def toggle_user_favorite(
    recipe_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    recipe = db.query(Recipe).filter(Recipe.id == recipe_id).first()
    if not recipe:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="La receta no existe en el catálogo",
        )

    existing = (
        db.query(UserFavorite)
        .filter(
            UserFavorite.user_id == current_user.id,
            UserFavorite.recipe_id == recipe_id,
        )
        .first()
    )
    if existing:
        db.delete(existing)
        db.commit()
        return {"recipe_id": recipe_id, "is_favorite": False}

    fav = UserFavorite(user_id=current_user.id, recipe_id=recipe_id)
    db.add(fav)
    db.commit()
    return {"recipe_id": recipe_id, "is_favorite": True}
