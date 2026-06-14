package com.duoc.ms_reportes;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.NotificacionDTO;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.dto.UbicacionDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import com.duoc.ms_reportes.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// ¡NUEVO IMPORT PARA RABBITMQ!
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - ReporteService")
class ReporteServiceTest {

    private ReporteRepository reporteRepository;
    private GeograficoClient geograficoClient;
    private NotificacionClient notificacionClient;
    private RabbitTemplate rabbitTemplate; // <-- NUEVO MOCK PARA LA PRUEBA
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteRepository = mock(ReporteRepository.class);
        geograficoClient = mock(GeograficoClient.class);
        notificacionClient = mock(NotificacionClient.class);
        rabbitTemplate = mock(RabbitTemplate.class); // <-- INICIALIZAMOS EL MOCK

        // ¡AQUÍ ESTABA EL ERROR! Ahora le pasamos los 4 argumentos, incluyendo rabbitTemplate
        reporteService = new ReporteService(reporteRepository, geograficoClient, notificacionClient, rabbitTemplate);
    }

    private ReporteDTO crearDtoValido(String prioridad) {
        ReporteDTO dto = new ReporteDTO();
        dto.setLatitud(-33.45);
        dto.setLongitud(-70.66);
        dto.setDescripcion("Incendio forestal");
        dto.setUrlMedia("");
        dto.setTipoUsuario("CIUDADANO");
        dto.setUsuarioId(1L);
        dto.setPrioridad(prioridad);
        return dto;
    }

    private Reporte crearReporteGuardado(Long id, String prioridad) {
        Reporte reporte = new Reporte();
        reporte.setId(id);
        reporte.setLatitud(-33.45);
        reporte.setLongitud(-70.66);
        reporte.setDescripcion("Incendio forestal");
        reporte.setUrlMedia("");
        reporte.setTipoUsuario("CIUDADANO");
        reporte.setUsuarioId(1L);
        reporte.setEstado("NUEVO");
        reporte.setPrioridad(prioridad);
        return reporte;
    }

    // =========================================================
    // crearReporteProcesado
    // =========================================================
    @Test
    void testCrearReporteProcesado_RolOperativoExitoso() {
        ReporteDTO dto = crearDtoValido("MEDIA");
        Reporte reporteSimulado = crearReporteGuardado(100L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "BRIGADISTA");

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals("NUEVO", resultado.getEstado());
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_AdminRespetaPrioridadAlta() {
        ReporteDTO dto = crearDtoValido("ALTA");
        Reporte reporteSimulado = crearReporteGuardado(101L, "ALTA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "ADMIN");

        assertNotNull(resultado);
        assertEquals(101L, resultado.getId());
        assertEquals("ALTA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_BomberoRespetaPrioridadBaja() {
        ReporteDTO dto = crearDtoValido("BAJA");
        Reporte reporteSimulado = crearReporteGuardado(102L, "BAJA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "BOMBERO");

        assertNotNull(resultado);
        assertEquals(102L, resultado.getId());
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_UserFuerzaPrioridadBajaAunqueEnvieAlta() {
        ReporteDTO dto = crearDtoValido("ALTA");
        Reporte reporteSimulado = crearReporteGuardado(103L, "BAJA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "USER");

        assertNotNull(resultado);
        assertEquals(103L, resultado.getId());
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_SinRolFuerzaPrioridadBaja() {
        ReporteDTO dto = crearDtoValido("CRITICA");
        Reporte reporteSimulado = crearReporteGuardado(104L, "BAJA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto);

        assertNotNull(resultado);
        assertEquals(104L, resultado.getId());
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_RolOperativoPrioridadInvalida() {
        ReporteDTO dto = crearDtoValido("CRITICA");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.crearReporteProcesado(dto, "BRIGADISTA")
        );

        assertEquals("La prioridad debe ser ALTA, MEDIA o BAJA", ex.getMessage());

        verify(reporteRepository, never()).save(any(Reporte.class));
        verify(geograficoClient, never()).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, never()).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_RolOperativoPrioridadVacia() {
        ReporteDTO dto = crearDtoValido(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.crearReporteProcesado(dto, "FUNCIONARIO")
        );

        assertEquals("La prioridad es obligatoria para usuarios operativos.", ex.getMessage());

        verify(reporteRepository, never()).save(any(Reporte.class));
        verify(geograficoClient, never()).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, never()).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_PrioridadMinusculaSeNormaliza() {
        ReporteDTO dto = crearDtoValido("media");
        Reporte reporteSimulado = crearReporteGuardado(105L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "BRIGADISTA");

        assertNotNull(resultado);
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_PrioridadConEspaciosSeNormaliza() {
        ReporteDTO dto = crearDtoValido(" MEDIA ");
        Reporte reporteSimulado = crearReporteGuardado(106L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "FUNCIONARIO");

        assertNotNull(resultado);
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_FallaMsGeografico() {
        ReporteDTO dto = crearDtoValido("MEDIA");
        Reporte reporteSimulado = crearReporteGuardado(107L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        doThrow(new RuntimeException("Error Geográfico"))
                .when(geograficoClient)
                .guardarUbicacion(any(UbicacionDTO.class));

        Reporte resultado = reporteService.crearReporteProcesado(dto, "ADMIN");

        assertNotNull(resultado);
        assertEquals(107L, resultado.getId());
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    @Test
    void testCrearReporteProcesado_FallaMsNotificaciones() {
        ReporteDTO dto = crearDtoValido("MEDIA");
        Reporte reporteSimulado = crearReporteGuardado(108L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteSimulado);

        doThrow(new RuntimeException("Error Notificación"))
                .when(notificacionClient)
                .enviarAlerta(any(NotificacionDTO.class));

        Reporte resultado = reporteService.crearReporteProcesado(dto, "ADMIN");

        assertNotNull(resultado);
        assertEquals(108L, resultado.getId());
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
        verify(notificacionClient, times(1)).enviarAlerta(any(NotificacionDTO.class));
    }

    // =========================================================
    // Listados
    // =========================================================
    @Test
    void testListarTodos() {
        when(reporteRepository.findAll())
                .thenReturn(List.of(new Reporte()));

        List<Reporte> lista = reporteService.listarTodos();

        assertEquals(1, lista.size());

        verify(reporteRepository, times(1)).findAll();
    }

    @Test
    void testListarActivos() {
        when(reporteRepository.findByEstadoIn(anyList()))
                .thenReturn(List.of(new Reporte()));

        List<Reporte> lista = reporteService.listarActivos();

        assertEquals(1, lista.size());

        verify(reporteRepository, times(1))
                .findByEstadoIn(List.of("NUEVO", "EN_PROGRESO"));
    }

    // =========================================================
    // obtenerPorId
    // =========================================================
    @Test
    void testObtenerPorId_Exitoso() {
        Reporte reporte = new Reporte();
        reporte.setId(5L);

        when(reporteRepository.findById(5L))
                .thenReturn(Optional.of(reporte));

        Reporte resultado = reporteService.obtenerPorId(5L);

        assertNotNull(resultado);
        assertEquals(5L, resultado.getId());

        verify(reporteRepository, times(1)).findById(5L);
    }

    @Test
    void testObtenerPorId_NoEncontrado() {
        when(reporteRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reporteService.obtenerPorId(99L)
        );

        verify(reporteRepository, times(1)).findById(99L);
    }

    // =========================================================
    // actualizarEstado
    // =========================================================
    @Test
    void testActualizarEstado_Exitoso() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(1L);
        reporteOriginal.setEstado("NUEVO");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(1L);
        reporteModificado.setEstado("EN_PROGRESO");

        when(reporteRepository.findById(1L))
                .thenReturn(Optional.of(reporteOriginal));

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarEstado(1L, "EN_PROGRESO");

        assertNotNull(resultado);
        assertEquals("EN_PROGRESO", resultado.getEstado());

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testActualizarEstado_NoEncontrado() {
        when(reporteRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reporteService.actualizarEstado(1L, "RESUELTO")
        );

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void testActualizarEstado_EstadoInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarEstado(1L, "FINALIZADO")
        );

        assertEquals("El estado debe ser NUEVO, EN_PROGRESO o RESUELTO.", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    // =========================================================
    // actualizarPrioridad
    // =========================================================
    @Test
    void testActualizarPrioridad_Exitoso() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(1L);
        reporteOriginal.setPrioridad("BAJA");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(1L);
        reporteModificado.setPrioridad("ALTA");

        when(reporteRepository.findById(1L))
                .thenReturn(Optional.of(reporteOriginal));

        when(reporteRepository.save(any(Reporte.class)))
                .thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarPrioridad(1L, "ALTA");

        assertNotNull(resultado);
        assertEquals("ALTA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testActualizarPrioridad_NoEncontrado() {
        when(reporteRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reporteService.actualizarPrioridad(1L, "MEDIA")
        );

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void testActualizarPrioridad_Invalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarPrioridad(1L, "URGENTE")
        );

        assertEquals("La prioridad debe ser ALTA, MEDIA o BAJA", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }
}