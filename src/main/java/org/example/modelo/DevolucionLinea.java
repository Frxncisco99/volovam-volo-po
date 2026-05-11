package org.example.modelo;

public class DevolucionLinea {
    private final int    idDetalle;
    private final int    idProducto;
    private final String nombre;
    private final int    vendido;
    private final int    yaDevuelto;
    private final int    disponible;
    private final double precioUnitario;

    public DevolucionLinea(int idDetalle, int idProducto, String nombre,
                           int vendido, int yaDevuelto, int disponible,
                           double precioUnitario) {
        this.idDetalle      = idDetalle;
        this.idProducto     = idProducto;
        this.nombre         = nombre;
        this.vendido        = vendido;
        this.yaDevuelto     = yaDevuelto;
        this.disponible     = disponible;
        this.precioUnitario = precioUnitario;
    }

    public int    getIdDetalle()      { return idDetalle; }
    public int    getIdProducto()     { return idProducto; }
    public String getNombre()         { return nombre; }
    public int    getVendido()        { return vendido; }
    public int    getYaDevuelto()     { return yaDevuelto; }
    public int    getDisponible()     { return disponible; }
    public double getPrecioUnitario() { return precioUnitario; }
}