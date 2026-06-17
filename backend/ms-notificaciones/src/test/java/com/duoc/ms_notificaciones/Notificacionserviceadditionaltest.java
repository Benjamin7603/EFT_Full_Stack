package com.duoc.ms_notificaciones;

import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.repository.NotificacionRepository;
import com.duoc.ms_notificaciones.service.NotificacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests adicionales para NotificacionService.
 * Cubren paths y ramas que los tests existentes no ejercitan.
 */
@DisplayName("Pruebas Adicionales - NotificacionService")
class NotificacionServiceAdditionalTest {

    private NotificacionRepository notificacionRepository;
    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        notificacionRepository = mock(NotificacionRepository.class);
        notificacionService = new NotificacionService(notificacionRepository);
    }

    private Notificacion crearNotificacion() {
        Notificacion n = new Notificacion();
        n.setId(1L);
        n.setTitulo("Nuevo reporte");
        n.setMensaje("Alerta activa");
        n.setDestinatario("BRIGADAS_ZONA_SUR");
        n.setTipo("REPORTE");
        n.setPrioridad("ALTA");
        n.setLeida(false);
        n.setReporteId(10L);
        return n;
    }

    // =========================================================
    // enviarAlerta — propaga RuntimeException del repositorio
    // =========================================================
    @Test
    @DisplayName("enviarAlerta - propaga excepción del repositorio")
    void testEnviarAlerta_PropagaExcepcion() {
        Notificacion notificacion = crearNotificacion();

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenThrow(new RuntimeException("BD no disponible"));

        assertThrows(RuntimeException.class,
                () -> notificacionService.enviarAlerta(notificacion));

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    // =========================================================
    // enviarAlerta — leida ya está en true (no se sobreescribe)
    // =========================================================
    @Test
    @DisplayName("enviarAlerta - respeta leida=true si viene informado")
    void testEnviarAlerta_LeidaYaEsTrue() {
        Notificacion notificacion = crearNotificacion();
        notificacion.setLeida(true);

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertTrue(resultado.getLeida());
    }

    // =========================================================
    // marcarComoLeida — EntityNotFoundException si no existe
    // =========================================================
    @Test
    @DisplayName("marcarComoLeida - lanza EntityNotFoundException si no existe")
    void testMarcarComoLeida_NoExiste() {
        when(notificacionRepository.findById(55L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> notificacionService.marcarComoLeida(55L));

        assertTrue(ex.getMessage().contains("55"));
        verify(notificacionRepository, never()).save(any());
    }

    // =========================================================
    // marcarTodasComoLeidas — lista ya vacía (no hay nada que actualizar)
    // =========================================================
    @Test
    @DisplayName("marcarTodasComoLeidas - lista vacía retorna lista vacía sin guardar")
    void testMarcarTodasComoLeidas_ListaVacia() {
        when(notificacionRepository.findByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of());
        when(notificacionRepository.saveAll(List.of())).thenReturn(List.of());

        List<Notificacion> resultado =
                notificacionService.marcarTodasComoLeidas("BRIGADAS_ZONA_SUR");

        assertTrue(resultado.isEmpty());
        verify(notificacionRepository, times(1))
                .findByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR");
        verify(notificacionRepository, times(1)).saveAll(List.of());
    }

    // =========================================================
    // listarHistorial — retorna lista vacía
    // =========================================================
    @Test
    @DisplayName("listarHistorial - retorna lista vacía cuando no hay notificaciones")
    void testListarHistorial_Vacio() {
        when(notificacionRepository.findAll()).thenReturn(List.of());

        List<Notificacion> resultado = notificacionService.listarHistorial();

        assertTrue(resultado.isEmpty());
        verify(notificacionRepository, times(1)).findAll();
    }

    // =========================================================
    // contarNoLeidas — retorna cero cuando no hay pendientes
    // =========================================================
    @Test
    @DisplayName("contarNoLeidas - retorna 0 cuando todas están leídas")
    void testContarNoLeidas_Cero() {
        when(notificacionRepository.countByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR"))
                .thenReturn(0L);

        long resultado = notificacionService.contarNoLeidas("BRIGADAS_ZONA_SUR");

        assertEquals(0L, resultado);
    }

    // =========================================================
    // listarNoLeidas — lista vacía cuando todas están leídas
    // =========================================================
    @Test
    @DisplayName("listarNoLeidas - retorna lista vacía cuando todas están leídas")
    void testListarNoLeidas_Vacio() {
        when(notificacionRepository
                .findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of());

        List<Notificacion> resultado =
                notificacionService.listarNoLeidas("BRIGADAS_ZONA_SUR");

        assertTrue(resultado.isEmpty());
    }

    // =========================================================
    // eliminar — propaga excepción del repositorio al hacer delete
    // =========================================================
    @Test
    @DisplayName("eliminar - propaga excepción del repositorio")
    void testEliminar_PropagaExcepcion() {
        Notificacion notificacion = crearNotificacion();
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        doThrow(new RuntimeException("Error al eliminar")).when(notificacionRepository).delete(notificacion);

        assertThrows(RuntimeException.class, () -> notificacionService.eliminar(1L));

        verify(notificacionRepository, times(1)).delete(notificacion);
    }
}