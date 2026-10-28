<#
.SYNOPSIS
  Publica el JAR del Servidor Central adentro del repositorio del sitio.

.DESCRIPTION
  Compila el modulo logica y copia dos archivos al repositorio del servidor
  web, dentro de su carpeta lib/:

    logica-<version>.jar   el Servidor Central compilado
    logica-<version>.pom   que bibliotecas necesita para funcionar

  El .pom es el que casi siempre se olvida. Sin el, Maven usa el JAR pero no
  sabe que hace falta Hibernate ni el driver de PostgreSQL: el sitio compila,
  arranca, y revienta en el primer acceso a la base. Se genera leyendo el
  pom.xml de la logica, asi que si manana se agrega una biblioteca, aparece
  sola.

  Se corre cada vez que cambia la logica. Despues hay que commitear los dos
  archivos en el repositorio del sitio.

.PARAMETER Destino
  La carpeta del repositorio del servidor web. Por defecto, una carpeta
  llamada eventos-uy-web al lado de esta.

.EXAMPLE
  .\scripts\publicar-logica.ps1
  .\scripts\publicar-logica.ps1 -Destino C:\Users\yo\GitHub\eventos-uy-web
#>

[CmdletBinding()]
param(
    [string]$Destino
)

$ErrorActionPreference = "Stop"

$Raiz = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (-not $Destino) {
    $Destino = Join-Path (Split-Path $Raiz -Parent) "eventos-uy-web"
}

function Paso($texto)  { Write-Host ""; Write-Host "==> $texto" -ForegroundColor Cyan }
function Listo($texto) { Write-Host $texto -ForegroundColor Green }
function Error2($texto){ Write-Host $texto -ForegroundColor Red }

if (-not (Test-Path $Destino)) {
    Error2 "No existe la carpeta del repositorio del sitio:"
    Write-Host "  $Destino"
    Write-Host ""
    Write-Host "Clonalo primero, o pasa la ruta con -Destino."
    exit 1
}

# ---------------------------------------------------------------- 1. compilar
Paso "Compilando el Servidor Central"

# Maven escribe avisos por la salida de error y PowerShell 5.1 los toma como
# fallas. Se afloja un momento y se mira el codigo de salida, que es lo que
# de verdad dice si anduvo.
$previo = $ErrorActionPreference
$ErrorActionPreference = "Continue"
& (Join-Path $Raiz "mvnw.cmd") -q -f (Join-Path $Raiz "pom.xml") -pl logica install "-DskipTests" 2>&1 | Out-Host
$codigo = $LASTEXITCODE
$ErrorActionPreference = $previo

if ($codigo -ne 0) {
    Error2 "La logica no compila. No se publica nada."
    exit 1
}

# ------------------------------------------------------- 2. version y archivos
[xml]$pomLogica = Get-Content (Join-Path $Raiz "logica\pom.xml")

# La version la hereda del proyecto padre, asi que puede no estar en su pom.
$version = $pomLogica.project.version
if (-not $version) { $version = $pomLogica.project.parent.version }

$grupo  = $pomLogica.project.groupId
if (-not $grupo) { $grupo = $pomLogica.project.parent.groupId }

$jarOrigen = Join-Path $Raiz "logica\target\logica-$version.jar"
if (-not (Test-Path $jarOrigen)) {
    Error2 "No se encontro el JAR compilado en:"
    Write-Host "  $jarOrigen"
    exit 1
}

# La carpeta lib/ del sitio imita la forma de un repositorio de Maven:
# lib\<grupo con barras>\<artefacto>\<version>\
$carpeta = Join-Path $Destino ("lib\" + ($grupo -replace '\.', '\') + "\logica\$version")
New-Item -ItemType Directory -Force $carpeta | Out-Null

Paso "Copiando el JAR"
Copy-Item $jarOrigen (Join-Path $carpeta "logica-$version.jar") -Force
Listo ("  logica-$version.jar  (" + [math]::Round((Get-Item $jarOrigen).Length / 1KB) + " KB)")

# ------------------------------------------------------------- 3. la ficha
Paso "Generando la ficha de dependencias"

$deps = @($pomLogica.project.dependencies.dependency | Where-Object { $_.scope -ne 'test' })

$lineas = foreach ($d in $deps) {
    $v = $d.version
    # Las versiones escritas como ${algo} salen de las propiedades del padre.
    if ($v -match '^\$\{(.+)\}$') {
        [xml]$pomPadre = Get-Content (Join-Path $Raiz "pom.xml")
        $v = $pomPadre.project.properties.($matches[1])
    }
    "        <dependency>`n" +
    "            <groupId>$($d.groupId)</groupId>`n" +
    "            <artifactId>$($d.artifactId)</artifactId>`n" +
    "            <version>$v</version>`n" +
    "        </dependency>"
}

$ficha = @"
<?xml version="1.0" encoding="UTF-8"?>
<!--
    GENERADO por scripts/publicar-logica.ps1 del repositorio de la logica.
    No se edita a mano: se regenera junto con el JAR.

    Le dice a Maven que bibliotecas necesita el Servidor Central, para que
    terminen tambien adentro del WAR. No lleva <parent>, porque este
    repositorio no tiene el proyecto padre.
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>$grupo</groupId>
    <artifactId>logica</artifactId>
    <version>$version</version>
    <packaging>jar</packaging>
    <name>EventosUY - Logica (Servidor Central)</name>

    <dependencies>
$($lineas -join "`n")
    </dependencies>
</project>
"@

Set-Content -Path (Join-Path $carpeta "logica-$version.pom") -Value $ficha -Encoding UTF8
Listo "  logica-$version.pom   ($($deps.Count) dependencias)"

foreach ($d in $deps) {
    Write-Host "     - $($d.artifactId)"
}

# ------------------------------------------------------------------- 4. listo
Paso "Publicado"
Write-Host "  en: $carpeta"
Write-Host ""
Write-Host "Ahora, en el repositorio del sitio:" -ForegroundColor Yellow
Write-Host "  1. commitea los dos archivos de lib/"
Write-Host "  2. .\scripts\servidor.ps1 -Reiniciar   para verlo andando"
Write-Host ""
