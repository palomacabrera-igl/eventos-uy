package logica;

public class DTOrganizador extends DTUsuario {

    private String descripcion;
    private String sitioWeb;

    public DTOrganizador(String nickname, String nombre, String correo,
                         String descripcion, String sitioWeb) {
        super(nickname, nombre, correo);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}

}