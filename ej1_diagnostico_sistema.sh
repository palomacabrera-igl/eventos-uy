#!/usr/bin/env bash

# Diagnóstico básico del sistema para Administración de Infraestructuras.

mostrar_uso() {
  echo "Uso: $0 {-d|-m|-p|-u USUARIO|-r [-u USUARIO]}" >&2
}

obtener_disco() {
  # df -P asegura una sola línea de datos aunque el nombre del dispositivo sea largo.
  disco_uso=$(df -P / | tail -n 1 | tr -s ' ' | cut -d ' ' -f 5 | tr -d '%')
}

obtener_memoria() {
  # Se recorre la fila Mem para obtener los campos total y usada en MB.
  for campo in total usada; do
    linea_memoria=$(free -m | grep '^Mem:' | tr -s ' ')
    case "$campo" in
      total) memoria_total=$(printf '%s\n' "$linea_memoria" | cut -d ' ' -f 2) ;;
      usada) memoria_usada=$(printf '%s\n' "$linea_memoria" | cut -d ' ' -f 3) ;;
    esac
  done

  if (( memoria_total > 0 )); then
    memoria_uso=$((memoria_usada * 100 / memoria_total))
  else
    memoria_uso=0
  fi
}

obtener_procesos() {
  procesos=$(ps -e | tail -n +2 | wc -l)
}

obtener_usuarios() {
  usuarios=$(grep -v '^[[:space:]]*#' /etc/passwd | cut -d ':' -f 1 | wc -l)
}

mostrar_usuario() {
  usuario_buscado="$1"
  linea_usuario=$(grep "^${usuario_buscado}:" /etc/passwd)

  if [[ -z "$linea_usuario" ]]; then
    echo "No existe el usuario ${usuario_buscado} en el sistema." >&2
    return 6
  fi

  nombre=$(printf '%s\n' "$linea_usuario" | cut -d ':' -f 1)
  uid=$(printf '%s\n' "$linea_usuario" | cut -d ':' -f 3)
  gid=$(printf '%s\n' "$linea_usuario" | cut -d ':' -f 4)
  home=$(printf '%s\n' "$linea_usuario" | cut -d ':' -f 6)
  shell=$(printf '%s\n' "$linea_usuario" | cut -d ':' -f 7)

  echo "USUARIO=$nombre"
  echo "UID=$uid"
  echo "GID=$gid"
  echo "HOME=$home"
  echo "SHELL=$shell"
}

mostrar_reporte() {
  obtener_disco
  obtener_memoria
  obtener_procesos
  obtener_usuarios

  echo "HOSTNAME=$(hostname)"
  echo "FECHA=$(date '+%Y-%m-%d %H:%M')"
  echo "DISCO_USO=$disco_uso"
  echo "MEMORIA_TOTAL_MB=$memoria_total"
  echo "MEMORIA_USADA_MB=$memoria_usada"
  echo "MEMORIA_USO=$memoria_uso"
  echo "PROCESOS=$procesos"
  echo "USUARIOS=$usuarios"
}

if [[ $# -eq 0 ]]; then
  mostrar_uso
  exit 3
fi

case "$1" in
  -d)
    if [[ $# -ne 1 ]]; then
      echo "Cantidad u orden de parámetros inválido." >&2
      mostrar_uso
      exit 3
    fi
    obtener_disco
    echo "DISCO_USO=$disco_uso"
    ;;
  -m)
    if [[ $# -ne 1 ]]; then
      echo "Cantidad u orden de parámetros inválido." >&2
      mostrar_uso
      exit 3
    fi
    obtener_memoria
    echo "MEMORIA_TOTAL_MB=$memoria_total"
    echo "MEMORIA_USADA_MB=$memoria_usada"
    echo "MEMORIA_USO=$memoria_uso"
    ;;
  -p)
    if [[ $# -ne 1 ]]; then
      echo "Cantidad u orden de parámetros inválido." >&2
      mostrar_uso
      exit 3
    fi
    obtener_procesos
    echo "PROCESOS=$procesos"
    ;;
  -u)
    if [[ $# -eq 1 ]]; then
      echo "La opción -u requiere un usuario." >&2
      exit 2
    fi
    if [[ $# -ne 2 ]]; then
      echo "Cantidad u orden de parámetros inválido." >&2
      mostrar_uso
      exit 3
    fi
    mostrar_usuario "$2" || exit $?
    ;;
  -r)
    if [[ $# -eq 1 ]]; then
      mostrar_reporte
    elif [[ $# -eq 2 && "$2" == "-u" ]]; then
      echo "La opción -u requiere un usuario." >&2
      exit 2
    elif [[ $# -eq 3 && "$2" == "-u" ]]; then
      mostrar_reporte
      mostrar_usuario "$3" || exit $?
    else
      echo "Cantidad u orden de parámetros inválido." >&2
      mostrar_uso
      exit 3
    fi
    ;;
  *)
    echo "Opción desconocida: $1" >&2
    mostrar_uso
    exit 4
    ;;
esac

exit 0
