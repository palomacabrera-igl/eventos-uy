package servicios;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlSeeAlso;

import logica.DTAsistente;
import logica.DTCategoria;
import logica.DTEdicionCompleto;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTImagen;
import logica.DTInstitucion;
import logica.DTOrganizador;
import logica.DTPatrocinio;
import logica.DTRegistro;
import logica.DTTipoRegistro;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.OrdenBusqueda;
import logica.ReglaNegocioException;
import logica.TipoUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * La fachada de servicios web del Servidor Central: lo unico que el sitio puede
 * pedirle a la logica desde otra computadora. Cada metodo publico es una operacion
 * del contrato (WSDL); JAX-WS lee las anotaciones y atiende los pedidos SOAP.
 * La publica Main con Endpoint.publish.
 *
 * Reglas de la clase:
 * - Es una fachada delgada: cada metodo llama al controlador (que se pide a la
 *   Fabrica, nunca con new Sistema()) y devuelve lo que contesta, sin reglas de
 *   negocio ni JPA.
 * - Solo operaciones sin estado (las del Laboratorio 1 guardan datos en campos de
 *   un unico Sistema y con varios clientes se pisarian) y con nombre unico (SOAP
 *   no admite sobrecargas).
 * - Todos los parametros llevan @WebParam(name = "..."); si no, el contrato los
 *   llama arg0, arg1...
 * - Las colecciones van como List y no Set (el cliente generado usa List), y solo
 *   viajan DT, tipos simples, enumerados y byte[].
 * - Los errores de negocio se declaran en el throws: ReglaNegocioException viaja
 *   como SOAP Fault con su mensaje.
 *
 * @XmlSeeAlso: varias operaciones declaran DTUsuario pero viaja un DTAsistente o
 * un DTOrganizador; JAXB solo conoce los tipos de las firmas y hay que indicarle
 * las subclases. Una operacion nueva va en la seccion de su tema, con su prueba
 * en PublicadorTest.
 */
@WebService
@XmlSeeAlso({DTAsistente.class, DTOrganizador.class})
public class Publicador {

    private final IControladorSistema controlador;

    /** Lo usa JAX-WS y el Main: el controlador real, con su unidad de trabajo. */
    public Publicador() {
        this(Fabrica.getInstancia().getControladorSistema());
    }

    /** Para las pruebas: permite darle un controlador de mentira, sin base de datos. */
    public Publicador(IControladorSistema controlador) {
        this.controlador = controlador;
    }

    // =====================================================================
    //  Sesion y usuarios (CU 1 a 5)
    // =====================================================================

    @WebMethod
    public DTUsuario iniciarSesion(@WebParam(name = "identificador") String identificador,
                                   @WebParam(name = "password") String password)
            throws ReglaNegocioException {
        return controlador.iniciarSesion(identificador, password);
    }

    @WebMethod
    public void altaUsuario(@WebParam(name = "datos") DTUsuario datos,
                            @WebParam(name = "tipo") TipoUsuario tipo,
                            @WebParam(name = "password") String password,
                            @WebParam(name = "confirmacion") String confirmacion,
                            @WebParam(name = "nombreInstitucion") String nombreInstitucion,
                            @WebParam(name = "imagen") DTImagen imagen)
            throws ReglaNegocioException {
        controlador.altaUsuario(datos, tipo, password, confirmacion, nombreInstitucion, imagen);
    }

    @WebMethod
    public DTUsuario consultarUsuario(@WebParam(name = "nickname") String nickname,
                                      @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.consultarUsuario(nickname, solicitante);
    }

    @WebMethod
    public List<DTUsuario> listarUsuarios() {
        return aLista(controlador.listarUsuarios());
    }

    @WebMethod
    public List<DTEdicionEvento> listarEdicionesDeOrganizador(
            @WebParam(name = "nickname") String nickname,
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return aLista(controlador.listarEdicionesDeOrganizador(nickname, solicitante));
    }

    @WebMethod
    public List<DTRegistro> listarRegistrosDeAsistente(
            @WebParam(name = "nickname") String nickname,
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return aLista(controlador.listarRegistrosDeAsistente(nickname, solicitante));
    }

    @WebMethod
    public void modificarUsuario(@WebParam(name = "solicitante") String solicitante,
                                 @WebParam(name = "datos") DTUsuario datos,
                                 @WebParam(name = "nuevaPassword") String nuevaPassword,
                                 @WebParam(name = "confirmacion") String confirmacion,
                                 @WebParam(name = "imagen") DTImagen imagen)
            throws ReglaNegocioException {
        controlador.modificarUsuario(solicitante, datos, nuevaPassword, confirmacion, imagen);
    }

    /**
     * Las imagenes se piden aparte y nunca dentro de un listado: un byte[] viaja
     * en Base64 dentro del XML y pesa, y listar 20 eventos mandaria 20 imagenes.
     * Devuelve null si el usuario no tiene.
     */
    @WebMethod
    public DTImagen obtenerImagenUsuario(@WebParam(name = "nickname") String nickname)
            throws ReglaNegocioException {
        return controlador.obtenerImagenUsuario(nickname);
    }

    @WebMethod
    public List<String> listarNombresInstituciones() {
        return aLista(controlador.listarNombresInstituciones());
    }

    // =====================================================================
    //  Eventos y ediciones (CU 6 a 9)
    // =====================================================================

    /**
     * Con categoria null o vacia devuelve todos los eventos. Es la unica version
     * publicada: la de sin parametros del Laboratorio 1 hace lo mismo que
     * llamar a esta con null, y SOAP no admite dos operaciones con el mismo nombre.
     */
    @WebMethod
    public List<DTEvento> listarEventos(@WebParam(name = "categoria") String categoria) {
        return aLista(controlador.listarEventos(categoria));
    }

    @WebMethod
    public DTEvento consultarEvento(@WebParam(name = "nombre") String nombre)
            throws ReglaNegocioException {
        return controlador.consultarEvento(nombre);
    }

    @WebMethod
    public List<DTEdicionEvento> listarEdicionesAceptadas(
            @WebParam(name = "nombreEvento") String nombreEvento)
            throws ReglaNegocioException {
        return aLista(controlador.listarEdicionesAceptadas(nombreEvento));
    }

    @WebMethod
    public List<DTCategoria> listarCategorias() {
        return aLista(controlador.listarCategorias());
    }

    @WebMethod
    public void altaEvento(@WebParam(name = "solicitante") String solicitante,
                           @WebParam(name = "nombre") String nombre,
                           @WebParam(name = "descripcion") String descripcion,
                           @WebParam(name = "sigla") String sigla,
                           @WebParam(name = "nombresCategorias") List<String> nombresCategorias,
                           @WebParam(name = "imagen") DTImagen imagen)
            throws ReglaNegocioException {
        controlador.altaEvento(solicitante, nombre, descripcion, sigla, nombresCategorias, imagen);
    }

    @WebMethod
    public void altaEdicion(@WebParam(name = "solicitante") String solicitante,
                            @WebParam(name = "nombreEvento") String nombreEvento,
                            @WebParam(name = "datos") DTEdicionEvento datos,
                            @WebParam(name = "imagen") DTImagen imagen)
            throws ReglaNegocioException {
        controlador.altaEdicion(solicitante, nombreEvento, datos, imagen);
    }

    @WebMethod
    public DTEdicionCompleto consultarEdicion(@WebParam(name = "nombreEdicion") String nombreEdicion,
                                              @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.consultarEdicion(nombreEdicion, solicitante);
    }

    @WebMethod
    public DTImagen obtenerImagenEvento(@WebParam(name = "nombreEvento") String nombreEvento)
            throws ReglaNegocioException {
        return controlador.obtenerImagenEvento(nombreEvento);
    }

    @WebMethod
    public DTImagen obtenerImagenEdicion(@WebParam(name = "nombreEdicion") String nombreEdicion)
            throws ReglaNegocioException {
        return controlador.obtenerImagenEdicion(nombreEdicion);
    }

    // =====================================================================
    //  Tipos de registro e instituciones (CU 10, 11 y 14)
    // =====================================================================

    @WebMethod
    public void altaTipoRegistro(@WebParam(name = "solicitante") String solicitante,
                                 @WebParam(name = "nombreEdicion") String nombreEdicion,
                                 @WebParam(name = "nombre") String nombre,
                                 @WebParam(name = "descripcion") String descripcion,
                                 @WebParam(name = "costo") double costo,
                                 @WebParam(name = "cupo") int cupo)
            throws ReglaNegocioException {
        controlador.altaTipoRegistro(solicitante, nombreEdicion, nombre, descripcion, costo, cupo);
    }

    @WebMethod
    public List<DTTipoRegistro> listarTiposDeRegistro(
            @WebParam(name = "nombreEdicion") String nombreEdicion)
            throws ReglaNegocioException {
        return aLista(controlador.listarTiposDeRegistro(nombreEdicion));
    }

    @WebMethod
    public DTTipoRegistro consultarTipoRegistro(
            @WebParam(name = "nombreEdicion") String nombreEdicion,
            @WebParam(name = "nombreTipo") String nombreTipo,
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.consultarTipoRegistro(nombreEdicion, nombreTipo, solicitante);
    }

    @WebMethod
    public void altaInstitucion(@WebParam(name = "solicitante") String solicitante,
                                @WebParam(name = "nombre") String nombre,
                                @WebParam(name = "descripcion") String descripcion,
                                @WebParam(name = "sitioWeb") String sitioWeb,
                                @WebParam(name = "imagen") DTImagen imagen)
            throws ReglaNegocioException {
        controlador.altaInstitucion(solicitante, nombre, descripcion, sitioWeb, imagen);
    }

    @WebMethod
    public DTInstitucion consultarInstitucion(@WebParam(name = "nombre") String nombre)
            throws ReglaNegocioException {
        return controlador.consultarInstitucion(nombre);
    }

    @WebMethod
    public DTImagen obtenerImagenInstitucion(@WebParam(name = "nombre") String nombre)
            throws ReglaNegocioException {
        return controlador.obtenerImagenInstitucion(nombre);
    }

    // =====================================================================
    //  Registros y patrocinios (CU 12, 13, 15 y 16)
    // =====================================================================

    /**
     * Es la version con codigo de patrocinio, que acepta null cuando el
     * asistente paga: cubre los dos casos con una sola operacion.
     */
    @WebMethod
    public void altaRegistro(@WebParam(name = "nickname") String nickname,
                             @WebParam(name = "nombreEdicion") String nombreEdicion,
                             @WebParam(name = "nombreTipo") String nombreTipo,
                             @WebParam(name = "codigoPatrocinio") String codigoPatrocinio)
            throws ReglaNegocioException {
        controlador.altaRegistro(nickname, nombreEdicion, nombreTipo, codigoPatrocinio);
    }

    @WebMethod
    public List<DTRegistro> listarRegistrosDeEdicion(
            @WebParam(name = "nombreEdicion") String nombreEdicion,
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return aLista(controlador.listarRegistrosDeEdicion(nombreEdicion, solicitante));
    }

    @WebMethod
    public DTRegistro consultarRegistro(@WebParam(name = "nombreEdicion") String nombreEdicion,
                                        @WebParam(name = "nickname") String nickname,
                                        @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.consultarRegistro(nombreEdicion, nickname, solicitante);
    }

    @WebMethod
    public void altaPatrocinio(@WebParam(name = "solicitante") String solicitante,
                               @WebParam(name = "nombreEdicion") String nombreEdicion,
                               @WebParam(name = "datos") DTPatrocinio datos)
            throws ReglaNegocioException {
        controlador.altaPatrocinio(solicitante, nombreEdicion, datos);
    }

    @WebMethod
    public List<DTPatrocinio> listarPatrociniosDeEdicion(
            @WebParam(name = "nombreEdicion") String nombreEdicion,
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return aLista(controlador.listarPatrociniosDeEdicion(nombreEdicion, solicitante));
    }

    @WebMethod
    public DTPatrocinio consultarPatrocinio(
            @WebParam(name = "nombreEdicion") String nombreEdicion,
            @WebParam(name = "nombreInstitucion") String nombreInstitucion,
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.consultarPatrocinio(nombreEdicion, nombreInstitucion, solicitante);
    }

    // =====================================================================
    //  Seguidores, finalizar evento y archivar edicion
    //  (esqueleto del Laboratorio 3: la logica la completa cada rol en Sistema)
    // =====================================================================

    @WebMethod
    public void seguirUsuario(@WebParam(name = "solicitante") String solicitante,
                              @WebParam(name = "nickname") String nickname)
            throws ReglaNegocioException {
        controlador.seguirUsuario(solicitante, nickname);
    }

    @WebMethod
    public void dejarDeSeguirUsuario(@WebParam(name = "solicitante") String solicitante,
                                     @WebParam(name = "nickname") String nickname)
            throws ReglaNegocioException {
        controlador.dejarDeSeguirUsuario(solicitante, nickname);
    }

    @WebMethod
    public List<DTUsuario> listarSeguidores(@WebParam(name = "nickname") String nickname)
            throws ReglaNegocioException {
        return controlador.listarSeguidores(nickname);
    }

    @WebMethod
    public List<DTUsuario> listarSeguidos(@WebParam(name = "nickname") String nickname)
            throws ReglaNegocioException {
        return controlador.listarSeguidos(nickname);
    }

    @WebMethod
    public void finalizarEvento(@WebParam(name = "solicitante") String solicitante,
                                @WebParam(name = "nombreEvento") String nombreEvento)
            throws ReglaNegocioException {
        controlador.finalizarEvento(solicitante, nombreEvento);
    }

    @WebMethod
    public List<DTEvento> listarEventosFinalizables(@WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.listarEventosFinalizables(solicitante);
    }

    @WebMethod
    public void archivarEdicion(@WebParam(name = "solicitante") String solicitante,
                                @WebParam(name = "nombreEdicion") String nombreEdicion)
            throws ReglaNegocioException {
        controlador.archivarEdicion(solicitante, nombreEdicion);
    }

    @WebMethod
    public List<DTEdicionEvento> listarEdicionesArchivables(
            @WebParam(name = "solicitante") String solicitante)
            throws ReglaNegocioException {
        return controlador.listarEdicionesArchivables(solicitante);
    }

    // =====================================================================
    //  Video, busqueda y sitio para celular
    //  (esqueleto del Laboratorio 3: la logica la completa cada rol en Sistema)
    //
    //  El video no tiene operaciones: viaja en el campo urlVideo de los DT de
    //  edicion. El sitio para celular usa las operaciones que ya estan.
    // =====================================================================

    @WebMethod
    public List<DTEvento> buscarEventos(@WebParam(name = "texto") String texto,
                                        @WebParam(name = "orden") OrdenBusqueda orden) {
        return controlador.buscarEventos(texto, orden);
    }

    @WebMethod
    public List<DTEdicionEvento> buscarEdiciones(@WebParam(name = "texto") String texto,
                                                 @WebParam(name = "orden") OrdenBusqueda orden) {
        return controlador.buscarEdiciones(texto, orden);
    }

    // =====================================================================
    //  Asistencia, constancia, visitas y validacion
    //  (esqueleto del Laboratorio 3: la logica la completa cada rol en Sistema)
    //
    //  listarEventosMasVisitados lo usa solo la Estacion de Trabajo, que llama al
    //  controlador directamente: no se publica.
    // =====================================================================

    @WebMethod
    public void registrarAsistencia(@WebParam(name = "solicitante") String solicitante,
                                    @WebParam(name = "nombreEdicion") String nombreEdicion)
            throws ReglaNegocioException {
        controlador.registrarAsistencia(solicitante, nombreEdicion);
    }

    /** El PDF viaja como byte[], en Base64 dentro del XML, igual que las imagenes. */
    @WebMethod
    public byte[] obtenerConstanciaAsistencia(@WebParam(name = "solicitante") String solicitante,
                                              @WebParam(name = "nombreEdicion") String nombreEdicion)
            throws ReglaNegocioException {
        return controlador.obtenerConstanciaAsistencia(solicitante, nombreEdicion);
    }

    @WebMethod
    public void registrarVisita(@WebParam(name = "nombreEvento") String nombreEvento)
            throws ReglaNegocioException {
        controlador.registrarVisita(nombreEvento);
    }

    @WebMethod
    public boolean existeNickname(@WebParam(name = "nickname") String nickname) {
        return controlador.existeNickname(nickname);
    }

    @WebMethod
    public boolean existeCorreo(@WebParam(name = "correo") String correo) {
        return controlador.existeCorreo(correo);
    }

    /**
     * Convierte el Set que devuelve el controlador en la List que viaja por la
     * red. Es privado a proposito: JAX-WS solo publica los metodos publicos.
     */
    private static <T> List<T> aLista(Set<T> conjunto) {
        return new ArrayList<>(conjunto);
    }
}
