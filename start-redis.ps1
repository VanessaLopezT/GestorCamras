# Verificar si el script se está ejecutando como administrador
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "Este script requiere privilegios de administrador. Solicitando elevación..." -ForegroundColor Yellow
    
    # Crear un nuevo proceso con privilegios elevados
    $arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$($MyInvocation.MyCommand.Path)`""
    Start-Process powershell -Verb RunAs -ArgumentList $arguments
    exit
}

Write-Host "=== Configuración de Redis para Gestor de Cámaras ===" -ForegroundColor Cyan

# Verificar si Redis está instalado
$redisInstalled = $false
$redisService = Get-Service -Name "Redis" -ErrorAction SilentlyContinue

if ($redisService -ne $null) {
    Write-Host "Redis está instalado como servicio." -ForegroundColor Green
    $redisInstalled = $true
} else {
    Write-Host "Redis no está instalado como servicio." -ForegroundColor Yellow
    
    # Verificar si el ejecutable de Redis está en la reta predeterminada
    $redisPath = "C:\Program Files\Redis\redis-server.exe"
    if (Test-Path $redisPath) {
        Write-Host "Se encontró Redis en $redisPath" -ForegroundColor Green
        $redisInstalled = $true
    } else {
        Write-Host "Redis no está instalado en la ruta predeterminada." -ForegroundColor Red
        $installChoice = Read-Host "¿Deseas instalar Redis automáticamente? (S/N)"
        
        if ($installChoice -eq "S" -or $installChoice -eq "s") {
            try {
                Write-Host "Descargando Redis para Windows..."
                $url = "https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.msi"
                $output = "$env:TEMP\Redis-x64-5.0.14.1.msi"
                
                # Descargar Redis
                Invoke-WebRequest -Uri $url -OutFile $output
                
                # Instalar Redis en modo silencioso
                Write-Host "Instalando Redis..."
                Start-Process msiexec.exe -Wait -ArgumentList "/I $output /quiet /norestart"
                
                # Agregar Redis al PATH
                $env:Path += ";C:\Program Files\Redis"
                [System.Environment]::SetEnvironmentVariable("Path", $env:Path, [System.EnvironmentVariableTarget]::Machine)
                
                Write-Host "Redis se ha instalado correctamente." -ForegroundColor Green
                $redisInstalled = $true
                
                # Iniciar el servicio de Redis
                Start-Service -Name "Redis" -ErrorAction SilentlyContinue
                if ($?) {
                    Write-Host "Servicio de Redis iniciado correctamente." -ForegroundColor Green
                }
            } catch {
                Write-Host "Error al instalar Redis: $_" -ForegroundColor Red
                exit 1
            }
        } else {
            Write-Host "No se instalará Redis. La aplicación podría no funcionar correctamente." -ForegroundColor Yellow
        }
    }
}

# Si Redis está instalado pero el servicio no está en ejecución, intentar iniciarlo
if ($redisInstalled) {
    $service = Get-Service -Name "Redis" -ErrorAction SilentlyContinue
    if ($service -ne $null -and $service.Status -ne "Running") {
        try {
            Write-Host "Iniciando el servicio de Redis..." -ForegroundColor Yellow
            Start-Service -Name "Redis" -ErrorAction Stop
            Write-Host "Servicio de Redis iniciado correctamente." -ForegroundColor Green
        } catch {
            Write-Host "No se pudo iniciar el servicio de Redis: $_" -ForegroundColor Red
            Write-Host "Intentando iniciar Redis manualmente..."
            
            # Intentar iniciar Redis manualmente
            try {
                Start-Process -FilePath "C:\Program Files\Redis\redis-server.exe" -ArgumentList "--service-run"
                Start-Sleep -Seconds 2
                
                # Verificar si se está ejecutando
                $redisProcess = Get-Process -Name "redis-server" -ErrorAction SilentlyContinue
                if ($redisProcess -ne $null) {
                    Write-Host "Redis se ha iniciado manualmente correctamente." -ForegroundColor Green
                } else {
                    throw "No se pudo iniciar Redis manualmente."
                }
            } catch {
                Write-Host "Error al iniciar Redis manualmente: $_" -ForegroundColor Red
                Write-Host "Por favor, inicia Redis manualmente antes de ejecutar la aplicación." -ForegroundColor Red
                exit 1
            }
        }
    } elseif ($service -eq $null) {
        # Si no hay servicio pero Redis está instalado, intentar iniciar directamente
        try {
            Write-Host "Iniciando Redis directamente..." -ForegroundColor Yellow
            Start-Process -FilePath "C:\Program Files\Redis\redis-server.exe" -ArgumentList "--service-run"
            Start-Sleep -Seconds 2
            Write-Host "Redis se ha iniciado correctamente." -ForegroundColor Green
        } catch {
            Write-Host "Error al iniciar Redis: $_" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Redis ya está en ejecución." -ForegroundColor Green
    }
}

# Iniciar la aplicación si se encontró Redis
if ($redisInstalled) {
    Write-Host "\n=== Iniciando Gestor de Cámaras ===" -ForegroundColor Cyan
    
    # Navegar al directorio del proyecto
    $projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    Set-Location $projectDir
    
    # Verificar si es necesario compilar el proyecto
    if (-not (Test-Path "target\gestor-camaras-0.0.1-SNAPSHOT.jar")) {
        Write-Host "Compilando el proyecto..." -ForegroundColor Yellow
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Error al compilar el proyecto. Por favor, verifica los errores de compilación." -ForegroundColor Red
            exit 1
        }
    }
    
    # Iniciar la aplicación
    Write-Host "Iniciando la aplicación..." -ForegroundColor Green
    java -jar "target\gestor-camaras-0.0.1-SNAPSHOT.jar"
} else {
    Write-Host "No se pudo verificar la instalación de Redis. La aplicación podría no funcionar correctamente." -ForegroundColor Red
}
