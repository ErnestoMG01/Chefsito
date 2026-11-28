#!/usr/bin/env bash
# ==============================================================================
# SCRIPT DE APROVISIONAMIENTO AUTOMÁTICO EN AWS EC2 (Ubuntu 22.04 / 24.04)
# Proyecto: Chefsito (Rama feat/243873)
# ==============================================================================
set -e

echo "=== [1/4] Actualizando paquetes del sistema e instalando Docker ==="
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg git ufw

if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com | sudo sh
    sudo usermod -aG docker "$USER" || true
fi

echo "=== [2/4] Configurando Firewall UFW en EC2 (Medidas de Seguridad AWS) ==="
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8000/tcp
sudo ufw --force enable

echo "=== [3/4] Preparando archivo .env de producción ==="
if [ ! -f .env ]; then
    cp backend/.env.example .env
    echo "Archivo .env creado desde backend/.env.example."
    echo "IMPORTANTE: Si usas Amazon RDS PostgreSQL externo, edita DATABASE_URL en .env con el Endpoint de tu RDS."
fi

echo "=== [4/4] Construyendo y levantando servicios de Chefsito Cloud ==="
sudo docker compose up -d --build

echo ""
echo "=================================================================="
echo " DESPLIEGUE COMPLETADO EXITOSAMENTE"
echo " - Healthcheck API: http://$(curl -s ifconfig.me):8000/health"
echo " - Panel Swagger / Admin Recetas: http://$(curl -s ifconfig.me):8000/docs"
echo "=================================================================="
