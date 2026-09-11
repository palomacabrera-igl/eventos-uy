# Documentación de análisis y diseño

Diagramas del proyecto **eventos.uy** (Laboratorio 1 — Programación de Aplicaciones, UTEC).

Están en formato **draw.io** (`.drawio.xml`). Se abren en [app.diagrams.net](https://app.diagrams.net):
*File → Open from → Device*, o arrastrando el archivo a la ventana.

> Cada archivo tiene **varias hojas** (las pestañas de abajo de la pantalla). Ahí está cada
> caso de uso por separado.

---

## Cómo están organizados

Un archivo por **área del sistema**, igual que los menús de la Estación de Trabajo. Así, para
ver cualquier caso de uso, se abre un solo archivo y se cambia de pestaña.

| Archivo | Hojas | Casos de uso |
|---|---|---|
| [`ModeloDeDominio`](ModeloDeDominio.drawio.xml) | 2 | Modelo de dominio · Diagrama de clases de diseño (DCD) |
| [`comunicacion/Usuario`](comunicacion/Usuario.drawio.xml) | 3 | Alta de Usuario · Consulta de Usuario · Modificar Datos de Usuario |
| [`comunicacion/EventosYEdiciones`](comunicacion/EventosYEdiciones.drawio.xml) | 4 | Alta de Evento · Consulta de Evento · Alta de Edición · Consulta de Edición |
| [`comunicacion/InstitucionYPatrocinio`](comunicacion/InstitucionYPatrocinio.drawio.xml) | 3 | Alta de Institución · Alta de Patrocinio · Consulta de Patrocinio |
| [`comunicacion/Categoria`](comunicacion/Categoria.drawio.xml) | 1 | Alta de Categoría |
| [`comunicacion/Registro`](comunicacion/Registro.drawio.xml) | 4 | Alta de Tipo de Registro · Consulta de Tipo de Registro · Registro a Edición · Consulta de Registro |

**15 casos de uso**, uno por cada entrada del menú de la Estación de Trabajo.

---

## Qué hay dentro de cada hoja

Cada caso de uso tiene las tres cosas juntas, una al lado de la otra:

| | Qué es |
|---|---|
| **Diagrama de secuencia del sistema (DSS)** | El actor *Administrador* y la línea de vida `:Sistema`, con las operaciones del caso de uso en orden y los bloques `OPT` / `LOOP` / `ALT` |
| **Tipos de datos (DT)** | Los atributos de cada DataType que cruza entre la lógica y la interfaz |
| **Diagramas de comunicación** | Uno por operación del DSS: qué objetos colaboran, con los mensajes numerados |

> Si al abrir una hoja solo ves una parte, alejá el zoom: **`Ctrl + Shift + H`** encuadra todo.

---

## Cómo exportar a PDF

En draw.io: **File → Export as → PDF**. Marcar **All Pages** para que salgan todas las hojas
en un mismo PDF, y *Crop* para que no queden márgenes vacíos. Guardar en `docs/pdf/`.

---

## Diseño de la solución

Las decisiones de diseño (separación en capas, persistencia, manejo de errores) están en el
[`README.md`](../README.md) de la raíz y en los comentarios del código, sobre todo en:

| Dónde | Qué explica |
|---|---|
| `logica/EntidadBase.java` | Cómo se generan los identificadores |
| `logica/Usuario.java` | La estrategia de herencia `SINGLE_TABLE` y su precio |
| `logica/Categoria.java` | La jerarquía de categorías (auto-asociación padre/hijas) |
| `logica/ManejadorUsuario.java` | Por qué las búsquedas usan JPQL y no `em.find()` |
| `logica/ManejadorEvento.java` | Por qué `actualizar()` no usa `merge()` |
| `persistencia/Persistencia.java` | Por qué hay un solo `EntityManager` en toda la aplicación |
| `logica/DatosDePrueba.java` | Por qué el orden de carga importa con JPA |

---

## Nota sobre el estado de los diagramas

Los diagramas corresponden a la etapa de **análisis y diseño**. Durante la implementación
algunas firmas de operaciones cambiaron —principalmente al unificar el manejo de errores en
`ReglaNegocioException`— y esas diferencias no se reflejaron en todos los diagramas.

**El código es la referencia para las firmas exactas**; los diagramas documentan la estructura
y las colaboraciones entre objetos, que sí se mantienen.
