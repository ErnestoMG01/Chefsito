import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Si no hay DATABASE_URL configurada, usa SQLite local para pruebas rápidas;
# en AWS EC2 + RDS PostgreSQL se inyecta postgresql://...
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://chefsito_admin:ChefsitoSecure2026!@db:5432/chefsito_db",
)

# Ajuste automático por si el proveedor entrega postgres:// en vez de postgresql://
if DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

POOL_SIZE = int(os.getenv("DB_POOL_SIZE", "15"))
MAX_OVERFLOW = int(os.getenv("DB_MAX_OVERFLOW", "25"))

if DATABASE_URL.startswith("sqlite"):
    engine = create_engine(
        DATABASE_URL,
        connect_args={"check_same_thread": False},
    )
else:
    # Configuración de Pool de Conexiones para concurrencia multiusuario en AWS RDS
    engine = create_engine(
        DATABASE_URL,
        pool_size=POOL_SIZE,
        max_overflow=MAX_OVERFLOW,
        pool_pre_ping=True,
        pool_recycle=1800,
    )

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
