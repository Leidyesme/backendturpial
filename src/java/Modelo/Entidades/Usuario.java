package Modelo.Entidades;

public class Usuario {
    private int idUsuario;
    private int idRol;
    private String name;
    private String correo;
    private String telefono;
    private String contrasena;
    private String estado;

    // Constructor vacío (Esencial para frameworks y buenas prácticas)
    public Usuario() {
    }

    // Constructor completo para cuando recuperamos datos de la BD
    public Usuario(int idUsuario, int idRol, String name, String correo, String telefono, String contrasena, String estado) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.name = name;
        this.correo = correo;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}