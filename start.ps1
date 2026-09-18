<#
Levanta la aplicacion empleados (app + PostgreSQL) con Docker Compose.
Si Docker Desktop no esta corriendo, lo inicia automaticamente y espera
a que el daemon quede listo antes de ejecutar `docker compose up --build`.
#>

function Test-DockerReady {
    docker info *> $null
    return $LASTEXITCODE -eq 0
}

if (-not (Test-DockerReady)) {

    Write-Host "Docker no esta corriendo. Buscando Docker Desktop..."

    $candidatos = @(
        "$env:ProgramFiles\Docker\Docker\Docker Desktop.exe",
        "$env:LocalAppData\Programs\DockerDesktop\Docker Desktop.exe"
    )

    $dockerDesktop = $candidatos | Where-Object { Test-Path $_ } | Select-Object -First 1

    if (-not $dockerDesktop) {
        Write-Error "No se encontro Docker Desktop instalado. Instalalo desde https://www.docker.com/products/docker-desktop/ y vuelve a ejecutar este script."
        exit 1
    }

    Start-Process -FilePath $dockerDesktop
    Write-Host "Iniciando Docker Desktop, esto puede tardar un minuto..."

    $intentos = 0
    while (-not (Test-DockerReady)) {

        $intentos++

        if ($intentos -gt 30) {
            Write-Error "Docker no quedo listo tras esperar 5 minutos. Revisa Docker Desktop manualmente."
            exit 1
        }

        Start-Sleep -Seconds 10
    }

    Write-Host "Docker esta listo."
}

Write-Host "Levantando app + PostgreSQL con docker compose..."
docker compose up --build
