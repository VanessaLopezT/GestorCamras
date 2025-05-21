@echo off
echo Verificando si Redis está en ejecución...

tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I /N "redis-server.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Redis ya está en ejecución.
) else (
    echo Iniciando Redis...
    start "" "C:\Program Files\Redis\redis-server.exe" --service-run
    timeout /t 2 /nobreak > NUL
    echo Redis se ha iniciado correctamente.
)

echo Iniciando la aplicación...
start "" "C:\Program Files\Java\jdk-17\bin\java.exe" -jar target\gestor-camaras-0.0.1-SNAPSHOT.jar

echo Si la aplicación no se inicia automáticamente, por favor ábrela manualmente.
