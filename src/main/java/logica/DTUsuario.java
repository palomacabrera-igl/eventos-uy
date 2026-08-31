package logica;

public class DTUsuario {

    private String nickname;
    private String nombre;
    private String correo;

    public DTUsuario(String nickname, String nombre, String correo) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getNickname() {return nickname;}
    public String getNombre() {return nombre;}
    public String getCorreo() {return correo;}

    public void setNombre(String nombre) {this.nombre = nombre;}
}