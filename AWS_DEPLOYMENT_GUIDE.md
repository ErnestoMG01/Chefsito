# Guía de Despliegue y Seguridad en Laboratorio de AWS (Rama `feat/243873`)

Esta guía detalla paso a paso cómo desplegar el backend multiusuario y la base de datos PostgreSQL de **Chefsito** en un **Laboratorio de AWS (AWS Academy Learner Lab)** cumpliendo con las medidas de seguridad, concurrencia de usuarios y actualización dinámica de recetas.

---

## 1. Arquitectura Desplegada en AWS

```
[ Teléfonos Android (Chefsito App) ]
        │  HTTPS / HTTP + JWT Bearer Token
        ▼
[ AWS VPC (10.0.0.0/16) ]
  ├── Subred Pública:
  │     └── Amazon EC2 (t3.micro / Ubuntu 24.04)
  │           ├── Security Group: sg-chefsito-backend (Puertos 8000/443 abiertos; SSH 22 solo a Mi IP)
  │           └── Contenedor Docker: FastAPI + Uvicorn Workers + Pool SQLAlchemy
  │                 ├── Autenticación Bcrypt + JWT + Rate Limiting por IP
  │                 └── Ingestador Dinámico (TheMealDB Mexican + DummyJSON + Carga JSON)
  │
  └── Subred Privada (Sin acceso público desde Internet):
        └── Amazon RDS para PostgreSQL 16 (db.t3.micro)
              ├── Security Group: sg-chefsito-rds (Puerto 5432 abierto SOLO a sg-chefsito-backend)
              └── Tablas Multiusuario: users, recipes, user_pantry, user_favorites
```

---

## 2. Paso a Paso en la Consola de AWS Learner Lab

### Paso 2.1: Crear los Grupos de Seguridad (Security Groups)
1. En la consola de AWS ve a **EC2 → Security Groups → Create security group**.
2. **Grupo 1 (`sg-chefsito-backend`):**
   - **Inbound Rules (Entrada):**
     - `SSH (TCP 22)` → Source: **My IP** (restringe acceso administrativo sólo a tu equipo).
     - `Custom TCP (TCP 8000)` → Source: `0.0.0.0/0` (para que los teléfonos Android consuman la API).
     - `HTTPS (TCP 443)` → Source: `0.0.0.0/0`.
3. **Grupo 2 (`sg-chefsito-rds`):**
   - **Inbound Rules (Entrada):**
     - `PostgreSQL (TCP 5432)` → Source: Selecciona el Security Group **`sg-chefsito-backend`** (nunca `0.0.0.0/0`). Así nadie desde Internet puede atacar directamente la base de datos.

### Paso 2.2: Desplegar la Base de Datos en Amazon RDS (PostgreSQL)
1. Ve a **Amazon RDS → Create database**:
   - **Engine:** `PostgreSQL` (versión 16).
   - **Templates:** `Free tier` o `Sandbox / Dev/Test` (según permita el laboratorio).
   - **DB instance identifier:** `chefsito-rds-db`
   - **Master username:** `chefsito_admin`
   - **Master password:** `ChefsitoSecure2026!`
   - **Instance configuration:** `Burstable classes` → `db.t3.micro`.
   - **Public access:** **No** (solo accesible desde la VPC donde vive tu EC2).
   - **VPC security group:** Elige `sg-chefsito-rds`.
   - **Additional configuration → Initial database name:** `chefsito_db`
   - **Encryption:** Activa *Enable encryption* (cifrado en reposo con AWS KMS).
2. Una vez que el estado sea **Available**, copia el **Endpoint** (ej. `chefsito-rds-db.xxxxxx.us-east-1.rds.amazonaws.com`).

### Paso 2.3: Desplegar el Servidor API en Amazon EC2
1. Ve a **EC2 → Launch instance**:
   - **Name:** `chefsito-api-server`
   - **AMI:** `Ubuntu Server 24.04 LTS (HVM)`
   - **Instance type:** `t3.micro` o `t2.micro`
   - **Key pair:** Selecciona `vockey` (la llave del AWS Learner Lab) o crea una nueva.
   - **Network settings:** Asigna el Security Group `sg-chefsito-backend` y habilita **Auto-assign public IP**.
   - **Advanced details → IAM instance profile:** Selecciona **`LabInstanceProfile`** (`LabRole`).
2. Conéctate por SSH a la instancia EC2 y ejecuta:

```bash
git clone -b feat/243873 https://github.com/ErnestoMG01/Chefsito.git
cd Chefsito

# Si usas Amazon RDS externo, configura el endpoint antes de levantar:
cp backend/.env.example .env
nano .env
# Cambia DATABASE_URL=postgresql://chefsito_admin:ChefsitoSecure2026!@<TU-ENDPOINT-RDS>:5432/chefsito_db

chmod +x deploy_aws_ec2.sh
./deploy_aws_ec2.sh
```

> **Nota para ahorro de créditos en el Lab:** Si prefieres no gastar créditos de RDS mientras haces pruebas, `docker compose up -d --build` ya incluye un contenedor `postgres:16-alpine` preconfigurado y aislado en `127.0.0.1:5432`.

---

## 3. Cómo Cargar y Actualizar Recetas Dinámicamente (Sin Recompilar la App)

El proyecto ahora cuenta con **4 mecanismos** para que el catálogo de recetas crezca continuamente:

1. **Catálogo Cloud Ampliado al Arrancar (36 recetas mexicanas):**
   - Al iniciar el backend en AWS se siembran automáticamente las 24 recetas originales más **12 nuevas recetas mexicanas** exclusivas de la nube (`AWS_CLOUD_CATALOG`).
2. **Ingesta Automática desde Proveedores Externos (`POST /api/v1/recipes/sync-external`):**
   - El servidor consulta en vivo **TheMealDB (Cocina Mexicana)** y **DummyJSON**, traduce los ingredientes al español, calcula automáticamente el costo estimado en MXN por porción usando el diccionario de precios de Chefsito y los inserta en PostgreSQL.
   - Desde la app Android, el usuario solo pulsa el botón **"Actualizar de AWS"** en la pantalla **Configurar Plan** y recibe todas las recetas nuevas al instante.
3. **Creación Directa desde la App Android (Botón "+ Nueva receta"):**
   - En la pantalla **Configurar Plan**, el botón **"Nueva receta"** abre un formulario que envía la receta directamente al servidor AWS (`POST /api/v1/recipes`) y la deja disponible inmediatamente para todos los usuarios conectados.
4. **Carga Masiva por JSON desde el Panel Swagger (`http://<IP-EC2>:8000/docs`):**
   - Entrando en el navegador a `http://<IP-PUBLICA-EC2>:8000/docs`, puedes usar el endpoint `POST /api/v1/recipes/bulk` con la cabecera `X-Admin-Key: chefsito-admin-243873` para subir decenas de recetas nuevas en un solo JSON.

---

## 4. Conexión de la App Android con la Instancia AWS EC2

1. Abre la app **Chefsito** en los teléfonos o emuladores.
2. Ve a la pestaña **Mi Perfil** → sección **Servidor AWS Cloud (EC2 + RDS)**.
3. Ingresa `http://<IP_PUBLICA_DE_TU_EC2>:8000` y pulsa **Guardar conexión AWS**.
4. Ahora el registro de usuarios, login con JWT, despensa con costos por usuario, favoritos y catálogo dinámico de recetas operan de forma multiusuario y concurrente contra tu base de datos en AWS.
