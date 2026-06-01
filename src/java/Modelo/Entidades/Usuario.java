package Modelo.Entidades;

public class Usuario {
    private int idUsuario;
    private int idRol;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String estado;

    // Constructor vacío (Esencial para frameworks y buenas prácticas)
    public Usuario() {
    }

    // Constructor completo para cuando recuperamos datos de la BD
    public Usuario(int idUsuario, int idRol, String name, String email, String phone, String password, String estado) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
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
}