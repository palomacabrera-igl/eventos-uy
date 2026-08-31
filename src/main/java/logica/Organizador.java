package logica;

/**
 * Usuario de tipo organizador. Ademas de los datos de {@link Usuario}, tiene
 * una descripcion general y un enlace a su sitio web, que puede no estar
 * definido
 */
public class Organizador extends Usuario {

    private String descripcion;
    private String sitioWeb;

    public Organizador(String nickname, String nombre, String correoElectronico,
                       String descripcion, String sitioWeb) {
        super(nickname, nombre, correoElectronico);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    @Override
    public DTUsuario obtenerDT() {
        return new DTOrganizador(getNickname(), getNombre(), getCorreoElectronico(),
                descripcion, sitioWeb);
    }

    @Override
    public TipoUsuario obtenerTipoUsuario() {
        return TipoUsuario.ORGANIZADOR;
    }

    @Override
    public void modificarDatos(DTUsuario dt) {
        super.modificarDatos(dt);
        DTOrganizador dorg = (DTOrganizador) dt;
        this.descripcion = dorg.getDescripcion();
        this.sitioWeb = dorg.getSitioWeb();
    }
}