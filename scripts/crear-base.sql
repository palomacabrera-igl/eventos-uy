-- ============================================================
--  eventos.uy - Creacion de la base de datos
-- ============================================================
--
--  Cada integrante del equipo corre esto UNA VEZ en su maquina.
--  Hay que ejecutarlo como superusuario (postgres).
--
--  Windows:
--    "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -f scripts\crear-base.sql
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
-- ============================================================

CREATE ROLE eventosuy LOGIN PASSWORD 'eventosuy';

CREATE DATABASE eventosuy OWNER eventosuy;
