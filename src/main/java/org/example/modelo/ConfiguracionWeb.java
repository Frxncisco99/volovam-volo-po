package org.example.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class ConfiguracionWeb {

    private int id = 1;
    private String supabaseUrl = "";
    private String supabaseAnonKey = "";
    private String proyectoRef = "";
    private boolean catalogoActivo = false;
    private boolean pedidosWebActivos = false;
    private boolean mostrarAgotados = true;
    private boolean ocultarSinStock = true;
    private boolean domicilioActivo = false;
    private BigDecimal costoEnvio = new BigDecimal("50.00");
    private String whatsapp = "";
    private String facebookUrl = "";
    private LocalDateTime ultimaSincronizacion;
    private String estadoConexion = "SIN_CONEXION";
    private boolean usarCodigoBarras = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSupabaseUrl() { return supabaseUrl; }
    public void setSupabaseUrl(String supabaseUrl) { this.supabaseUrl = limpiar(supabaseUrl); }

    public String getSupabaseAnonKey() { return supabaseAnonKey; }
    public void setSupabaseAnonKey(String supabaseAnonKey) { this.supabaseAnonKey = limpiar(supabaseAnonKey); }

    public String getProyectoRef() { return proyectoRef; }
    public void setProyectoRef(String proyectoRef) { this.proyectoRef = limpiar(proyectoRef); }

    public boolean isCatalogoActivo() { return catalogoActivo; }
    public void setCatalogoActivo(boolean catalogoActivo) { this.catalogoActivo = catalogoActivo; }

    public boolean isPedidosWebActivos() { return pedidosWebActivos; }
    public void setPedidosWebActivos(boolean pedidosWebActivos) { this.pedidosWebActivos = pedidosWebActivos; }

    public boolean isMostrarAgotados() { return mostrarAgotados; }
    public void setMostrarAgotados(boolean mostrarAgotados) { this.mostrarAgotados = mostrarAgotados; }

    public boolean isOcultarSinStock() { return ocultarSinStock; }
    public void setOcultarSinStock(boolean ocultarSinStock) { this.ocultarSinStock = ocultarSinStock; }

    public boolean isDomicilioActivo() { return domicilioActivo; }
    public void setDomicilioActivo(boolean domicilioActivo) { this.domicilioActivo = domicilioActivo; }

    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio == null ? BigDecimal.ZERO : costoEnvio; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = limpiar(whatsapp); }

    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = limpiar(facebookUrl); }

    public LocalDateTime getUltimaSincronizacion() { return ultimaSincronizacion; }
    public void setUltimaSincronizacion(LocalDateTime ultimaSincronizacion) { this.ultimaSincronizacion = ultimaSincronizacion; }

    public String getEstadoConexion() { return estadoConexion; }
    public void setEstadoConexion(String estadoConexion) { this.estadoConexion = limpiar(estadoConexion).isBlank() ? "SIN_CONEXION" : limpiar(estadoConexion); }

    public boolean isUsarCodigoBarras() { return usarCodigoBarras; }
    public void setUsarCodigoBarras(boolean usarCodigoBarras) { this.usarCodigoBarras = usarCodigoBarras; }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
