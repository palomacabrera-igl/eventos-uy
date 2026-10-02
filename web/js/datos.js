/*
 * Datos estáticos de la Parte 1.
 *
 * No hay conexión con Java ni con la base de datos todavía. app.js debe leer
 * este objeto para dibujar las pantallas y simular los distintos recorridos.
 * En la Parte 2 estos datos pasarán a llegar desde los servlets.
 */

const datos = {
  categorias: [
    { id: "1", nombre: "Ingeniería" },
    { id: "2", nombre: "Charlas" },
    { id: "3", nombre: "Talleres" }
  ],

  instituciones: [
    {
      id: "utec",
      nombre: "UTEC",
      descripcion: "Universidad Tecnológica del Uruguay.",
      sitioWeb: "https://utec.edu.uy",
      imagen: "web/public/images/instituciones/avatarInstitucion.png"
    }
  ],

  usuarios: [
    {
      nickname: "pfernandez",
      rol: "asistente",
      nombre: "Paloma",
      apellido: "Fernández",
      correo: "paloma@example.com",
      password: "Paloma2026!",
      fechaNacimiento: "2000-05-14",
      fechaAlta: "2026-01-10",
      institucionId: null,
      imagen: "web/public/images/usuarios/avatarFemenino.png"
    },
    {
      nickname: "utec",
      rol: "organizador",
      nombre: "UTEC Eventos",
      correo: "eventos@utec.edu.uy",
      password: "Utec2026!",
      descripcion: "Organizador institucional de eventos académicos de UTEC.",
      sitioWeb: "https://utec.edu.uy",
      fechaAlta: "2025-01-05",
      imagen: "web/public/images/usuarios/avatarMasculino.png"
    }
  ],

  eventos: [
    {
      id: "2",
      nombre: "JIAP",
      sigla: "JIAP",
      descripcion: "Jornadas de Ingeniería",
      fechaAlta: "2025-01-10",
      categoriaIds: ["1"],
      imagen: "web/public/images/eventos/jiap/eventoIng.png"
    },
    {
      id: "1",
      nombre: "Feria del libro",
      sigla: "FL",
      descripcion: "Semana del libro",
      fechaAlta: "2017-01-01",
      categoriaIds: ["2"],
      imagen: "web/public/images/eventos/FeriaDelLibro/eventoLibro.png"
    }
  ],

  ediciones: [
    {
      id: "2",
      eventoId: "2",
      nombre: "JIAP 2026",
      sigla: "JIAP26",
      ciudad: "Montevideo",
      pais: "Uruguay",
      fechaInicio: "2026-10-01",
      fechaFin: "2026-10-03",
      fechaAlta: "2026-01-15",
      estado: "aceptada",
      organizadorNickname: "utec",
      imagen: "web/public/images/eventos/JIAP/ediciones/edicionIng2026.png"
    },
    {
      id: "3",
      eventoId: "2",
      nombre: "JIAP 2027",
      sigla: "JIAP27",
      ciudad: "Maldonado",
      pais: "Uruguay",
      fechaInicio: "2027-10-02",
      fechaFin: "2027-10-04",
      fechaAlta: "2026-09-25",
      estado: "ingresada",
      organizadorNickname: "utec",
      imagen: "web/public/images/eventos/JIAP/ediciones/edicionIng2027.png"
    },
    {
      id: "1",
      eventoId: "2",
      nombre: "JIAP 2025",
      sigla: "JIAP25",
      ciudad: "Montevideo",
      pais: "Uruguay",
      fechaInicio: "2025-10-01",
      fechaFin: "2025-10-03",
      fechaAlta: "2025-01-15",
      estado: "rechazada",
      organizadorNickname: "utec",
      imagen: "web/public/images/eventos/JIAP/ediciones/edicionIng2025.png"
    },
    {
      id: "4",
      eventoId: "1",
      nombre: "Libro1",
      sigla: "L1",
      ciudad: "San José de Mayo",
      pais: "Uruguay",
      fechaInicio: "2026-10-01",
      fechaFin: "2026-10-07",
      fechaAlta: "2026-01-15",
      estado: "rechazada",
      organizadorNickname: "utec",
      imagen: "web/public/images/eventos/FeriaDelLibro/ediciones/edicionLib2026.png"
    },
  ],

  tiposRegistro: [
    { id: "1", edicionId: "2", nombre: "General", descripcion: "Acceso a todas las charlas.", costo: 1500, cupo: 200, cuposOcupados: 1 },
    { id: "2", edicionId: "2", nombre: "Estudiante", descripcion: "Acceso con tarifa estudiantil.", costo: 800, cupo: 80, cuposOcupados: 0 },
    { id: "3", edicionId: "3", nombre: "Taller intensivo", descripcion: "Incluye talleres prácticos con cupo limitado.", costo: 2200, cupo: 30, cuposOcupados: 30 },
    { id: "4", edicionId: "4", nombre: "General", descripcion: "Acceso a charlas y talleres de la semana.", costo: 1200, cupo: 150, cuposOcupados: 0 },
      ],

  patrocinios: [
    {
      id: "1",
      edicionId: "2",
      institucionId: "utec",
      nivel: "oro",
      aporte: 20000,
      tipoRegistroId: "2",
      cantidadRegistrosGratuitos: 10,
      usosActuales: 0,
      codigo: "UTEC-JIAP-26",
      fechaAlta: "2026-02-01"
    },
    {
      id: "2",
      edicionId: "3",
      institucionId: "utec",
      nivel: "plata",
      aporte: 10000,
      tipoRegistroId: "3",
      cantidadRegistrosGratuitos: 4,
      usosActuales: 0,
      codigo: "UTEC-JIAP-25",
      fechaAlta: "2025-02-10"
    },
    {
      id: "3",
      edicionId: "4",
      institucionId: "utec",
      nivel: "bronce",
      aporte: 8000,
      tipoRegistroId: "4",
      cantidadRegistrosGratuitos: 2,
      usosActuales: 0,
      codigo: "UTEC-L1",
      fechaAlta: "2025-05-01"
    }
  ],

  registros: [
    {
      id: "1",
      asistenteNickname: "pfernandez",
      edicionId: "2",
      tipoRegistroId: "1",
      fechaRegistro: "2026-09-01",
      costo: 1500,
      patrocinioId: 1
    }
  ],
};
