<#
.SYNOPSIS
  Compila el sitio y lo deja andando en Tomcat.

.DESCRIPTION
  Arma el WAR con el Maven Wrapper, lo despliega en Tomcat y lo deja
  corriendo en segundo plano.

  Funciona con el PowerShell que viene con Windows (5.1): no hace falta
  instalar PowerShell 7. Solo necesita JAVA_HOME apuntando a un JDK 25.

  Tomcat NO se baja de internet: se copia una sola vez desde el ZIP de la
  demo del curso (ProyectoWeb.zip -> apache-tomcat-11.0.26). El script lo
  busca, en este orden:

    1. La variable de entorno CATALINA_HOME, si esta definida.
    2. <carpeta del usuario>\tomcat\apache-tomcat-11.0.26
    3. tools\apache-tomcat-11.0.26 dentro del proyecto (como era antes).

  Lo recomendado es el 2: Tomcat es un servidor de aplicaciones, no parte
  del codigo, y adentro del proyecto ensucia el arbol que se ve en IntelliJ.

.PARAMETER Detener
  Para el Tomcat de este proyecto.

.PARAMETER Reiniciar
  Para, vuelve a compilar y arranca de nuevo.

.EXAMPLE
  .\scripts\servidor.ps1
  .\scripts\servidor.ps1 -Reiniciar
  .\scripts\servidor.ps1 -Detener
#>

[CmdletBinding()]
param(
    [switch]$Detener,
    [switch]$Reiniciar
)

$ErrorActionPreference = "Stop"

$VersionTomcat = "11.0.26"
$Puerto        = 8080
$Raiz          = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Url           = "http://localhost:$Puerto/eventos"

# Donde esta Tomcat. Se prueban los tres lugares y gana el primero que exista,
# asi cada integrante lo puede tener donde le quede comodo sin tocar el script.
function Buscar-Tomcat {
    $candidatos = @()
    if ($env:CATALINA_HOME) { $candidatos += $env:CATALINA_HOME }
    $candidatos += Join-Path $HOME "tomcat\apache-tomcat-$VersionTomcat"
    $candidatos += Join-Path $Raiz "tools\apache-tomcat-$VersionTomcat"

    foreach ($c in $candidatos) {
        if ($c -and (Test-Path (Join-Path $c "bin\catalina.bat"))) { return $c }
    }
    return $candidatos[1]   # el recomendado, para poder avisar donde ponerlo
}

$Tomcat        = Buscar-Tomcat
$Webapps       = Join-Path $Tomcat "webapps"
$Recomendado   = Join-Path $HOME "tomcat\apache-tomcat-$VersionTomcat"
$EnElProyecto  = Join-Path $Raiz "tools\apache-tomcat-$VersionTomcat"

function Paso($texto)  { Write-Host ""; Write-Host "==> $texto" -ForegroundColor Cyan }
function Listo($texto) { Write-Host $texto -ForegroundColor Green }
function Aviso($texto) { Write-Host $texto -ForegroundColor Yellow }

function Parar-Tomcat {
    # Se usa el apagado del propio Tomcat y no se mata el proceso.
    #
    # Por que: catalina.bat es un archivo .bat, y al arrancarlo Windows corre
    # primero un cmd.exe que despues lanza el java de verdad y se va. Si se
    # guarda el numero de proceso del .bat y se lo mata, se mata al cmd que ya
    # no existe y el java sigue vivo ocupando el puerto 8080.
    #
    # "catalina.bat stop" le habla al propio Tomcat por su puerto de apagado y
    # lo cierra ordenadamente, cierre de JPA incluido.
    if (-not (Test-Path $Tomcat)) {
        Aviso "No se encontro Tomcat, no hay nada que parar."
        return
    }

    Paso "Parando Tomcat"
    $env:CATALINA_HOME = $Tomcat
    $env:CATALINA_BASE = $Tomcat

    $antes = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & (Join-Path $Tomcat "bin\catalina.bat") stop 2>$null | Out-Null
    $ErrorActionPreference = $antes

    Start-Sleep -Seconds 3
    $ocupado = Get-NetTCPConnection -LocalPort $Puerto -State Listen -ErrorAction SilentlyContinue
    if ($ocupado) {
        Aviso "El puerto $Puerto sigue ocupado. Esperá unos segundos y volvé a probar."
    } else {
        Listo "Tomcat detenido."
    }
}

# ---------------------------------------------------------------- detener
if ($Detener) {
    Parar-Tomcat
    return
}

# ------------------------------------------------- comprobaciones previas
if (-not (Test-Path $Tomcat)) {
    Write-Host ""
    Write-Host "No se encontro Tomcat en:" -ForegroundColor Red
    Write-Host "  $Tomcat"
    Write-Host ""
    Write-Host "Copialo una sola vez desde el ZIP de la demo del curso:"
    Write-Host "  ProyectoWeb.zip -> ProyectoWeb\tools\apache-tomcat-$VersionTomcat"
    Write-Host ""
    Write-Host "y pegalo en cualquiera de estos lugares:"
    Write-Host "  $Recomendado   (recomendado)"
    Write-Host "  $EnElProyecto"
    Write-Host ""
    Write-Host "o defini CATALINA_HOME apuntando a donde ya lo tengas."
    Write-Host ""
    exit 1
}

if (-not $env:JAVA_HOME) {
    Write-Host "Falta la variable JAVA_HOME apuntando a un JDK 25." -ForegroundColor Red
    exit 1
}

if ($Reiniciar) { Parar-Tomcat }

# --------------------------------------------------------------- compilar
Paso "Compilando el proyecto"
Push-Location $Raiz
try {
    # En PowerShell 5.1, con ErrorActionPreference en Stop, cualquier linea que
    # un programa externo escriba en la salida de errores se toma como un fallo.
    # Maven y la JVM escriben avisos ahi aunque todo vaya bien, asi que durante
    # la compilacion se afloja y despues se mira el codigo de salida de verdad.
    $antes = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & (Join-Path $Raiz "mvnw.cmd") -B -ntp install -DskipTests
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = $antes
    if ($codigo -ne 0) { throw "La compilacion fallo (codigo $codigo)." }
} finally {
    Pop-Location
}

$War = Join-Path $Raiz "servidor-web\target\eventos-uy.war"
if (-not (Test-Path $War)) { throw "No se genero el WAR en $War" }

# --------------------------------------------------------------- desplegar
Paso "Desplegando el sitio"
# Se despliega como ROOT para que quede en la raiz: http://localhost:8080/
$RootDir = Join-Path $Webapps "ROOT"
$RootWar = Join-Path $Webapps "ROOT.war"
if (Test-Path $RootDir) { Remove-Item $RootDir -Recurse -Force }
if (Test-Path $RootWar) { Remove-Item $RootWar -Force }
Copy-Item $War $RootWar
Listo "WAR copiado a webapps\ROOT.war"

# ---------------------------------------------------------------- arrancar
Paso "Arrancando Tomcat"
$env:CATALINA_HOME = $Tomcat
$env:CATALINA_BASE = $Tomcat

$null = Start-Process -FilePath (Join-Path $Tomcat "bin\catalina.bat") `
                         -ArgumentList "run" `
                         -WorkingDirectory $Tomcat `
                         -WindowStyle Hidden `
                         -PassThru

Paso "Esperando a que responda"
$listo = $false
foreach ($i in 1..40) {
    Start-Sleep -Seconds 1
    try {
        $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
        if ($r.StatusCode -eq 200) { $listo = $true; break }
    } catch {
        # todavia no levanto, se sigue esperando
    }
}

Write-Host ""
if ($listo) {
    Listo "El sitio esta andando:"
    Write-Host "  $Url                              (cartelera desde PostgreSQL)"
    Write-Host "  http://localhost:$Puerto/index.html   (el sitio estatico de la Parte 1)"
    Write-Host ""
    Write-Host "Para pararlo:  .\scripts\servidor.ps1 -Detener"
} else {
    Aviso "Tomcat arranco pero el sitio no respondio en 40 segundos."
    Write-Host "Mira el log:  $(Join-Path $Tomcat 'logs\localhost.*.log')"
}
