-- ============================================================
--  eventos.uy - Creacion de la base de datos
-- ============================================================
--
--  Cada integrante del equipo corre esto UNA VEZ en su maquina.
--  Hay que ejecutarlo como superusuario (postgres).
--
--  Windows:
--    & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -f scripts\crear-base.sql
--
--  Linux / macOS:
--    psql -U postgres -f scripts/crear-base.sql
--
--  Pide la contrasenia del usuario 'postgres' (la que cada uno puso al
--  instalar PostgreSQL). Esa contrasenia NO va al repositorio.
--
--  Despues de correrlo, el sistema se conecta con el usuario 'eventosuy',
--  que es lo que dice src/main/resources/META-INF/persistence.xml.
--  Asi no dependemos de que todos tengamos la misma clave de 'postgres'.
--
--  Las tablas NO se crean aca: las crea Hibernate solo al arrancar,
--  porque persistence.xml tiene hibernate.hbm2ddl.auto = update.
--
--  Se puede correr mas de una vez. Si el rol o la base ya existen, esas
--  dos lineas dan error ("ya existe") y psql sigue con la siguiente:
--  no rompe nada.
-- ============================================================

CREATE ROLE eventosuy LOGIN PASSWORD 'eventosuy';

CREATE DATABASE eventosuy OWNER eventosuy;

-- ------------------------------------------------------------
--  IMPORTANTE (PostgreSQL 15 en adelante)
-- ------------------------------------------------------------
--  Desde la version 15, el esquema 'public' de cada base le da permiso
--  de CREAR tablas SOLO al dueño de esa base. A cualquier otro usuario
--  le da nada mas permiso de USO (leer lo que ya existe).
--
--  Si la base ya existia de antes (creada a mano, con pgAdmin, o por una
--  version anterior de este script), el CREATE DATABASE de arriba falla y
--  el dueño queda siendo 'postgres'. En ese caso Hibernate arranca, se
--  conecta bien, y despues falla con:
--
--      ERROR: permiso denegado al esquema public
--
--  Esta linea corrige el dueño siempre, exista o no la base de antes.
--  No hace falta ningun GRANT extra: 'pg_database_owner' es un rol
--  implicito que significa "el dueño de esta base", y el esquema public
--  ya le concede el permiso de crear.
-- ------------------------------------------------------------

ALTER DATABASE eventosuy OWNER TO eventosuy;
