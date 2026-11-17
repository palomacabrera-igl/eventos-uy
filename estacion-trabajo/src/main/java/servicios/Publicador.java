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
import logica.ReglaNegocioException;
import logica.TipoUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * La fachada de servicios web del Servidor Central: lo unico que el sitio (el
 * Servidor Web) puede pedirle a la logica desde otra computadora.
 *
 * <h2>Que es</h2>
 * Cada metodo publico de esta clase es una operacion del contrato (el WSDL).
 * JAX-WS lee las anotaciones, arma el contrato solo y atiende los pedidos
 * SOAP que llegan. Quien la publica es el Main: Endpoint.publish(url, new Publicador()).
 *
 * <h2>Reglas para tocar esta clase</h2>
 * <ol>
 *   <li><b>Es una fachada delgada.</b> Cada metodo hace UNA cosa: llamar al
 *       controlador y devolver lo que contesta. Nada de reglas de negocio, ni
 *       de JPA, ni de transacciones: eso ya vive en la logica, y la
 *       transaccion la abre el intermediario que da la Fabrica
 *       (ControladorConUnidadDeTrabajo). Por eso el controlador se pide a la
 *       Fabrica y nunca con new Sistema().</li>
 *   <li><b>Solo operaciones SIN estado.</b> Las del Laboratorio 1
 *       (seleccionarXxx, ingresarXxx) guardan datos en campos compartidos de
 *       un unico Sistema; con varios clientes a la vez se pisarian. Las que se
 *       publican reciben TODO lo que necesitan en una sola llamada.</li>
 *   <li><b>Cada operacion tiene un nombre unico.</b> SOAP no admite dos
 *       operaciones con el mismo nombre. El controlador tiene sobrecargas
 *       (por ejemplo listarEventos() y listarEventos(categoria)): aca se
 *       publica una sola version de cada una, la que no depende del estado.</li>
 *   <li><b>@WebParam(name = "...") en todos los parametros.</b> Sin el, en el
 *       contrato se llaman arg0, arg1... y el codigo que genera el sitio queda
 *       ilegible.</li>
 *   <li><b>List y no Set.</b> En XML no existe el "conjunto", solo elementos
 *       que se repiten, y el cliente que genera wsimport siempre usa List. El
 *       controlador devuelve Set en varias operaciones: se convierte aca.</li>
 *   <li><b>Solo viajan DT, tipos simples, enumerados y byte[].</b> Nunca
 *       entidades del dominio ni LocalDate (ver DatatypesJaxbTest).</li>
 *   <li><b>Los errores de negocio se declaran en el throws.</b>
 *       ReglaNegocioException se convierte en un SOAP Fault que conserva el
 *       mensaje. Una excepcion NO declarada llegaria al sitio como un error
 *       generico, sin mensaje util.</li>
 * </ol>
 *
 * <h2>Por que @XmlSeeAlso</h2>
 * Varias operaciones declaran DTUsuario, pero lo que viaja de verdad es un
 * DTAsistente o un DTOrganizador. JAXB solo incluye en el contrato los tipos
 * que aparecen en las firmas; sin esta anotacion no conoceria las subclases y
 * fallaria al responder. Con ella el XML lleva el tipo real del usuario.
 *
 * <h2>Donde agregar una operacion nueva</h2>
 * Al final, en la seccion de su tema, y con su prueba en PublicadorTest.
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
    //  (operaciones nuevas del Laboratorio 3: agregar aca)
    // =====================================================================

    // =====================================================================
    //  Video, busqueda y sitio para celular
    //  (operaciones nuevas del Laboratorio 3: agregar aca)
    // =====================================================================

    // =====================================================================
    //  Asistencia, constancia, visitas y validacion
    //  (operaciones nuevas del Laboratorio 3: agregar aca)
    // =====================================================================

    /**
     * Convierte el Set que devuelve el controlador en la List que viaja por la
     * red. Es privado a proposito: JAX-WS solo publica los metodos publicos.
     */
    private static <T> List<T> aLista(Set<T> conjunto) {
        return new ArrayList<>(conjunto);
    }
}
