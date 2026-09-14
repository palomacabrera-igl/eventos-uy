# Documentación de análisis y diseño

Diagramas del proyecto **eventos.uy** (Laboratorio 1 — Programación de Aplicaciones, UTEC).

Todos los diagramas están en **dos formatos**, con el mismo contenido. Se puede usar el que resulte más cómodo:

| Formato | Para qué sirve | Cómo se abre |
|---|---|---|
| **PDF** (`.pdf`) | Leerlos o imprimirlos | Con cualquier visor de PDF o con el navegador |
| **draw.io** (`.drawio.xml`) | Recorrerlos con zoom o modificarlos | Con draw.io, de escritorio o en su versión web ([app.diagrams.net](https://app.diagrams.net)): *File → Open from → Device*, o arrastrando el archivo a la ventana |

> Cada archivo tiene **varias hojas**, una por caso de uso. En draw.io son las pestañas de la parte de abajo de la pantalla.

---

## Cómo están organizados

```
docs/
├── README.md
├── ModeloDeDominio.drawio.pdf
├── ModeloDeDominio.drawio.xml
├── DSS y diagramas de comunicacion/
│   ├── Formato pdf/          un PDF por área del sistema
│   └── Formato xml/          los mismos diagramas, en draw.io
└── Registro de horas_.xlsx
```

Los casos de uso están agrupados por **área del sistema**, igual que los menús de la Estación de Trabajo: para ver cualquiera se abre un solo archivo y se cambia de hoja.

| Archivo | Hojas | Contenido | Abrir |
|---|---|---|---|
| `ModeloDeDominio` | 2 | Modelo de dominio · Diagrama de clases de diseño (DCD) | [PDF](ModeloDeDominio.drawio.pdf) · [draw.io](ModeloDeDominio.drawio.xml) |
| `Usuario` | 3 | Alta de Usuario · Consulta de Usuario · Modificar Datos de Usuario | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/Usuario.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/Usuario.drawio.xml) |
| `EventosYEdiciones` | 4 | Alta de Evento · Consulta de Evento · Alta de Edición · Consulta de Edición | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/EventosYEdiciones.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/EventosYEdiciones.drawio.xml) |
| `InstitucionYPatrocinio` | 3 | Alta de Institución · Alta de Patrocinio · Consulta de Patrocinio | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/InstitucionYPatrocinio.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/InstitucionYPatrocinio.drawio.xml) |
| `Categoria` | 1 | Alta de Categoría | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/Categoria.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/Categoria.drawio.xml) |
| `Registro` | 4 | Alta de Tipo de Registro · Consulta de Tipo de Registro · Registro a Edición · Consulta de Registro | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/Registro.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/Registro.drawio.xml) |

**15 casos de uso**, uno por cada entrada del menú de la Estación de Trabajo.

---

## Qué hay dentro de cada hoja

Cada caso de uso tiene las tres cosas juntas, una al lado de la otra:

| | Qué es |
|---|---|
| **Diagrama de secuencia del sistema (DSS)** | El actor *Administrador* y la línea de vida `:Sistema`, con las operaciones del caso de uso en orden y los bloques `OPT` / `LOOP` / `ALT` |
| **Tipos de datos (DT)** | Los atributos de cada DataType que cruza entre la lógica y la interfaz |
| **Diagramas de comunicación** | Uno por operación del DSS: qué objetos colaboran, con los mensajes numerados |

> Si al abrir una hoja en draw.io solo se ve una parte, alejar el zoom: **`Ctrl + Shift + H`** encuadra todo.

---

## Registro de horas

`Registro de horas_.xlsx` detalla las horas que dedicó cada integrante, semana a semana, separadas por actividad: reuniones y teórico, estudio, análisis, diseño, implementación, interfaz gráfica, verificación y otros.

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

Si se modifica un diagrama, hay que volver a exportar su PDF desde draw.io: *File → Export as → PDF*,
con **All pages** marcado.
