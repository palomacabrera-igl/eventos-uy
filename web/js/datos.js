/*
 * Datos estáticos de la Parte 1.
 *
 * No hay conexión con Java ni con la base de datos todavía. app.js debe leer
 * este objeto para dibujar las pantallas y simular los distintos recorridos.
 * En la Parte 2 estos datos pasarán a llegar desde los servlets.
 */

const datos = {
  categorias: [
    { id: "tec", nombre: "Tecnología" },
    { id: "cul", nombre: "Cultura" },
    { id: "dep", nombre: "Deporte" },
    { id: "mus", nombre: "Música" },
    { id: "neg", nombre: "Negocios" }
  ],

  instituciones: [
    {
      id: "utec",
      nombre: "UTEC",
      descripcion: "Universidad Tecnológica del Uruguay.",
      sitioWeb: "https://utec.edu.uy",
      imagen: "public/images/instituciones/avatarInstitucion.png"
    },
    {
      id: "udelar",
      nombre: "Universidad de la República",
      descripcion: "Universidad pública del Uruguay.",
      sitioWeb: "https://udelar.edu.uy",
      imagen: "public/images/instituciones/avatarInstitucion.png"
    },
    {
      id: "ancap",
      nombre: "ANCAP",
      descripcion: "Administración Nacional de Combustibles, Alcohol y Portland.",
      sitioWeb: "https://ancap.com.uy",
      imagen: "public/images/instituciones/avatarInstitucion.png"
    }
  ],

  usuarios: [
    {
      nickname: "vale23",
      rol: "asistente",
      nombre: "Valentina",
      apellido: "Díaz",
      correo: "valentina@mail.com",
      password: "Vale2026!",
      fechaNacimiento: "2001-03-22",
      fechaAlta: "2026-01-10",
      institucionId: "utec",
      imagen: "public/images/usuarios/avatarFemenino.png"
    },
    {
      nickname: "jperez",
      rol: "asistente",
      nombre: "Juan",
      apellido: "Pérez",
      correo: "jperez@mail.com",
      password: "Juan2026!",
      fechaNacimiento: "1998-11-05",
      fechaAlta: "2026-02-18",
      institucionId: null,
      imagen: "public/images/usuarios/avatarMasculino.png"
    },
    {
      nickname: "lgomez",
      rol: "asistente",
      nombre: "Lucía",
      apellido: "Gómez",
      correo: "lucia@mail.com",
      password: "Lucia2026!",
      fechaNacimiento: "1999-07-30",
      fechaAlta: "2026-03-05",
      institucionId: "utec",
      imagen: "public/images/usuarios/avatarFemenino.png"
    },
    {
      nickname: "imm",
      rol: "organizador",
      nombre: "Intendencia de Montevideo",
      correo: "eventos@imm.gub.uy",
      password: "Imm2026!",
      descripcion: "Organismo que coordina los eventos culturales y deportivos de la capital.",
      sitioWeb: "https://montevideo.gub.uy",
      fechaAlta: "2025-01-05",
      imagen: "public/images/instituciones/avatarInstitucion.png"
    },
    {
      nickname: "antel",
      rol: "organizador",
      nombre: "ANTEL",
      correo: "eventos@antel.com.uy",
      password: "Antel2026!",
      descripcion: "Empresa de telecomunicaciones del Estado uruguayo.",
      sitioWeb: "",
      fechaAlta: "2025-06-30",
      imagen: "public/images/instituciones/avatarInstitucion.png"
    }
  ],
  
  eventos: [
    {
      id: "2",
      nombre: "JIAP",
      sigla: "JIAP",
      descripcion: "Jornadas de Ingeniería",
      fechaAlta: "2025-01-10",
      categoriaIds: ["tec"],
      imagen: "public/images/eventos/JIAP/eventoIng.png"
    },
    {
      id: "1",
      nombre: "Feria del libro",
      sigla: "FL",
      descripcion: "Semana del libro",
      fechaAlta: "2017-01-01",
      categoriaIds: ["cul"],
      imagen: "public/images/eventos/FeriaDelLibro/eventoLibro.png"
    },
    {
      id: "3",
      nombre: "Maratón de Montevideo",
      sigla: "MARATON",
      descripcion: "Competencia deportiva anual en la capital.",
      fechaAlta: "2024-03-12",
      categoriaIds: ["dep"]
    },
    {
      id: "4",
      nombre: "Montevideo Rock",
      sigla: "MVDROCK",
      descripcion: "Festival con artistas nacionales e internacionales.",
      fechaAlta: "2023-08-20",
      categoriaIds: ["mus"]
    },
    {
      id: "5",
      nombre: "Expo Negocios UY",
      sigla: "EXPONEG",
      descripcion: "Encuentro de empresas y emprendedores del país.",
      fechaAlta: "2025-02-05",
      categoriaIds: ["neg", "tec"]
    },
    {
      id: "6",
      nombre: "Montevideo Comics",
      sigla: "MVDCOM",
      descripcion: "Convención de historietas, cine y cultura geek.",
      fechaAlta: "2022-06-30",
      categoriaIds: ["cul"]
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
      organizadorNickname: "imm",
      imagen: "public/images/eventos/JIAP/ediciones/edicionIng2026.png"
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
      organizadorNickname: "imm",
      imagen: "public/images/eventos/JIAP/ediciones/edicionIng2027.png"
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
      organizadorNickname: "imm",
      imagen: "public/images/eventos/JIAP/ediciones/edicionIng2025.png"
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
      estado: "aceptada",
      organizadorNickname: "antel",
      imagen: "public/images/eventos/FeriaDelLibro/ediciones/edicionLib2026.png"
    },
    {
      id: "5",
      eventoId: "3",
      nombre: "Maratón 2026",
      sigla: "MVD26",
      ciudad: "Montevideo",
      pais: "Uruguay",
      fechaInicio: "2026-11-15",
      fechaFin: "2026-11-15",
      fechaAlta: "2026-03-01",
      estado: "aceptada",
      organizadorNickname: "imm"
    },
    {
      id: "6",
      eventoId: "3",
      nombre: "Maratón 2025",
      sigla: "MVD25",
      ciudad: "Montevideo",
      pais: "Uruguay",
      fechaInicio: "2025-11-16",
      fechaFin: "2025-11-16",
      fechaAlta: "2025-02-20",
      estado: "aceptada",
      organizadorNickname: "imm"
    },
    {
      id: "7",
      eventoId: "4",
      nombre: "Montevideo Rock 2026",
      sigla: "ROCK26",
      ciudad: "Montevideo",
      pais: "Uruguay",
      fechaInicio: "2026-12-05",
      fechaFin: "2026-12-06",
      fechaAlta: "2026-04-18",
      estado: "aceptada",
      organizadorNickname: "antel"
    },
    {
      id: "8",
      eventoId: "5",
      nombre: "Expo Negocios 2026",
      sigla: "EXPO26",
      ciudad: "Punta del Este",
      pais: "Uruguay",
      fechaInicio: "2026-09-18",
      fechaFin: "2026-09-20",
      fechaAlta: "2026-05-30",
      estado: "ingresada",
      organizadorNickname: "antel"
    },
    {
      id: "9",
      eventoId: "6",
      nombre: "Comics 2026",
      sigla: "COM26",
      ciudad: "Montevideo",
      pais: "Uruguay",
      fechaInicio: "2026-10-03",
      fechaFin: "2026-10-04",
      fechaAlta: "2026-01-22",
      estado: "aceptada",
      organizadorNickname: "imm"
    }
  ],

  tiposRegistro: [
    { id: "1", edicionId: "2", nombre: "General", descripcion: "Acceso a todas las charlas.", costo: 1500, cupo: 200, cuposOcupados: 1 },
    { id: "2", edicionId: "2", nombre: "Estudiante", descripcion: "Acceso con tarifa estudiantil.", costo: 800, cupo: 80, cuposOcupados: 80 },
    { id: "3", edicionId: "3", nombre: "Taller intensivo", descripcion: "Incluye talleres prácticos con cupo limitado.", costo: 2200, cupo: 30, cuposOcupados: 30 },
    { id: "4", edicionId: "4", nombre: "General", descripcion: "Acceso a charlas y talleres de la semana.", costo: 1200, cupo: 150, cuposOcupados: 0 },
    { id: "5", edicionId: "5", nombre: "Corredor 10K", descripcion: "Recorrido de 10 kilómetros por la rambla.", costo: 1200, cupo: 500, cuposOcupados: 1 },
    { id: "6", edicionId: "5", nombre: "Corredor 21K", descripcion: "Media maratón.", costo: 1800, cupo: 300, cuposOcupados: 0 },
    { id: "7", edicionId: "7", nombre: "Entrada general", descripcion: "Acceso a los dos días del festival.", costo: 2500, cupo: 1000, cuposOcupados: 1 },
    { id: "8", edicionId: "9", nombre: "Entrada general", descripcion: "Acceso a la convención.", costo: 900, cupo: 400, cuposOcupados: 0 }
  ],

  patrocinios: [
    {
      id: "1",
      edicionId: "2",
      institucionId: "utec",
      nivel: "oro",
      aporte: 20000,
      tipoRegistroId: "1",
      cantidadRegistrosGratuitos: 1,
      usosActuales: 1,
      codigo: "UTEC-JIAP-26",
      fechaAlta: "2026-02-01"
    },
    {
      id: "2",
      edicionId: "3",
      institucionId: "utec",
      nivel: "plata",
      aporte: 25000,
      tipoRegistroId: "3",
      cantidadRegistrosGratuitos: 2,
      usosActuales: 0,
      codigo: "UTEC-JIAP-27",
      fechaAlta: "2025-02-10"
    },
    {
      id: "3",
      edicionId: "4",
      institucionId: "utec",
      nivel: "bronce",
      aporte: 8000,
      tipoRegistroId: "4",
      cantidadRegistrosGratuitos: 1,
      usosActuales: 0,
      codigo: "UTEC-L1",
      fechaAlta: "2025-05-01"
    },
    {
      id: "4",
      edicionId: "5",
      institucionId: "ancap",
      nivel: "oro",
      aporte: 50000,
      tipoRegistroId: "5",
      cantidadRegistrosGratuitos: 8,
      usosActuales: 0,
      codigo: "ANCAP-MVD-26",
      fechaAlta: "2026-04-10"
    },
    {
      id: "5",
      edicionId: "7",
      institucionId: "udelar",
      nivel: "plata",
      aporte: 30000,
      tipoRegistroId: "7",
      cantidadRegistrosGratuitos: 2,
      usosActuales: 0,
      codigo: "UDELAR-ROCK-26",
      fechaAlta: "2026-05-12"
    }
  ],

  registros: [
    {
      id: "1",
      asistenteNickname: "vale23",
      edicionId: "2",
      tipoRegistroId: "1",
      fechaRegistro: "2026-09-01",
      costo: 0,
      patrocinioId: 1
    },
    {
      id: "2",
      asistenteNickname: "jperez",
      edicionId: "5",
      tipoRegistroId: "5",
      fechaRegistro: "2026-05-02",
      costo: 1200,
      patrocinioId: null
    },
    {
      id: "3",
      asistenteNickname: "vale23",
      edicionId: "7",
      tipoRegistroId: "7",
      fechaRegistro: "2026-06-01",
      costo: 2500,
      patrocinioId: null
    }
  ],
};
