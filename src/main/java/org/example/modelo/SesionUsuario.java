package org.example.modelo;

public class SesionUsuario {

    private static SesionUsuario instancia;

    private int idUsuario;
    private String nombre;
    private String usuario;
    private int idRol;
    private String rol;
    private double tipoCambioDolar;

    public double getTipoCambioDolar() { return tipoCambioDolar; }
    public void setTipoCambioDolar(double tipoCambioDolar) { this.tipoCambioDolar = tipoCambioDolar; }

    // Constructor privado
    private SesionUsuario() {

    }

    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public static void cerrarSesion() {
        instancia = null;
    }

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    private int idCaja;

    public int getIdCaja() { return idCaja; }
    public void setIdCaja(int idCaja) { this.idCaja = idCaja; }
}