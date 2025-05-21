$source = "src\main\java\com\example\gestorcamras\Escritorio\ServidorUI.java.new"
$destination = "src\main\java\com\example\gestorcamras\Escritorio\ServidorUI.java"

# Verificar si el archivo de origen existe
if (Test-Path $source) {
    # Copiar el archivo
    Copy-Item -Path $source -Destination $destination -Force
    Write-Host "Archivo actualizado correctamente."
} else {
    Write-Host "Error: No se encontró el archivo de origen: $source"
}
