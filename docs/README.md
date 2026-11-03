# Documentación de análisis y diseño

Diagramas del proyecto **eventos.uy** — Programación de Aplicaciones, UTEC.

Hay documentación de las dos iteraciones. Los diagramas del **Laboratorio 2** son los que reflejan el estado actual del sistema; los del **Laboratorio 1** se conservan porque los casos de uso que esta iteración no modificó siguen valiendo.

Cada diagrama está en **dos formatos**, con el mismo contenido:

| Formato | Para qué sirve | Cómo se abre |
|---|---|---|
| **PDF** (`.pdf`) | Leerlos o imprimirlos | Con cualquier visor de PDF o con el navegador |
| **draw.io** (`.drawio.xml`) | Recorrerlos con zoom o modificarlos | Con draw.io, de escritorio o en su versión web ([app.diagrams.net](https://app.diagrams.net)): *File → Open from → Device*, o arrastrando el archivo a la ventana |

> Los diagramas grandes ocupan más de una hoja en el PDF. Para verlos enteros de una vez conviene abrir el `.drawio.xml`.

> Cada archivo tiene **varias hojas**, una por caso de uso. En draw.io son las pestañas de abajo de la pantalla. Si al abrir una hoja solo se ve una parte, **`Ctrl + Shift + H`** encuadra todo.

---

## Cómo están organizados

```
docs/
├── README.md
├── ModeloGeneral.drawio.pdf          modelo de dominio y DCD
├── ModeloGeneral.drawio.xml
├── DSS-Laboratorio2.drawio.pdf       los DSS de esta iteración
├── DSS-Laboratorio2.drawio.xml
├── DSS y diagramas de comunicacion/  los del Laboratorio 1
│   ├── Formato pdf/
│   └── Formato xml/
└── Registro de horas_.xlsx
```

## Laboratorio 2

Lo que pide la letra en «Se pide», punto 1: el modelo de dominio actualizado y los diagramas de secuencia del sistema de los casos de uso nuevos o modificados.

| Archivo | Hojas | Contenido | Abrir |
|---|---|---|---|
| `ModeloGeneral` | 2 | **ModeloDeDominio** — actualizado con la contraseña, el estado de la edición, la imagen y la fecha de alta del usuario<br>**DCD** — diagrama de clases de diseño | [PDF](ModeloGeneral.drawio.pdf) · [draw.io](ModeloGeneral.drawio.xml) |
| `DSS-Laboratorio2` | 9 | Los casos de uso nuevos y los que cambiaron | [PDF](DSS-Laboratorio2.drawio.pdf) · [draw.io](DSS-Laboratorio2.drawio.xml) |

Las nueve hojas del DSS:

| Hoja | Caso de uso |
|---|---|
| `InicioDeSesion` | Inicio de Sesión (nuevo) |
| `AceptarRechazarEdicion` | Aceptar/Rechazar Edición (nuevo, Estación de Trabajo) |
| `AltaUsuario` | Alta de Usuario (cambia: contraseña e imagen) |
| `ModificarUsuario` | Modificar Datos de Usuario (cambia: contraseña e imagen) |
| `AltaEvento` | Alta de Evento (cambia: imagen) |
| `AltaEdicion` | Alta de Edición (cambia: imagen, nace Ingresada) |
| `RegistroAEdicion` | Registro a Edición (cambia: solo ediciones Aceptadas) |
| `ConsultaUsuario` | Consulta de Usuario (cambia: perfil propio y ajeno) |
| `ConsultaEdicion` | Consulta de Edición (cambia: según quién consulta) |

### Criterios que siguen estos diagramas

- Las flechas van **siempre del actor al sistema**. No hay flechas de vuelta.
- Lo que el sistema devuelve se escribe en la misma línea de la operación, después de los dos puntos: `listarCategorias() : Set<DTCategoria>`.
- Al costado va una nota con la **Firma**, la **Pre-Condición** y la **Post-Condición** de cada operación.

## Laboratorio 1

Los 15 casos de uso de la Estación de Trabajo, agrupados por área del sistema igual que sus menús. Cada hoja trae el DSS, los tipos de datos y los diagramas de comunicación, uno por operación.

| Archivo | Hojas | Contenido | Abrir |
|---|---|---|---|
| `Usuario` | 3 | Alta · Consulta · Modificar Datos | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/Usuario.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/Usuario.drawio.xml) |
| `EventosYEdiciones` | 4 | Alta y Consulta de Evento · Alta y Consulta de Edición | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/EventosYEdiciones.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/EventosYEdiciones.drawio.xml) |
| `InstitucionYPatrocinio` | 3 | Alta de Institución · Alta y Consulta de Patrocinio | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/InstitucionYPatrocinio.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/InstitucionYPatrocinio.drawio.xml) |
| `Categoria` | 1 | Alta de Categoría | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/Categoria.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/Categoria.drawio.xml) |
| `Registro` | 4 | Alta y Consulta de Tipo de Registro · Registro a Edición · Consulta de Registro | [PDF](DSS%20y%20diagramas%20de%20comunicacion/Formato%20pdf/Registro.drawio.pdf) · [draw.io](DSS%20y%20diagramas%20de%20comunicacion/Formato%20xml/Registro.drawio.xml) |

---

## Registro de horas

`Registro de horas_.xlsx` detalla las horas que dedicó cada integrante, semana a semana, separadas por actividad: reuniones y teórico, estudio, análisis, diseño, implementación, interfaz gráfica, verificación y otros.

---

## Diseño de la solución

El **diseño de la solución web** —separación MVC, biblioteca JAR embebida, sesión, imágenes y estados de la edición— está en [`DisenioSolucionWeb.md`](DisenioSolucionWeb.md).

Las decisiones de la capa lógica y de persistencia están en el [`README.md`](../README.md) de la raíz y en los comentarios del código, sobre todo en:

| Dónde | Qué explica |
|---|---|
| `logica/EntidadBase.java` | Cómo se generan los identificadores |
| `logica/Usuario.java` | La estrategia de herencia `SINGLE_TABLE` y su precio |
| `logica/Categoria.java` | La jerarquía de categorías (auto-asociación padre/hijas) |
| `logica/ManejadorUsuario.java` | Por qué las búsquedas usan JPQL y no `em.find()` |
| `logica/ManejadorEvento.java` | Por qué `actualizar()` no usa `merge()` |
| `persistencia/Persistencia.java` | Por qué el `EntityManager` es uno por proceso |
| `logica/DatosDePrueba.java` | Por qué el orden de carga importa con JPA |

---

## Nota sobre el estado de los diagramas

Los diagramas del **Laboratorio 1** corresponden a su etapa de análisis y diseño. Durante la implementación algunas firmas cambiaron —principalmente al unificar el manejo de errores en `ReglaNegocioException`— y esas diferencias no se reflejaron en todos.

El **DCD** también corresponde al Laboratorio 1: muestra el diseño con las operaciones que guardaban estado entre llamadas. Esta iteración agregó operaciones sin estado, porque el sitio recibe cada pedido por separado y no puede apoyarse en lo que pasó antes; eso está explicado en el diseño de la solución web.

**El código es la referencia para las firmas exactas.** Los diagramas documentan la estructura y las colaboraciones entre objetos, que sí se mantienen.

Si se modifica un diagrama y se quiere su PDF: *File → Export as → PDF*, con **All pages** marcado.
