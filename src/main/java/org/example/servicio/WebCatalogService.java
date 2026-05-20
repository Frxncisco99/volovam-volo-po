package org.example.servicio;

import org.example.dao.ConfiguracionWebDAO;
import org.example.modelo.ConfiguracionWeb;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCatalogService {

    private final ConfiguracionWebDAO dao;
    private final HttpClient httpClient;

    public WebCatalogService() {
        this(new ConfiguracionWebDAO());
    }

    public WebCatalogService(ConfiguracionWebDAO dao) {
        this.dao = dao;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public WebResult probarConexion() throws IOException, InterruptedException {
        return probarConexion(cargarConfiguracionWeb());
    }

    public WebResult probarConexion(ConfiguracionWeb config) throws IOException, InterruptedException {
        validarConexionBasica(config);
        HttpRequest request = request(config, "categorias?select=id&limit=1")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            dao.actualizarEstadoConexion("CONECTADO");
            return new WebResult(true, "Conexion web correcta.", response.statusCode(), response.body());
        }
        dao.actualizarEstadoConexion("ERROR");
        return new WebResult(false, mensajeHttp("No se pudo leer Supabase", response), response.statusCode(), response.body());
    }

    public void guardarConfiguracionWeb(ConfiguracionWeb config) {
        dao.guardarConfiguracionWeb(config);
    }

    public ConfiguracionWeb cargarConfiguracionWeb() {
        return dao.cargarConfiguracionWeb();
    }

    public WebResult sincronizarInventario() throws IOException, InterruptedException {
        return sincronizarInventario(cargarConfiguracionWeb());
    }

    public WebResult sincronizarInventario(ConfiguracionWeb config) throws IOException, InterruptedException {
        try {
            WebResult subida = subirInventarioLocalAWeb(config);
            if (!subida.ok()) return subida;
            WebResult pedidos = descargarPedidosWeb(config);
            if (!pedidos.ok()) return pedidos;
            dao.marcarSincronizacion("SINCRONIZADO");
            return new WebResult(true, "Inventario subido y pedidos web consultados correctamente.", 200, "");
        } catch (IOException | InterruptedException e) {
            int pendientes = dao.encolarInventarioLocal();
            dao.actualizarEstadoConexion("PENDIENTE");
            return new WebResult(false, "Sin conexion o escritura no disponible. Se dejaron " + pendientes + " cambios pendientes.", 0, e.getMessage());
        }
    }

    public WebResult subirInventarioLocalAWeb(ConfiguracionWeb config) throws IOException, InterruptedException {
        validarConexionBasica(config);
        List<ConfiguracionWebDAO.CategoriaWeb> categorias = dao.listarCategoriasWeb();
        List<ConfiguracionWebDAO.ProductoWeb> productos = dao.listarProductosWeb();

        if (!categorias.isEmpty()) {
            WebResult categoriasResult = upsertCategorias(config, categorias);
            if (!categoriasResult.ok()) return categoriasResult;
        }

        Map<Integer, String> categoriasWeb = obtenerMapaCategoriasWeb(config);
        for (ConfiguracionWebDAO.ProductoWeb producto : productos) {
            WebResult result = subirProductoWeb(config, producto, categoriasWeb.get(producto.idCategoriaLocal()));
            if (!result.ok()) return result;
        }

        WebResult configResult = subirConfiguracionPublica(config);
        if (!configResult.ok()) return configResult;

        dao.marcarSincronizacion("SINCRONIZADO");
        return new WebResult(true, "Inventario local subido a Supabase: " + productos.size() + " producto(s).", 200, "");
    }

    public WebResult subirProductoWeb(ConfiguracionWebDAO.ProductoWeb producto) throws IOException, InterruptedException {
        ConfiguracionWeb config = cargarConfiguracionWeb();
        Map<Integer, String> categoriasWeb = obtenerMapaCategoriasWeb(config);
        return subirProductoWeb(config, producto, categoriasWeb.get(producto.idCategoriaLocal()));
    }

    public WebResult subirProductoWeb(ConfiguracionWeb config, ConfiguracionWebDAO.ProductoWeb producto, String categoriaWebId)
            throws IOException, InterruptedException {
        String json = productoJson(producto, categoriaWebId);
        HttpRequest request = request(config, "productos?on_conflict=id_local")
                .header("Prefer", "resolution=merge-duplicates,return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString("[" + json + "]"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            return new WebResult(true, "Producto sincronizado: " + producto.nombre(), response.statusCode(), response.body());
        }
        return new WebResult(false, mensajeHttp("No se pudo subir producto " + producto.nombre(), response), response.statusCode(), response.body());
    }

    public WebResult actualizarStockWeb(int idProductoLocal, int nuevoStock) throws IOException, InterruptedException {
        return actualizarStockWeb(cargarConfiguracionWeb(), idProductoLocal, nuevoStock, null);
    }

    public WebResult actualizarStockWeb(ConfiguracionWeb config, int idProductoLocal, int nuevoStock, String codigoBarras)
            throws IOException, InterruptedException {
        validarConexionBasica(config);
        String filtro = codigoBarras != null && !codigoBarras.isBlank()
                ? "codigo_barras=eq." + url(codigoBarras)
                : "id_local=eq." + idProductoLocal;
        HttpRequest request = request(config, "productos?" + filtro)
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"stock\":" + Math.max(0, nuevoStock) + "}"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            return new WebResult(true, "Stock web actualizado.", response.statusCode(), response.body());
        }
        return new WebResult(false, mensajeHttp("No se pudo actualizar stock web", response), response.statusCode(), response.body());
    }

    public WebResult descargarPedidosWeb() throws IOException, InterruptedException {
        return descargarPedidosWeb(cargarConfiguracionWeb());
    }

    public WebResult descargarPedidosWeb(ConfiguracionWeb config) throws IOException, InterruptedException {
        validarConexionBasica(config);
        HttpRequest request = request(config, "pedidos?select=id,cliente_nombre,total,estado,created_at&order=created_at.desc&limit=20")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            return new WebResult(true, "Pedidos web consultados.", response.statusCode(), response.body());
        }
        return new WebResult(false, mensajeHttp("No se pudieron descargar pedidos web", response), response.statusCode(), response.body());
    }

    public WebResult descargarDatosWeb() throws IOException, InterruptedException {
        ConfiguracionWeb config = cargarConfiguracionWeb();
        validarConexionBasica(config);
        HttpRequest request = request(config, "productos?select=id&limit=1")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            return new WebResult(true, "Datos web disponibles para lectura.", response.statusCode(), response.body());
        }
        return new WebResult(false, mensajeHttp("No se pudieron leer datos web", response), response.statusCode(), response.body());
    }

    private WebResult upsertCategorias(ConfiguracionWeb config, List<ConfiguracionWebDAO.CategoriaWeb> categorias)
            throws IOException, InterruptedException {
        StringBuilder body = new StringBuilder("[");
        for (int i = 0; i < categorias.size(); i++) {
            if (i > 0) body.append(',');
            body.append(categorias.get(i).toJson());
        }
        body.append(']');
        HttpRequest request = request(config, "categorias?on_conflict=id_local")
                .header("Prefer", "resolution=merge-duplicates,return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            return new WebResult(true, "Categorias sincronizadas.", response.statusCode(), response.body());
        }
        return new WebResult(false, mensajeHttp("No se pudieron subir categorias", response), response.statusCode(), response.body());
    }

    private Map<Integer, String> obtenerMapaCategoriasWeb(ConfiguracionWeb config) throws IOException, InterruptedException {
        HttpRequest request = request(config, "categorias?select=id,id_local")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (!esExitoso(response.statusCode())) {
            throw new IOException(mensajeHttp("No se pudo leer el mapeo de categorias web", response));
        }
        Map<Integer, String> mapa = new HashMap<>();
        Matcher matcher = Pattern.compile("\\{\"id\":\"([^\"]+)\",\"id_local\":(\\d+)").matcher(response.body());
        while (matcher.find()) {
            mapa.put(Integer.parseInt(matcher.group(2)), matcher.group(1));
        }
        return mapa;
    }

    private WebResult subirConfiguracionPublica(ConfiguracionWeb config) throws IOException, InterruptedException {
        String body = "["
                + configItem("domicilio_activo", Boolean.toString(config.isDomicilioActivo())) + ","
                + configItem("costo_envio", config.getCostoEnvio().toPlainString()) + ","
                + configItem("whatsapp", config.getWhatsapp()) + ","
                + configItem("facebook_url", config.getFacebookUrl()) + ","
                + configItem("catalogo_activo", Boolean.toString(config.isCatalogoActivo())) + ","
                + configItem("pedidos_web_activos", Boolean.toString(config.isPedidosWebActivos())) + ","
                + configItem("mostrar_agotados", Boolean.toString(config.isMostrarAgotados())) + ","
                + configItem("ocultar_sin_stock", Boolean.toString(config.isOcultarSinStock()))
                + "]";
        HttpRequest request = request(config, "configuracion?on_conflict=clave")
                .header("Prefer", "resolution=merge-duplicates,return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (esExitoso(response.statusCode())) {
            return new WebResult(true, "Configuracion publica sincronizada.", response.statusCode(), response.body());
        }
        return new WebResult(false, mensajeHttp("No se pudo subir configuracion publica", response), response.statusCode(), response.body());
    }

    private HttpRequest.Builder request(ConfiguracionWeb config, String path) {
        String base = config.getSupabaseUrl().replaceAll("/+$", "");
        return HttpRequest.newBuilder(URI.create(base + "/rest/v1/" + path))
                .timeout(Duration.ofSeconds(18))
                .header("apikey", config.getSupabaseAnonKey())
                .header("Authorization", "Bearer " + config.getSupabaseAnonKey())
                .header("Content-Type", "application/json");
    }

    private void validarConexionBasica(ConfiguracionWeb config) {
        if (config.getSupabaseUrl().isBlank() || !config.getSupabaseUrl().startsWith("https://")) {
            throw new IllegalArgumentException("Configura una URL de Supabase valida con https://");
        }
        if (config.getSupabaseAnonKey().isBlank()) {
            throw new IllegalArgumentException("Configura la anon key/public key de Supabase.");
        }
        if (pareceServiceRole(config.getSupabaseAnonKey())) {
            throw new IllegalArgumentException("No guardes service role key en el POS. Usa solo anon key o un backend seguro.");
        }
    }

    private boolean pareceServiceRole(String key) {
        String lower = key.toLowerCase();
        if (lower.contains("service_role") || lower.contains("service-role")) return true;
        try {
            String[] parts = key.split("\\.");
            if (parts.length < 2) return false;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8).toLowerCase();
            return payload.contains("\"role\":\"service_role\"") || payload.contains("\"role\": \"service_role\"");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean esExitoso(int status) {
        return status >= 200 && status < 300;
    }

    private String mensajeHttp(String prefijo, HttpResponse<String> response) {
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            return prefijo + ". Supabase rechazo la operacion con la anon key. Para escritura segura usa usuario autenticado, backend o Edge Function.";
        }
        String body = response.body() == null ? "" : response.body();
        if (body.length() > 280) body = body.substring(0, 280) + "...";
        return prefijo + " (HTTP " + response.statusCode() + "). " + body;
    }

    private String productoJson(ConfiguracionWebDAO.ProductoWeb p, String categoriaWebId) {
        return "{"
                + "\"id_local\":" + p.idLocal()
                + ",\"codigo_barras\":" + jsonOrNull(p.codigoBarras())
                + ",\"nombre\":\"" + json(p.nombre()) + "\""
                + ",\"descripcion\":" + jsonOrNull(p.descripcion())
                + ",\"precio\":" + decimal(p.precio())
                + ",\"costo\":" + decimal(p.costo())
                + ",\"stock\":" + Math.max(0, p.stock())
                + ",\"stock_minimo\":" + Math.max(0, p.stockMinimo())
                + ",\"categoria_id\":" + jsonOrNull(categoriaWebId)
                + ",\"imagen_url\":" + jsonOrNull(p.imagenUrl())
                + ",\"unidad_medida\":\"" + json(p.unidadMedida() == null || p.unidadMedida().isBlank() ? "pieza" : p.unidadMedida()) + "\""
                + ",\"activo\":" + p.activo()
                + ",\"origen\":\"pos_local\""
                + ",\"sincronizado_en\":\"" + LocalDateTime.now() + "\""
                + "}";
    }

    private String configItem(String clave, String valor) {
        return "{\"clave\":\"" + json(clave) + "\",\"valor\":\"" + json(valor) + "\"}";
    }

    private String decimal(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private String jsonOrNull(String value) {
        return value == null || value.isBlank() ? "null" : "\"" + json(value) + "\"";
    }

    private String json(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record WebResult(boolean ok, String mensaje, int statusCode, String body) {}
}
