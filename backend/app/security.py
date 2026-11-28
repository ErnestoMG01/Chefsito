import os
import time
from collections import defaultdict
from datetime import datetime, timedelta, timezone
from typing import Optional

import bcrypt
import jwt
from fastapi import Depends, Header, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from .database import get_db
from .models import User

JWT_SECRET_KEY = os.getenv(
    "JWT_SECRET_KEY",
    "chefsito-aws-lab-super-secret-jwt-key-243873-change-in-prod",
)
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
JWT_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "10080"))  # 7 días
ADMIN_API_KEY = os.getenv("ADMIN_API_KEY", "chefsito-admin-243873")

bearer_scheme = HTTPBearer(auto_error=False)

# Control de Rate Limiting en memoria por IP para proteger endpoints de Login/Registro en AWS
_rate_limit_buckets: dict[str, list[float]] = defaultdict(list)
RATE_LIMIT_WINDOW_SEC = 60
RATE_LIMIT_MAX_ATTEMPTS = 25


def check_rate_limit(request: Request):
    client_ip = request.client.host if request.client else "unknown"
    now = time.time()
    timestamps = _rate_limit_buckets[client_ip]
    _rate_limit_buckets[client_ip] = [
        ts for ts in timestamps if now - ts < RATE_LIMIT_WINDOW_SEC
    ]
    if len(_rate_limit_buckets[client_ip]) >= RATE_LIMIT_MAX_ATTEMPTS:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Demasiados intentos. Por seguridad, espera un minuto antes de reintentar.",
        )
    _rate_limit_buckets[client_ip].append(now)


def hash_password(plain_password: str) -> str:
    salt = bcrypt.gensalt(rounds=12)
    return bcrypt.hashpw(plain_password.encode("utf-8"), salt).decode("utf-8")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    try:
        return bcrypt.checkpw(
            plain_password.encode("utf-8"),
            hashed_password.encode("utf-8"),
        )
    except Exception:
        return False


def create_access_token(user_id: int, email: str) -> str:
    expire = datetime.now(timezone.utc) + timedelta(minutes=JWT_EXPIRE_MINUTES)
    payload = {
        "sub": str(user_id),
        "email": email,
        "exp": expire,
        "iat": datetime.now(timezone.utc),
    }
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)


def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
    db: Session = Depends(get_db),
) -> User:
    if credentials is None or not credentials.credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="No se proporcionó token de autenticación",
        )
    token = credentials.credentials
    try:
        payload = jwt.decode(token, JWT_SECRET_KEY, algorithms=[JWT_ALGORITHM])
        user_id = int(payload.get("sub", "0"))
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="La sesión expiró, vuelve a iniciar sesión",
        )
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token de sesión inválido",
        )

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="El usuario asociado al token ya no existe",
        )
    return user


def get_optional_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
    db: Session = Depends(get_db),
) -> Optional[User]:
    if credentials is None or not credentials.credentials:
        return None
    try:
        payload = jwt.decode(
            credentials.credentials,
            JWT_SECRET_KEY,
            algorithms=[JWT_ALGORITHM],
        )
        user_id = int(payload.get("sub", "0"))
        return db.query(User).filter(User.id == user_id).first()
    except Exception:
        return None


def verify_admin_key(x_admin_key: Optional[str] = Header(default=None)):
    if x_admin_key != ADMIN_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Se requiere cabecera X-Admin-Key válida para gestionar el catálogo global",
        )
