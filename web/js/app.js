const lista = document.querySelector("#lista-eventos");
const categoriasContenedor = document.querySelector("#categorias");
const buscador = document.querySelector("#buscador");
const mensaje = document.querySelector("#mensaje");

let categoriaSeleccionada = null;
let textoBusqueda = "";

function rutaImagen(ruta) {
    return ruta.replace(/^web\//, "");
}

function obtenerCategorias(evento) {
    return evento.categoriaIds
        .map(id => datos.categorias.find(categoria => categoria.id === id)?.nombre)
        .filter(Boolean);
}

function renderizarCategorias() {
    categoriasContenedor.innerHTML = `
    <button class="btn btn-sm btn-outline-primary category-btn ${categoriaSeleccionada === null ? "active" : ""}"
      data-id="">Todas</button>
    ${datos.categorias.map(categoria => `
      <button class="btn btn-sm btn-outline-primary category-btn
        ${categoriaSeleccionada === categoria.id ? "active" : ""}"
        data-id="${categoria.id}">
        ${categoria.nombre}
      </button>
    `).join("")}
  `;

    document.querySelectorAll(".category-btn").forEach(boton => {
        boton.addEventListener("click", () => {
            categoriaSeleccionada = boton.dataset.id || null;
            renderizarCategorias();
            renderizarEventos();
        });
    });
}

function renderizarEventos() {
    const eventosFiltrados = datos.eventos.filter(evento => {
        const coincideCategoria =
            !categoriaSeleccionada ||
            evento.categoriaIds.includes(categoriaSeleccionada);

        const texto = `${evento.nombre} ${evento.descripcion}`.toLowerCase();
        const coincideBusqueda =
            texto.includes(textoBusqueda.toLowerCase());

        return coincideCategoria && coincideBusqueda;
    });

    mensaje.classList.toggle("d-none", eventosFiltrados.length > 0);
    mensaje.textContent = "No se encontraron eventos con esos criterios.";

    lista.innerHTML = eventosFiltrados.map(evento => {
        const categorias = obtenerCategorias(evento);

        return `
      <article class="col-12 col-md-6">
        <div class="card event-card h-100 border-0 shadow-sm">
          <img
            src="${rutaImagen(evento.imagen)}"
            class="card-img-top event-image"
            alt="Imagen de ${evento.nombre}"
          >

          <div class="card-body d-flex flex-column">
            <div class="mb-2">
              ${categorias.map(categoria =>
            `<span class="badge text-bg-primary me-1">${categoria}</span>`
        ).join("")}
            </div>

            <h2 class="h4">${evento.nombre}</h2>

            <p class="text-secondary">
              ${evento.descripcion}
            </p>

            <button class="btn btn-outline-primary mt-auto">
                Ver evento
            </button>
          </div>
        </div>
      </article>
    `;
    }).join("");
}

buscador.addEventListener("input", event => {
    textoBusqueda = event.target.value;
    renderizarEventos();
});

renderizarCategorias();
renderizarEventos();