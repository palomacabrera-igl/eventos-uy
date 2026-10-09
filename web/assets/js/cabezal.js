/* ============================================================
   eventos.uy — cabezal + barra de categorías + footer compartidos
   (Parte 1, sesión simulada). Cada página incluye <div id="cabezal"></div>
   y este script. Las categorías van en una tira propia DEBAJO del cabezal;
   una página puede ocultarla poniendo data-cats="no" en ese div
   (ej. el detalle de evento). El rol se guarda en localStorage; las
   categorías enlazan a la home filtrada (index.html?cat=...).
   ============================================================ */
(function () {
  // Categorías: salen de datos.js cuando está cargado (home); si no, un fallback.
  const CATS = (typeof datos !== "undefined" && datos.categorias)
    ? datos.categorias.map(function (c) { return c.nombre; })
    : ["Tecnología", "Cultura", "Deporte", "Música", "Negocios"];

  // Rol actual (sesión simulada)
  let rol = "visitante";
  try { const g = localStorage.getItem("demoRol"); if (g) rol = g; } catch (e) {}
  let nick = null;
  try { nick = localStorage.getItem("demoNick"); } catch (e) {}

  // Categoría activa (solo tiene sentido en la home)
  let catActiva = null;
  try { catActiva = new URLSearchParams(location.search).get("cat"); } catch (e) {}

  // Botones de acceso según el rol
  function authHTML() {
    if (rol === "visitante") {
      return '<a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="login.html">Iniciar sesión</a>' +
             '<a class="btn btn-accent btn-sm" href="alta-usuario.html">Registrarse</a>';
    }
    if (rol === "asistente") {
      return '<span class="ev-who">' + (nick || "vale23") + '<small>Asistente</small></span>' +
             '<a class="btn btn-outline-secondary btn-sm" href="perfil.html">Mi perfil</a>' +
             '<a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="#" id="ev-salir">Salir</a>';
    }
    return '<a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="alta-evento.html">Alta de evento</a>' +
           '<a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="alta-edicion.html">Alta de edición</a>' +
           '<a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="alta-institucion.html">Alta de institución</a>' +
           '<span class="ev-who">' + (nick || "imm") + '<small>Organizador</small></span>' +
           '<a class="btn btn-outline-secondary btn-sm" href="perfil.html">Mi perfil</a>' +
           '<a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="#" id="ev-salir">Salir</a>';
  }

  // Chips de categoría (enlazan a la home filtrada)
  function catsHTML() {
    let h = '<a class="ev-chip' + (!catActiva ? " active" : "") + '" href="index.html">Todos</a>';
    CATS.forEach((c) => {
      const on = c === catActiva ? " active" : "";
      h += '<a class="ev-chip' + on + '" href="index.html?cat=' + encodeURIComponent(c) + '">' + c + "</a>";
    });
    return h;
  }

  const headerHTML =
    '<header class="ev-header"><div class="container">' +
      '<div class="d-flex flex-wrap align-items-center justify-content-between gap-2 py-2">' +
        '<a href="index.html" class="ev-brand">eventos<span>.uy</span></a>' +
        '<div class="d-flex align-items-center gap-3 flex-wrap">' +
          '<nav class="d-flex align-items-center gap-2 flex-wrap" aria-label="Sesión">' + authHTML() + '</nav>' +
        '</div>' +
      '</div>' +
    '</div></header>';

  const footerHTML =
    '<footer class="ev-footer"><div class="container">' +
      '<span>eventos.uy</span><span>Cartelera de eventos de Uruguay</span>' +
    '</div></footer>';

  // ¿Mostrar la tira de categorías? Se oculta en las páginas que lo pidan
  // con data-cats="no" en el <div id="cabezal"> (ej. detalle de evento).
  const slot = document.getElementById("cabezal");
  const mostrarCats = !slot || slot.dataset.cats !== "no";
  const catbarHTML = mostrarCats
    ? '<nav class="ev-catbar" aria-label="Categorías"><div class="container">' + catsHTML() + '</div></nav>'
    : "";

  // Insertar el cabezal (y la tira de categorías cuando corresponde)
  if (slot) { slot.outerHTML = headerHTML + catbarHTML; }
  else { document.body.insertAdjacentHTML("afterbegin", headerHTML + catbarHTML); }

  // Insertar el footer al final del body
  document.body.insertAdjacentHTML("beforeend", footerHTML);

  // Cerrar sesión -> vuelve a visitante y a la home
  const salir = document.getElementById("ev-salir");
  if (salir) salir.addEventListener("click", (e) => {
    e.preventDefault();
    try {
      localStorage.removeItem("demoRol");
      localStorage.removeItem("demoNick");
    } catch (err) {}
    location.href = "index.html";
  });
})();
