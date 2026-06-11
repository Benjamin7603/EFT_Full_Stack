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

@DisplayName("Pruebas Unitarias - NotificacionService")
class NotificacionServiceTest {

    private NotificacionRepository notificacionRepository;
    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        notificacionRepository = mock(NotificacionRepository.class);
        notificacionService = new NotificacionService(notificacionRepository);
    }

    private Notificacion crearNotificacion() {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setTitulo("Nuevo reporte");
        notificacion.setMensaje("Alerta activa");
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setTipo("REPORTE");
        notificacion.setPrioridad("ALTA");
        notificacion.setLeida(false);
        notificacion.setReporteId(10L);
        return notificacion;
    }

    @Test
    void testEnviarAlerta_ConDatosCompletos() {
        Notificacion notificacion = crearNotificacion();

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertNotNull(resultado);
        assertEquals("Nuevo reporte", resultado.getTitulo());
        assertEquals("REPORTE", resultado.getTipo());
        assertEquals("ALTA", resultado.getPrioridad());
        assertFalse(resultado.getLeida());

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testEnviarAlerta_AsignaValoresPorDefectoCuandoCamposSonNull() {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje("Mensaje de prueba");
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setTitulo(null);
        notificacion.setTipo(null);
        notificacion.setPrioridad(null);
        notificacion.setLeida(null);

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertEquals("Alerta GeoFire", resultado.getTitulo());
        assertEquals("SISTEMA", resultado.getTipo());
        assertEquals("MEDIA", resultado.getPrioridad());
        assertFalse(resultado.getLeida());

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testEnviarAlerta_AsignaValoresPorDefectoCuandoCamposSonBlancos() {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje("Mensaje de prueba");
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setTitulo("   ");
        notificacion.setTipo("   ");
        notificacion.setPrioridad("   ");
        notificacion.setLeida(null);

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertEquals("Alerta GeoFire", resultado.getTitulo());
        assertEquals("SISTEMA", resultado.getTipo());
        assertEquals("MEDIA", resultado.getPrioridad());
        assertFalse(resultado.getLeida());

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testEnviarAlerta_NormalizaTexto() {
        Notificacion notificacion = new Notificacion();
        notificacion.setTitulo("  alerta importante  ");
        notificacion.setMensaje("  Mensaje de prueba  ");
        notificacion.setDestinatario("  BRIGADAS_ZONA_SUR  ");
        notificacion.setTipo(" reporte ");
        notificacion.setPrioridad(" alta ");
        notificacion.setLeida(false);

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertEquals("alerta importante", resultado.getTitulo());
        assertEquals("Mensaje de prueba", resultado.getMensaje());
        assertEquals("BRIGADAS_ZONA_SUR", resultado.getDestinatario());
        assertEquals("REPORTE", resultado.getTipo());
        assertEquals("ALTA", resultado.getPrioridad());
        assertFalse(resultado.getLeida());

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testEnviarAlerta_NoFallaConMensajeNullPorqueValidacionEsDelController() {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje(null);
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertNull(resultado.getMensaje());
        assertEquals("BRIGADAS_ZONA_SUR", resultado.getDestinatario());

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testEnviarAlerta_NoFallaConDestinatarioNullPorqueValidacionEsDelController() {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje("Mensaje válido");
        notificacion.setDestinatario(null);

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.enviarAlerta(notificacion);

        assertEquals("Mensaje válido", resultado.getMensaje());
        assertNull(resultado.getDestinatario());

        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testListarHistorial() {
        when(notificacionRepository.findAll())
                .thenReturn(List.of(crearNotificacion()));

        List<Notificacion> resultado = notificacionService.listarHistorial();

        assertEquals(1, resultado.size());
        verify(notificacionRepository, times(1)).findAll();
    }

    @Test
    void testObtenerPorId_Existe() {
        Notificacion notificacion = crearNotificacion();

        when(notificacionRepository.findById(1L))
                .thenReturn(Optional.of(notificacion));

        Notificacion resultado = notificacionService.obtenerPorId(1L);

        assertEquals(1L, resultado.getId());
        verify(notificacionRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerPorId_NoExiste() {
        when(notificacionRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                notificacionService.obtenerPorId(99L)
        );

        assertEquals("La notificación con ID 99 no existe.", ex.getMessage());
        verify(notificacionRepository, times(1)).findById(99L);
    }

    @Test
    void testListarPorDestinatario() {
        when(notificacionRepository.findByDestinatarioOrderByFechaEnvioDesc("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of(crearNotificacion()));

        List<Notificacion> resultado =
                notificacionService.listarPorDestinatario("BRIGADAS_ZONA_SUR");

        assertEquals(1, resultado.size());
        verify(notificacionRepository, times(1))
                .findByDestinatarioOrderByFechaEnvioDesc("BRIGADAS_ZONA_SUR");
    }

    @Test
    void testListarPorDestinatario_Null() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.listarPorDestinatario(null)
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never()).findByDestinatarioOrderByFechaEnvioDesc(anyString());
    }

    @Test
    void testListarPorDestinatario_Blanco() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.listarPorDestinatario(" ")
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never()).findByDestinatarioOrderByFechaEnvioDesc(anyString());
    }

    @Test
    void testListarNoLeidas() {
        when(notificacionRepository.findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of(crearNotificacion()));

        List<Notificacion> resultado =
                notificacionService.listarNoLeidas("BRIGADAS_ZONA_SUR");

        assertEquals(1, resultado.size());
        verify(notificacionRepository, times(1))
                .findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc("BRIGADAS_ZONA_SUR");
    }

    @Test
    void testListarNoLeidas_DestinatarioNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.listarNoLeidas(null)
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never())
                .findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc(anyString());
    }

    @Test
    void testListarNoLeidas_DestinatarioBlanco() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.listarNoLeidas("   ")
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never())
                .findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc(anyString());
    }

    @Test
    void testContarNoLeidas() {
        when(notificacionRepository.countByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR"))
                .thenReturn(5L);

        long resultado = notificacionService.contarNoLeidas("BRIGADAS_ZONA_SUR");

        assertEquals(5L, resultado);
        verify(notificacionRepository, times(1))
                .countByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR");
    }

    @Test
    void testContarNoLeidas_DestinatarioNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.contarNoLeidas(null)
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never()).countByDestinatarioAndLeidaFalse(anyString());
    }

    @Test
    void testContarNoLeidas_DestinatarioBlanco() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.contarNoLeidas("   ")
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never()).countByDestinatarioAndLeidaFalse(anyString());
    }

    @Test
    void testMarcarComoLeida() {
        Notificacion notificacion = crearNotificacion();
        notificacion.setLeida(false);

        when(notificacionRepository.findById(1L))
                .thenReturn(Optional.of(notificacion));

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.marcarComoLeida(1L);

        assertTrue(resultado.getLeida());
        verify(notificacionRepository, times(1)).findById(1L);
        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void testMarcarTodasComoLeidas() {
        Notificacion n1 = crearNotificacion();
        n1.setLeida(false);

        Notificacion n2 = crearNotificacion();
        n2.setId(2L);
        n2.setLeida(false);

        when(notificacionRepository.findByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of(n1, n2));

        when(notificacionRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Notificacion> resultado =
                notificacionService.marcarTodasComoLeidas("BRIGADAS_ZONA_SUR");

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getLeida());
        assertTrue(resultado.get(1).getLeida());

        verify(notificacionRepository, times(1))
                .findByDestinatarioAndLeidaFalse("BRIGADAS_ZONA_SUR");
        verify(notificacionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testMarcarTodasComoLeidas_DestinatarioNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.marcarTodasComoLeidas(null)
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never()).findByDestinatarioAndLeidaFalse(anyString());
        verify(notificacionRepository, never()).saveAll(anyList());
    }

    @Test
    void testMarcarTodasComoLeidas_DestinatarioBlanco() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.marcarTodasComoLeidas("   ")
        );

        assertEquals("El destinatario es obligatorio.", ex.getMessage());
        verify(notificacionRepository, never()).findByDestinatarioAndLeidaFalse(anyString());
        verify(notificacionRepository, never()).saveAll(anyList());
    }

    @Test
    void testEliminar() {
        Notificacion notificacion = crearNotificacion();

        when(notificacionRepository.findById(1L))
                .thenReturn(Optional.of(notificacion));

        notificacionService.eliminar(1L);

        verify(notificacionRepository, times(1)).findById(1L);
        verify(notificacionRepository, times(1)).delete(notificacion);
    }

    @Test
    void testEliminar_NoExiste() {
        when(notificacionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                notificacionService.eliminar(99L)
        );

        verify(notificacionRepository, times(1)).findById(99L);
        verify(notificacionRepository, never()).delete(any(Notificacion.class));
    }
}