from datetime import datetime, timezone
from sqlalchemy import (
    Boolean,
    Column,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import relationship
from .database import Base


def utc_now():
    return datetime.now(timezone.utc)


class User(Base):
    """
    Usuario registrado en la base de datos central (AWS RDS).
    Soporta múltiples sesiones y dispositivos simultáneos.
    """
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(120), nullable=False)
    email = Column(String(180), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    budget = Column(Float, nullable=False, default=150.0)
    phone = Column(String(40), nullable=False, default="")
    created_at = Column(DateTime(timezone=True), default=utc_now, nullable=False)

    pantry_items = relationship(
        "UserPantryItem",
        back_populates="user",
        cascade="all, delete-orphan",
    )
    favorites = relationship(
        "UserFavorite",
        back_populates="user",
        cascade="all, delete-orphan",
    )


class Recipe(Base):
    """
    Catálogo centralizado y dinámico de recetas en AWS RDS.
    Conserva el formato `"Cantidad + Nombre|CostoEstimado"` en `ingredients`
    para compatibilidad directa con el motor de costos de la app Android,
    además de metadatos de origen (`source`, `external_id`, `image_url`, `updated_at`)
    para cargar nuevas recetas continuamente sin quedarse solo con las precargadas.
    """
    __tablename__ = "recipes"

    id = Column(Integer, primary_key=True, index=True)
    external_id = Column(String(100), unique=True, nullable=True, index=True)
    title = Column(String(200), nullable=False, index=True)
    estimated_price = Column(Float, nullable=False, default=0.0)
    prep_time_minutes = Column(Integer, nullable=False, default=25)
    difficulty = Column(String(40), nullable=False, default="Fácil")
    description = Column(Text, nullable=False, default="")
    meal_type = Column(String(40), nullable=False, default="Comida", index=True)
    day_of_week = Column(String(40), nullable=False, default="")
    ingredients = Column(Text, nullable=False, default="")
    steps = Column(Text, nullable=False, default="")
    image_url = Column(String(500), nullable=False, default="")
    source = Column(String(60), nullable=False, default="SEED_MEXICANO")
    updated_at = Column(
        DateTime(timezone=True),
        default=utc_now,
        onupdate=utc_now,
        nullable=False,
        index=True,
    )


class UserPantryItem(Base):
    """
    Despensa aislada por usuario (`user_id`).
    Permite que múltiples usuarios simultáneos capturen sus propios ingredientes
    y los costos reales que pagaron (`user_cost`) sin interferir entre sí.
    """
    __tablename__ = "user_pantry"
    __table_args__ = (
        UniqueConstraint("user_id", "name", name="uq_user_pantry_user_ingredient"),
    )

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    name = Column(String(140), nullable=False)
    category = Column(String(80), nullable=False, default="Abarrotes / General")
    user_cost = Column(Float, nullable=False, default=0.0)
    is_in_pantry = Column(Boolean, nullable=False, default=True)
    updated_at = Column(
        DateTime(timezone=True),
        default=utc_now,
        onupdate=utc_now,
        nullable=False,
    )

    user = relationship("User", back_populates="pantry_items")


class UserFavorite(Base):
    """
    Relación N:M de recetas favoritas por usuario.
    Reemplaza el booleano global para que cada usuario tenga sus propios favoritos.
    """
    __tablename__ = "user_favorites"
    __table_args__ = (
        UniqueConstraint("user_id", "recipe_id", name="uq_user_favorite_pair"),
    )

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    recipe_id = Column(
        Integer,
        ForeignKey("recipes.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    created_at = Column(DateTime(timezone=True), default=utc_now, nullable=False)

    user = relationship("User", back_populates="favorites")
