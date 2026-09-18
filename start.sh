#!/usr/bin/env bash
set -e

if ! docker info > /dev/null 2>&1; then
    echo "Docker no esta corriendo. Inicia Docker Desktop (o el servicio docker) y vuelve a ejecutar este script."
    exit 1
fi

echo "Levantando app + PostgreSQL con docker compose..."
docker compose up --build
