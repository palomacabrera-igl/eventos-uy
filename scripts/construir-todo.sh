#!/usr/bin/env bash
#
# Un solo comando que genera los dos paquetes de eventos.uy (letra, 7.9):
#
#   entrega/servidor.jar   Servidor Central + Estacion de Trabajo:  java -jar servidor.jar
#   entrega/web.war        el sitio, para desplegar en Tomcat
#
# Es la misma idea que construir-todo.ps1, para Linux y macOS. El codigo esta en
# dos repositorios: busca el del sitio al lado de este (../eventos-uy-web) o en
# la carpeta que se pase como primer argumento.
#
#   ./scripts/construir-todo.sh
#   ./scripts/construir-todo.sh /ruta/a/eventos-uy-web
#
# Necesita JAVA_HOME apuntando a un JDK 25. Las pruebas no se corren (para eso:
# ./mvnw -pl logica test).

set -euo pipefail

RAIZ_CENTRAL="$(cd "$(dirname "$0")/.." && pwd)"
RUTA_SITIO="${1:-$(dirname "$RAIZ_CENTRAL")/eventos-uy-web}"
ENTREGA="$RAIZ_CENTRAL/entrega"

if [ ! -f "$RUTA_SITIO/pom.xml" ]; then
    echo "No encuentro el repositorio del sitio en $RUTA_SITIO. Pasarlo como primer argumento." >&2
    exit 1
fi

echo "==> Servidor Central y Estacion de Trabajo ($RAIZ_CENTRAL)"
(cd "$RAIZ_CENTRAL" && ./mvnw -q package -DskipTests)

echo "==> Servidor Web ($RUTA_SITIO)"
(cd "$RUTA_SITIO" && ./mvnw -q package)

echo "==> Juntando los dos paquetes en $ENTREGA"
mkdir -p "$ENTREGA"
cp "$RAIZ_CENTRAL/estacion-trabajo/target/servidor.jar" "$ENTREGA/servidor.jar"
cp "$(ls "$RUTA_SITIO"/target/*.war | head -n 1)" "$ENTREGA/web.war"

echo
echo "Listo:"
ls -lh "$ENTREGA"
echo
echo "Servidor Central:  java -jar entrega/servidor.jar"
echo "Servidor Web:      desplegar entrega/web.war en Tomcat"
