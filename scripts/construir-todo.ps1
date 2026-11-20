<#
.SYNOPSIS
  Un solo comando que genera los dos paquetes de eventos.uy:
  servidor.jar (Servidor Central + Estacion de Trabajo) y web.war (el sitio).

.DESCRIPTION
  La letra (seccion 7.9) pide un unico comando, documentado, que genere los dos
  paquetes. El codigo esta en DOS repositorios (lo pidio el profesor: lo mas
  independientes posible), asi que este script compila los dos y deja el
  resultado en una sola carpeta:

    entrega\servidor.jar   se arranca con:  java -jar servidor.jar
    entrega\web.war        se despliega en Tomcat (administrador de aplicaciones
                           o copiandolo a la carpeta webapps)

  Por defecto busca el repositorio del sitio al lado de este, con su nombre de
  GitHub (..\eventos-uy-web). Si esta en otro lugar, se indica con -RutaSitio.

  Funciona con el PowerShell que viene con Windows (5.1). Necesita JAVA_HOME
  apuntando a un JDK 25. No hace falta instalar Maven: cada repositorio trae el
  Maven Wrapper (mvnw).

.PARAMETER RutaSitio
  Carpeta del repositorio del sitio (eventos-uy-web).

.PARAMETER ConPruebas
  Corre tambien las pruebas de la logica antes de empaquetar. Las de
  integracion necesitan PostgreSQL con los datos de prueba cargados.

.EXAMPLE
  .\scripts\construir-todo.ps1
  .\scripts\construir-todo.ps1 -RutaSitio C:\proyectos\eventos-uy-web
  .\scripts\construir-todo.ps1 -ConPruebas
#>

[CmdletBinding()]
param(
    [string]$RutaSitio,
    [switch]$ConPruebas
)

$ErrorActionPreference = "Stop"

$RaizCentral = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (-not $RutaSitio) {
    $RutaSitio = Join-Path (Split-Path $RaizCentral -Parent) "eventos-uy-web"
}
$Entrega = Join-Path $RaizCentral "entrega"

function Paso([string]$Texto) {
    Write-Host ""
    Write-Host "==> $Texto" -ForegroundColor Cyan
}

# Corre el Maven Wrapper de un repositorio y corta si falla.
function Maven([string]$Carpeta, [string[]]$Argumentos) {
    Push-Location $Carpeta
    try {
        & .\mvnw.cmd @Argumentos
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo la compilacion en $Carpeta (codigo $LASTEXITCODE)."
        }
    } finally {
        Pop-Location
    }
}

# ------------------------------------------------------------ comprobaciones
if (-not $env:JAVA_HOME) {
    throw "Falta la variable JAVA_HOME apuntando a un JDK 25."
}
if (-not (Test-Path (Join-Path $RutaSitio "pom.xml"))) {
    throw "No encuentro el repositorio del sitio en $RutaSitio. Indicarlo con -RutaSitio."
}

# ------------------------------------------------- 1. Servidor Central (jar)
Paso "Servidor Central y Estacion de Trabajo ($RaizCentral)"
$argumentosCentral = @("-q", "package")
if (-not $ConPruebas) {
    $argumentosCentral += "-DskipTests"
}
Maven $RaizCentral $argumentosCentral

# ------------------------------------------------------- 2. Sitio (war)
Paso "Servidor Web ($RutaSitio)"
Maven $RutaSitio @("-q", "package")

# --------------------------------------------------- 3. Juntar en entrega\
Paso "Juntando los dos paquetes en $Entrega"
New-Item -ItemType Directory -Force -Path $Entrega | Out-Null

$jar = Join-Path $RaizCentral "estacion-trabajo\target\servidor.jar"
$war = Get-ChildItem -Path (Join-Path $RutaSitio "target") -Filter "*.war" | Select-Object -First 1
if (-not (Test-Path $jar)) { throw "No se genero $jar" }
if (-not $war) { throw "No se genero el WAR en $RutaSitio\target" }

Copy-Item $jar (Join-Path $Entrega "servidor.jar") -Force
Copy-Item $war.FullName (Join-Path $Entrega "web.war") -Force

# ------------------------------------------------------------- resumen
Write-Host ""
Write-Host "Listo:" -ForegroundColor Green
Get-ChildItem $Entrega | ForEach-Object {
    Write-Host ("  {0,-14} {1,6:N1} MB" -f $_.Name, ($_.Length / 1MB))
}
Write-Host ""
Write-Host "Servidor Central:  java -jar entrega\servidor.jar"
Write-Host "                   (configuracion en ~/.eventosUy/central.properties)"
Write-Host "Servidor Web:      desplegar entrega\web.war en Tomcat"
Write-Host "                   (configuracion en ~/.eventosUy/web.properties)"
