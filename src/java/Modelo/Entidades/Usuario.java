package Modelo.Entidades;

public class Usuario {
    private String idUsuario;
    private String idRol;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String estado;
    private String direccion;

    // Constructor vacío 
    public Usuario() {
    }

    // Constructor completo para cuando recuperamos datos de la BD
    public Usuario(String idUsuario, String idRol, String name, String email, String phone, String password, String estado, String direccion) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.estado = estado;
        this.direccion = direccion;
    }

    // Getters y Setters
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdRol() {
        return idRol;
    }

    public void setIdRol(String idRol) {
        this.idRol = idRol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}