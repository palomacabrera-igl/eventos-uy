-- Deja la asistencia y las visitas de los datos de prueba como las carga
-- DatosDePrueba (seccion "Rol 4"), para repetir la demostracion sin recrear la base.
--
-- Una demostracion los cambia: el asistente que registra su asistencia desde el
-- celular, las visitas que suma el sitio al consultar un evento. Este script los
-- vuelve a su valor inicial. NO borra registros, usuarios ni eventos, y no toca
-- nada mas que estas dos columnas: registro.asistio y evento.visitas.
--
-- Se corre con el usuario de la aplicacion, desde la raiz del repositorio:
--
--   $env:PGPASSWORD = "eventosuy"
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U eventosuy -h localhost -d eventosuy -f scripts\restaurar-asistencia-y-visitas.sql
--
-- Se puede correr las veces que haga falta: siempre deja lo mismo.
--
-- Si el grupo cambia esos datos en DatosDePrueba (otras visitas, otros registros
-- con asistencia), hay que cambiarlos tambien aca.

-- Los nombres llevan tildes: sin esto, en Windows psql lee el archivo con la
-- codificacion de la consola y falla con "secuencia de bytes no valida".
\encoding UTF8

BEGIN;

-- Asistencia: nadie asistio, salvo estos tres registros.
UPDATE registro SET asistio = false;

UPDATE registro r
   SET asistio = true
  FROM usuario u, edicion_evento ed
 WHERE u.id = r.asistente_id
   AND ed.id = r.edicion_id
   AND (ed.nombre, u.nickname) IN (
         ('JIAP 2026',    'vale23'),
         ('Comics 2026',  'jperez'),
         ('Maratón 2026', 'vale23'));

-- Visitas: en cero, salvo los cinco eventos que cargan los datos de prueba.
UPDATE evento SET visitas = 0;

UPDATE evento e
   SET visitas = v.visitas
  FROM (VALUES ('Montevideo Rock',        24),
               ('JIAP',                   17),
               ('Maratón de Montevideo',  12),
               ('Montevideo Comics',       7),
               ('Feria del libro',         3)) AS v(nombre, visitas)
 WHERE e.nombre = v.nombre;

COMMIT;

-- Lo que quedo, para controlar de un vistazo.
SELECT ed.nombre AS edicion, u.nickname AS asistente, r.asistio
  FROM registro r
  JOIN edicion_evento ed ON ed.id = r.edicion_id
  JOIN usuario u ON u.id = r.asistente_id
 ORDER BY ed.nombre, u.nickname;

SELECT nombre AS evento, visitas FROM evento ORDER BY visitas DESC, nombre;
