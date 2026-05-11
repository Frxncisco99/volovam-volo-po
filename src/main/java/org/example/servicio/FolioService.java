package org.example.servicio;

public class FolioService {

    public static String venta(int id)      { return String.format("VTA-%06d", id); }
    public static String devolucion(int id) { return String.format("DEV-%06d", id); }
    public static String corte(int id)      { return String.format("COR-%06d", id); }
    public static String cancelacion(int id){ return String.format("CAN-%06d", id); }
    public static String ajuste(int id)     { return String.format("AJU-%06d", id); }
}