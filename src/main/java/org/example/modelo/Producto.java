package org.example.modelo;

public class Producto {

    private int idProducto;
    private String nombre;
    private double precio;
    private double costo;
    private int stock;
    private int stockMinimo;
    private int idCategoria;
    private boolean activo;

    // ESTE CAMPO ES PARA EL INVENTARIO (JOIN)
    private String categoria;

    public Producto() {}

    public Producto(int idProducto, String nombre, double precio, double costo,
                    int stock, int stockMinimo, int idCategoria, boolean activo) {
        this.idProducto  = idProducto;
        this.nombre      = nombre;
        this.precio      = precio;
        this.costo       = costo;
        this.stock       = stock;
        this.stockMinimo = stockMinimo;
        this.idCategoria = idCategoria;
        this.activo      = activo;
    }

    // =====================
    // GETTERS Y SETTERS
    // =====================

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // Método utilitario — usado en todo el sistema
    public boolean isBajoStock() {
        return stock <= stockMinimo;
    }
}