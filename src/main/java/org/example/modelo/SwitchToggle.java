package org.example.modelo;

import javafx.animation.TranslateTransition;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


public class SwitchToggle extends StackPane {

    // --- Colores ---
    private static final Color COLOR_ON      = Color.web("#D4A843");   // dorado
    private static final Color COLOR_OFF     = Color.web("#CCBBAA");   // gris cálido
    private static final Color COLOR_THUMB   = Color.WHITE;
    private static final Color COLOR_HOVER   = Color.web("#B8A090");   // hover off

    // --- Dimensiones ---
    private static final double WIDTH  = 46;
    private static final double HEIGHT = 26;
    private static final double RADIUS = HEIGHT / 2.0;
    private static final double THUMB_R = (HEIGHT / 2.0) - 3;

    // --- Posiciones thumb ---
    private static final double POS_OFF = -(WIDTH / 2.0 - RADIUS);
    private static final double POS_ON  =  (WIDTH / 2.0 - RADIUS);

    // --- Nodos internos ---
    private final Rectangle track;
    private final Circle    thumb;

    // --- Estado ---
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    // --- Color actual del track (para evitar leer getFill() durante animacion) ---
    private Color currentTrackColor;

    // ---------------------------------------------------------------
    public SwitchToggle() {
        this(false);
    }

    public SwitchToggle(boolean initialState) {

        // ---- Track (fondo del switch) ----
        track = new Rectangle(WIDTH, HEIGHT);
        track.setArcWidth(HEIGHT);
        track.setArcHeight(HEIGHT);
        currentTrackColor = initialState ? COLOR_ON : COLOR_OFF;
        track.setFill(currentTrackColor);

        // ---- Thumb (bolita blanca) ----
        thumb = new Circle(THUMB_R);
        thumb.setFill(COLOR_THUMB);
        thumb.setTranslateX(initialState ? POS_ON : POS_OFF);

        // Sombra suave en la bolita
        thumb.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 4, 0, 0, 1);");

        getChildren().addAll(track, thumb);
        setSelected(initialState);

        // ---- Cursor ----
        setStyle("-fx-cursor: hand;");

        // ---- Click ----
        setOnMouseClicked(e -> toggle());

        // ---- Hover track ----
        setOnMouseEntered(e -> {
            if (!selected.get()) track.setFill(COLOR_HOVER);
        });
        setOnMouseExited(e -> {
            // Restaurar al color correcto segun estado actual
            track.setFill(selected.get() ? COLOR_ON : COLOR_OFF);
            currentTrackColor = selected.get() ? COLOR_ON : COLOR_OFF;
        });

        // ---- Escuchar cambios externos (ej. setSelected desde controlador) ----
        selected.addListener((obs, oldVal, newVal) -> animateTo(newVal));
    }

    // ---------------------------------------------------------------
    /** Alterna el estado con animación */
    public void toggle() {
        setSelected(!selected.get());
    }

    // ---------------------------------------------------------------
    /** Anima el thumb y el color del track */
    private void animateTo(boolean on) {
        Color targetColor = on ? COLOR_ON : COLOR_OFF;

        // Thumb deslizante
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), thumb);
        slide.setToX(on ? POS_ON : POS_OFF);

        // Color del track — parte SIEMPRE del color guardado, no del getFill()
        FillTransition fill = new FillTransition(Duration.millis(200), track);
        fill.setFromValue(currentTrackColor);
        fill.setToValue(targetColor);

        // Actualizar el color destino ANTES de lanzar la animacion
        currentTrackColor = targetColor;

        // Ambos en paralelo
        new ParallelTransition(slide, fill).play();
    }

    // ---------------------------------------------------------------
    // ---- Propiedades JavaFX (compatibles con FXML) ----

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    // Tamaño preferido
    @Override
    protected double computePrefWidth(double height) {
        return WIDTH + 2;
    }

    @Override
    protected double computePrefHeight(double width) {
        return HEIGHT + 2;
    }
}