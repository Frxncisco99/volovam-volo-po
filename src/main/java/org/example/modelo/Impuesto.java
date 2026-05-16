package org.example.modelo;

import java.math.BigDecimal;

public class Impuesto {

    private int idImpuesto;
    private String clave;
    private String nombre;
    private String tipo;
    private BigDecimal tasa;
    private boolean activo;
    private boolean predeterminado;

    public Impuesto() {
        this.tasa = BigDecimal.ZERO;
    }

    public Impuesto(int idImpuesto, String clave, String nombre, String tipo,
                    BigDecimal tasa, boolean activo, boolean predeterminado) {
        this.idImpuesto = idImpuesto;
        this.clave = clave;
        this.nombre = nombre;
        this.tipo = tipo;
        this.tasa = tasa == null ? BigDecimal.ZERO : tasa;
        this.activo = activo;
        this.predeterminado = predeterminado;
    }

    public static Impuesto sinImpuesto() {
        return new Impuesto(0, "SIN_IMPUESTO", "Sin impuesto", "SIN_IMPUESTO",
                BigDecimal.ZERO, true, false);
    }

    public int getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getTasa() {
        return tasa == null ? BigDecimal.ZERO : tasa;
    }

    public void setTasa(BigDecimal tasa) {
        this.tasa = tasa == null ? BigDecimal.ZERO : tasa;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }

    @Override
    public String toString() {
        return nombre == null ? "" : nombre;
    }
}
