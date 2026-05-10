package org.example.controlador;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * TemaManager — maneja el tema visual de toda la aplicación.
 * Uso:
 *   TemaManager.get().aplicarTema("cafe", scene);
 *   String actual = TemaManager.get().getTemaActual();
 */
public class Temamanager {

    public enum Tema {
        CAFE("cafe",    "☕ Café",   "tema-cafe.css"),
        OSCURO("oscuro","🌙 Oscuro", "tema-oscuro.css"),
        VERDE("verde",  "🌿 Verde",  "tema-verde.css"),
        AZUL("azul",    "❄️ Azul",   "tema-azul.css");

        public final String id;
        public final String nombre;
        public final String archivo;

        Tema(String id, String nombre, String archivo) {
            this.id = id;
            this.nombre = nombre;
            this.archivo = archivo;
        }

        public static Tema porId(String id) {
            for (Tema t : values()) {
                if (t.id.equals(id)) return t;
            }
            return CAFE; // default
        }
    }

    // ---- Singleton ----
    private static final Temamanager INSTANCE = new Temamanager();
    public static Temamanager get() { return INSTANCE; }

    private static final String PREF_KEY = "tema_actual";
    private final Preferences prefs = Preferences.userNodeForPackage(Temamanager.class);

    private final List<Scene> scenesRegistradas = new ArrayList<>();
    private String temaActual;

    private Temamanager() {
        temaActual = prefs.get(PREF_KEY, Tema.CAFE.id);
    }

    // ---- Registrar escenas para actualización automática ----
    public void registrarScene(Scene scene) {
        if (scene != null && !scenesRegistradas.contains(scene)) {
            scenesRegistradas.add(scene);
            aplicarTemaAScene(temaActual, scene);
        }
    }

    // ---- Cambiar tema globalmente ----
    public void cambiarTema(String temaId) {
        temaActual = temaId;
        prefs.put(PREF_KEY, temaId);
        for (Scene scene : scenesRegistradas) {
            aplicarTemaAScene(temaId, scene);
        }
    }

    // ---- Aplicar a una scene específica ----
    private void aplicarTemaAScene(String temaId, Scene scene) {
        if (scene == null) return;
        Tema tema = Tema.porId(temaId);
        String url = Temamanager.class.getResource(
                "/org/example/vista/" + tema.archivo
        ).toExternalForm();

        // Quitar temas anteriores, conservar otros CSS
        scene.getStylesheets().removeIf(s ->
                s.contains("tema-cafe") ||
                        s.contains("tema-oscuro") ||
                        s.contains("tema-verde") ||
                        s.contains("tema-azul")
        );
        // Insertar el nuevo tema AL INICIO para que las variables estén disponibles
        scene.getStylesheets().add(0, url);
    }

    // ---- Obtener lista de temas para ComboBox ----
    public String[] getNombresTemas() {
        Tema[] temas = Tema.values();
        String[] nombres = new String[temas.length];
        for (int i = 0; i < temas.length; i++) nombres[i] = temas[i].nombre;
        return nombres;
    }

    public String getTemaActual() { return temaActual; }

    public String getNombreTemaActual() {
        return Tema.porId(temaActual).nombre;
    }
}