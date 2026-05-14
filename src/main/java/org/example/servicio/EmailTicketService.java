package org.example.servicio;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.modelo.Ticket;

import java.util.Properties;
import java.util.prefs.Preferences;

public class EmailTicketService {

    private final Preferences prefs = Preferences.userNodeForPackage(
            org.example.controlador.ConfiguracionController.class);

    public boolean estaActivo() {
        return prefs.getBoolean("email_activo", false);
    }

    public void enviarTicket(Ticket ticket, String destinatario) throws Exception {
        if (!estaActivo()) {
            return;
        }
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("El correo destino es obligatorio.");
        }

        Config config = cargarConfig();
        if (config.remitente().isBlank() || config.password().isBlank()) {
            throw new IllegalStateException("Configura remitente y password de correo en Configuracion.");
        }

        Session session = Session.getInstance(propiedades(config), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.remitente(), config.password());
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.remitente()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario.trim()));
        message.setSubject("Ticket de venta #" + String.format("%06d", ticket.getIdVenta()));
        message.setText(cuerpoTicket(ticket, config));
        Transport.send(message);
    }

    private String cuerpoTicket(Ticket ticket, Config config) {
        String textoTicket = TicketRenderer.generar(ticket,
                config.ticketNombre(), config.ticketGiro(),
                config.ticketDireccion(), config.ticketCiudad(), config.ticketTelefono(),
                config.ticketEncabezado(), config.ticketPie(), config.ticketAviso(),
                config.ticketLogo(), config.ticketFolio(), config.ticketDesglose(), config.ticketQr(),
                true, true, config.ticketAncho());

        return "Gracias por su compra.\n\n" +
                textoTicket +
                "\nEste correo fue generado automaticamente por el POS.";
    }

    private Properties propiedades(Config config) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", config.puerto());
        return props;
    }

    private Config cargarConfig() {
        String smtp = prefs.get("email_smtp", "Gmail");
        String host = prefs.get("email_host", "").trim();
        String puerto = prefs.get("email_puerto", "").trim();

        if (host.isBlank() || puerto.isBlank()) {
            if ("Outlook / Hotmail".equals(smtp)) {
                host = "smtp.office365.com";
                puerto = "587";
            } else {
                host = "smtp.gmail.com";
                puerto = "587";
            }
        }

        String anchoPapel = prefs.get("ticket_ancho", prefs.get("cmbAnchoPapel", "58 mm"));
        int ancho = "58 mm".equals(anchoPapel) ? TicketRenderer.ANCHO_58MM : TicketRenderer.ANCHO_80MM;

        return new Config(
                smtp,
                prefs.get("email_remitente", "").trim(),
                prefs.get("email_password", ""),
                host,
                puerto,
                prefs.get("ticket_nombre", ""),
                prefs.get("ticket_giro", ""),
                prefs.get("ticket_direccion", ""),
                prefs.get("ticket_ciudad", ""),
                prefs.get("ticket_telefono", ""),
                prefs.get("ticket_encabezado", ""),
                prefs.get("ticket_pie", ""),
                prefs.get("ticket_aviso", "Este ticket no es comprobante fiscal"),
                prefs.getBoolean("ticket_logo", true),
                prefs.getBoolean("ticket_folio", true),
                prefs.getBoolean("ticket_desglose", true),
                prefs.getBoolean("ticket_qr", false),
                ancho
        );
    }

    private record Config(
            String smtp,
            String remitente,
            String password,
            String host,
            String puerto,
            String ticketNombre,
            String ticketGiro,
            String ticketDireccion,
            String ticketCiudad,
            String ticketTelefono,
            String ticketEncabezado,
            String ticketPie,
            String ticketAviso,
            boolean ticketLogo,
            boolean ticketFolio,
            boolean ticketDesglose,
            boolean ticketQr,
            int ticketAncho
    ) {}
}
