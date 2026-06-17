package com.duoc.ms_reportes;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.dto.UbicacionDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import com.duoc.ms_reportes.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - ReporteService")
class ReporteServiceTest {

    private ReporteRepository reporteRepository;
    private GeograficoClient geograficoClient;
    private NotificacionClient notificacionClient;
    private RabbitTemplate rabbitTemplate;
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteRepository = mock(ReporteRepository.class);
        geograficoClient = mock(GeograficoClient.class);
        notificacionClient = mock(NotificacionClient.class);

        rabbitTemplate = new RabbitTemplate() {
            @Override
            public void convertAndSend(String routingKey, Object message) {
                // Mock manual para evitar conexión real a RabbitMQ.
            }
        };

        reporteService = new ReporteService(
                reporteRepository,
                geograficoClient,
                notificacionClient,
                rabbitTemplate
        );
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

    @Test
    @DisplayName("Crear reporte - BRIGADISTA respeta prioridad MEDIA")
    void testCrearReporteProcesado_RolOperativoExitoso() {
        ReporteDTO dto = crearDtoValido("MEDIA");
        Reporte reporteSimulado = crearReporteGuardado(100L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "BRIGADISTA");

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals("NUEVO", resultado.getEstado());
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - ADMIN respeta prioridad ALTA")
    void testCrearReporteProcesado_AdminRespetaPrioridadAlta() {
        ReporteDTO dto = crearDtoValido("ALTA");
        Reporte reporteSimulado = crearReporteGuardado(101L, "ALTA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "ADMIN");

        assertNotNull(resultado);
        assertEquals(101L, resultado.getId());
        assertEquals("ALTA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - BOMBERO respeta prioridad BAJA")
    void testCrearReporteProcesado_BomberoRespetaPrioridadBaja() {
        ReporteDTO dto = crearDtoValido("BAJA");
        Reporte reporteSimulado = crearReporteGuardado(102L, "BAJA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "BOMBERO");

        assertNotNull(resultado);
        assertEquals(102L, resultado.getId());
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - FUNCIONARIO normaliza prioridad con espacios")
    void testCrearReporteProcesado_PrioridadConEspaciosSeNormaliza() {
        ReporteDTO dto = crearDtoValido(" MEDIA ");
        Reporte reporteSimulado = crearReporteGuardado(106L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "FUNCIONARIO");

        assertNotNull(resultado);
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - USER fuerza prioridad BAJA aunque envíe ALTA")
    void testCrearReporteProcesado_UserFuerzaPrioridadBajaAunqueEnvieAlta() {
        ReporteDTO dto = crearDtoValido("ALTA");
        Reporte reporteSimulado = crearReporteGuardado(103L, "BAJA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "USER");

        assertNotNull(resultado);
        assertEquals(103L, resultado.getId());
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - sin rol fuerza prioridad BAJA")
    void testCrearReporteProcesado_SinRolFuerzaPrioridadBaja() {
        ReporteDTO dto = crearDtoValido("CRITICA");
        Reporte reporteSimulado = crearReporteGuardado(104L, "BAJA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto);

        assertNotNull(resultado);
        assertEquals(104L, resultado.getId());
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - rol vacío fuerza prioridad BAJA")
    void testCrearReporteProcesado_RolVacioFuerzaPrioridadBaja() {
        ReporteDTO dto = crearDtoValido("ALTA");
        Reporte reporteSimulado = crearReporteGuardado(108L, "BAJA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "   ");

        assertNotNull(resultado);
        assertEquals("BAJA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - prioridad minúscula se normaliza")
    void testCrearReporteProcesado_PrioridadMinusculaSeNormaliza() {
        ReporteDTO dto = crearDtoValido("media");
        Reporte reporteSimulado = crearReporteGuardado(105L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto, "BRIGADISTA");

        assertNotNull(resultado);
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - rol operativo con prioridad inválida lanza excepción")
    void testCrearReporteProcesado_RolOperativoPrioridadInvalida() {
        ReporteDTO dto = crearDtoValido("CRITICA");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.crearReporteProcesado(dto, "BRIGADISTA")
        );

        assertEquals("La prioridad debe ser ALTA, MEDIA o BAJA", ex.getMessage());

        verify(reporteRepository, never()).save(any(Reporte.class));
        verify(geograficoClient, never()).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - rol operativo con prioridad vacía lanza excepción")
    void testCrearReporteProcesado_RolOperativoPrioridadVacia() {
        ReporteDTO dto = crearDtoValido(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.crearReporteProcesado(dto, "FUNCIONARIO")
        );

        assertEquals("La prioridad es obligatoria para usuarios operativos.", ex.getMessage());

        verify(reporteRepository, never()).save(any(Reporte.class));
        verify(geograficoClient, never()).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - rol operativo con prioridad null lanza excepción")
    void testCrearReporteProcesado_RolOperativoPrioridadNull() {
        ReporteDTO dto = crearDtoValido(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.crearReporteProcesado(dto, "ADMIN")
        );

        assertEquals("La prioridad es obligatoria para usuarios operativos.", ex.getMessage());

        verify(reporteRepository, never()).save(any(Reporte.class));
        verify(geograficoClient, never()).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - si falla ms-geográfico igual retorna reporte")
    void testCrearReporteProcesado_FallaMsGeografico() {
        ReporteDTO dto = crearDtoValido("MEDIA");
        Reporte reporteSimulado = crearReporteGuardado(107L, "MEDIA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        doThrow(new RuntimeException("Error Geográfico"))
                .when(geograficoClient)
                .guardarUbicacion(any(UbicacionDTO.class));

        Reporte resultado = reporteService.crearReporteProcesado(dto, "ADMIN");

        assertNotNull(resultado);
        assertEquals(107L, resultado.getId());
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Crear reporte - si falla RabbitMQ igual retorna reporte")
    void testCrearReporteProcesado_FallaRabbitMQ() {
        RabbitTemplate rabbitTemplateConFallo = new RabbitTemplate() {
            @Override
            public void convertAndSend(String routingKey, Object message) {
                throw new RuntimeException("RabbitMQ no disponible");
            }
        };

        ReporteService serviceConRabbitFallando = new ReporteService(
                reporteRepository,
                geograficoClient,
                notificacionClient,
                rabbitTemplateConFallo
        );

        ReporteDTO dto = crearDtoValido("ALTA");
        Reporte reporteSimulado = crearReporteGuardado(109L, "ALTA");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = serviceConRabbitFallando.crearReporteProcesado(dto, "ADMIN");

        assertNotNull(resultado);
        assertEquals(109L, resultado.getId());
        assertEquals("ALTA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verify(geograficoClient, times(1)).guardarUbicacion(any(UbicacionDTO.class));
    }

    @Test
    @DisplayName("Listar todos")
    void testListarTodos() {
        when(reporteRepository.findAll()).thenReturn(List.of(new Reporte()));

        List<Reporte> lista = reporteService.listarTodos();

        assertEquals(1, lista.size());

        verify(reporteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Listar activos")
    void testListarActivos() {
        when(reporteRepository.findByEstadoIn(anyList())).thenReturn(List.of(new Reporte()));

        List<Reporte> lista = reporteService.listarActivos();

        assertEquals(1, lista.size());

        verify(reporteRepository, times(1))
                .findByEstadoIn(List.of("NUEVO", "EN_PROGRESO"));
    }

    @Test
    @DisplayName("Obtener por ID - exitoso")
    void testObtenerPorId_Exitoso() {
        Reporte reporte = new Reporte();
        reporte.setId(5L);

        when(reporteRepository.findById(5L)).thenReturn(Optional.of(reporte));

        Reporte resultado = reporteService.obtenerPorId(5L);

        assertNotNull(resultado);
        assertEquals(5L, resultado.getId());

        verify(reporteRepository, times(1)).findById(5L);
    }

    @Test
    @DisplayName("Obtener por ID - no encontrado")
    void testObtenerPorId_NoEncontrado() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reporteService.obtenerPorId(99L)
        );

        verify(reporteRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Actualizar estado - EN_PROGRESO exitoso")
    void testActualizarEstado_Exitoso() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(1L);
        reporteOriginal.setEstado("NUEVO");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(1L);
        reporteModificado.setEstado("EN_PROGRESO");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporteOriginal));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarEstado(1L, "EN_PROGRESO");

        assertNotNull(resultado);
        assertEquals("EN_PROGRESO", resultado.getEstado());

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar estado - NUEVO exitoso")
    void testActualizarEstado_NuevoExitoso() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(2L);
        reporteOriginal.setEstado("EN_PROGRESO");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(2L);
        reporteModificado.setEstado("NUEVO");

        when(reporteRepository.findById(2L)).thenReturn(Optional.of(reporteOriginal));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarEstado(2L, "NUEVO");

        assertNotNull(resultado);
        assertEquals("NUEVO", resultado.getEstado());

        verify(reporteRepository, times(1)).findById(2L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar estado - RESUELTO con espacios y minúsculas")
    void testActualizarEstado_ResueltoConEspaciosMinuscula() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(3L);
        reporteOriginal.setEstado("EN_PROGRESO");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(3L);
        reporteModificado.setEstado("RESUELTO");

        when(reporteRepository.findById(3L)).thenReturn(Optional.of(reporteOriginal));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarEstado(3L, " resuelto ");

        assertNotNull(resultado);
        assertEquals("RESUELTO", resultado.getEstado());

        verify(reporteRepository, times(1)).findById(3L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar estado - no encontrado")
    void testActualizarEstado_NoEncontrado() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reporteService.actualizarEstado(1L, "RESUELTO")
        );

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar estado - inválido")
    void testActualizarEstado_EstadoInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarEstado(1L, "FINALIZADO")
        );

        assertEquals("El estado debe ser NUEVO, EN_PROGRESO o RESUELTO.", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar estado - null")
    void testActualizarEstado_Null() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarEstado(1L, null)
        );

        assertEquals("El estado del reporte es obligatorio.", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar estado - blank")
    void testActualizarEstado_Blank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarEstado(1L, "   ")
        );

        assertEquals("El estado del reporte es obligatorio.", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar prioridad - exitoso")
    void testActualizarPrioridad_Exitoso() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(1L);
        reporteOriginal.setPrioridad("BAJA");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(1L);
        reporteModificado.setPrioridad("ALTA");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporteOriginal));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarPrioridad(1L, "ALTA");

        assertNotNull(resultado);
        assertEquals("ALTA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar prioridad - MEDIA con espacios y minúsculas")
    void testActualizarPrioridad_MediaConEspaciosMinuscula() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(2L);
        reporteOriginal.setPrioridad("BAJA");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(2L);
        reporteModificado.setPrioridad("MEDIA");

        when(reporteRepository.findById(2L)).thenReturn(Optional.of(reporteOriginal));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarPrioridad(2L, " media ");

        assertNotNull(resultado);
        assertEquals("MEDIA", resultado.getPrioridad());

        verify(reporteRepository, times(1)).findById(2L);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar prioridad - no encontrado")
    void testActualizarPrioridad_NoEncontrado() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reporteService.actualizarPrioridad(1L, "MEDIA")
        );

        verify(reporteRepository, times(1)).findById(1L);
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar prioridad - inválida")
    void testActualizarPrioridad_Invalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarPrioridad(1L, "URGENTE")
        );

        assertEquals("La prioridad debe ser ALTA, MEDIA o BAJA", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Actualizar prioridad - null")
    void testActualizarPrioridad_Null() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reporteService.actualizarPrioridad(1L, null)
        );

        assertEquals("La prioridad es obligatoria para usuarios operativos.", ex.getMessage());

        verify(reporteRepository, never()).findById(anyLong());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    @DisplayName("Generar Excel de auditoría - exitoso")
    void testDescargarExcelAuditoria() {
        Reporte reporteMock = new Reporte();
        reporteMock.setId(100L);
        reporteMock.setDescripcion("Foco de prueba");
        reporteMock.setPrioridad("ALTA");
        reporteMock.setEstado("NUEVO");
        reporteMock.setTipoUsuario("BOMBERO");
        reporteMock.setLatitud(-33.45);
        reporteMock.setLongitud(-70.66);
        reporteMock.setUsuarioId(77L);
        reporteMock.setFechaReporte(LocalDateTime.now());

        when(reporteRepository.findAll()).thenReturn(List.of(reporteMock));

        byte[] excelGenerado = reporteService.generarExcelAuditoriaReportes();

        assertNotNull(excelGenerado);
        assertTrue(excelGenerado.length > 0);

        verify(reporteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Generar Excel de auditoría - cubre campos nulos y resúmenes")
    void testGenerarExcelAuditoriaConCamposNulosYResumenes() {
        Reporte reporteNulo = new Reporte();
        reporteNulo.setId(null);
        reporteNulo.setDescripcion(null);
        reporteNulo.setPrioridad(null);
        reporteNulo.setEstado(null);
        reporteNulo.setTipoUsuario(null);
        reporteNulo.setLatitud(null);
        reporteNulo.setLongitud(null);
        reporteNulo.setUsuarioId(null);
        reporteNulo.setFechaReporte(null);

        Reporte reporteNuevoAlta = new Reporte();
        reporteNuevoAlta.setId(200L);
        reporteNuevoAlta.setDescripcion("Reporte nuevo");
        reporteNuevoAlta.setPrioridad("ALTA");
        reporteNuevoAlta.setEstado("NUEVO");
        reporteNuevoAlta.setTipoUsuario("ADMIN");
        reporteNuevoAlta.setLatitud(-33.45);
        reporteNuevoAlta.setLongitud(-70.66);
        reporteNuevoAlta.setUsuarioId(10L);
        reporteNuevoAlta.setFechaReporte(LocalDateTime.now());

        Reporte reporteEnProgresoBaja = new Reporte();
        reporteEnProgresoBaja.setId(201L);
        reporteEnProgresoBaja.setDescripcion("Reporte en progreso");
        reporteEnProgresoBaja.setPrioridad("BAJA");
        reporteEnProgresoBaja.setEstado("EN_PROGRESO");
        reporteEnProgresoBaja.setTipoUsuario("BRIGADISTA");
        reporteEnProgresoBaja.setLatitud(-34.00);
        reporteEnProgresoBaja.setLongitud(-71.00);
        reporteEnProgresoBaja.setUsuarioId(11L);
        reporteEnProgresoBaja.setFechaReporte(LocalDateTime.now());

        Reporte reporteResueltoMedia = new Reporte();
        reporteResueltoMedia.setId(202L);
        reporteResueltoMedia.setDescripcion("Reporte resuelto");
        reporteResueltoMedia.setPrioridad("MEDIA");
        reporteResueltoMedia.setEstado("RESUELTO");
        reporteResueltoMedia.setTipoUsuario("FUNCIONARIO");
        reporteResueltoMedia.setLatitud(-35.00);
        reporteResueltoMedia.setLongitud(-72.00);
        reporteResueltoMedia.setUsuarioId(12L);
        reporteResueltoMedia.setFechaReporte(LocalDateTime.now());

        when(reporteRepository.findAll())
                .thenReturn(List.of(
                        reporteNulo,
                        reporteNuevoAlta,
                        reporteEnProgresoBaja,
                        reporteResueltoMedia
                ));

        byte[] excelGenerado = reporteService.generarExcelAuditoriaReportes();

        assertNotNull(excelGenerado);
        assertTrue(excelGenerado.length > 0);

        verify(reporteRepository, times(1)).findAll();
    }
}