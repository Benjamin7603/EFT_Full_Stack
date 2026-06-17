package com.duoc.ms_notificaciones;

import com.duoc.ms_notificaciones.listener.ReporteListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests unitarios para ReporteListener.
 * No requiere RabbitMQ real — se invoca el método directamente,
 * igual que cualquier método Java normal.
 */
@DisplayName("Pruebas Unitarias - ReporteListener")
class ReporteListenerTest {

    private ReporteListener reporteListener;

    @BeforeEach
    void setUp() {
        reporteListener = new ReporteListener();
    }

    // =========================================================
    // recibirMensaje — JSON válido con mensaje y destinatario
    // =========================================================
    @Test
    @DisplayName("recibirMensaje - JSON válido es procesado sin excepción")
    void testRecibirMensaje_JsonValido() {
        String mensajeJson = """
                {
                    "mensaje": "Foco de incendio detectado en sector sur",
                    "destinatario": "BRIGADAS_ZONA_SUR"
                }
                """;

        assertDoesNotThrow(() -> reporteListener.recibirMensaje(mensajeJson));
    }

    // =========================================================
    // recibirMensaje — JSON con campos adicionales (se ignoran)
    // =========================================================
    @Test
    @DisplayName("recibirMensaje - JSON con campos extra no lanza excepción")
    void testRecibirMensaje_JsonConCamposExtra() {
        String mensajeJson = """
                {
                    "mensaje": "Alerta activa",
                    "destinatario": "COORDINADORES",
                    "prioridad": "ALTA",
                    "reporteId": 99
                }
                """;

        assertDoesNotThrow(() -> reporteListener.recibirMensaje(mensajeJson));
    }

    // =========================================================
    // recibirMensaje — JSON inválido (entra al catch, no lanza)
    // =========================================================
    @Test
    @DisplayName("recibirMensaje - JSON malformado entra al catch sin propagar excepción")
    void testRecibirMensaje_JsonInvalido() {
        String mensajeJson = "esto no es json {{{";

        assertDoesNotThrow(() -> reporteListener.recibirMensaje(mensajeJson));
    }

    // =========================================================
    // recibirMensaje — JSON vacío (falta clave "mensaje" → NullPointerException en catch)
    // =========================================================
    @Test
    @DisplayName("recibirMensaje - JSON sin clave 'mensaje' entra al catch sin propagar excepción")
    void testRecibirMensaje_JsonSinClaveMensaje() {
        String mensajeJson = """
                {
                    "destinatario": "BRIGADAS_ZONA_SUR"
                }
                """;

        assertDoesNotThrow(() -> reporteListener.recibirMensaje(mensajeJson));
    }

    // =========================================================
    // recibirMensaje — string vacío (JSON inválido → catch)
    // =========================================================
    @Test
    @DisplayName("recibirMensaje - string vacío entra al catch sin propagar excepción")
    void testRecibirMensaje_StringVacio() {
        assertDoesNotThrow(() -> reporteListener.recibirMensaje(""));
    }
}