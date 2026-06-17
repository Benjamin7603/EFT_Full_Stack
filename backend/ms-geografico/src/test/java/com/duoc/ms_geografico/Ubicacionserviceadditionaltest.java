package com.duoc.ms_geografico;

import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.repository.UbicacionRepository;
import com.duoc.ms_geografico.service.UbicacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests adicionales para UbicacionService.
 * Complementan UbicacionServiceTest.java cubriendo los escenarios
 * que faltan para alcanzar >85 % de cobertura.
 */
@DisplayName("Pruebas Adicionales - UbicacionService")
class UbicacionServiceAdditionalTest {

    private UbicacionRepository ubicacionRepository;
    private UbicacionService ubicacionService;

    @BeforeEach
    void setUp() {
        ubicacionRepository = mock(UbicacionRepository.class);
        ubicacionService = new UbicacionService(ubicacionRepository);
    }

    private Ubicacion crearUbicacion(Long id, Long idReporte, Double latitud, Double longitud) {
        Ubicacion u = new Ubicacion();
        if (id        != null) ReflectionTestUtils.setField(u, "id",        id);
        if (idReporte != null) ReflectionTestUtils.setField(u, "idReporte", idReporte);
        if (latitud   != null) ReflectionTestUtils.setField(u, "latitud",   latitud);
        if (longitud  != null) ReflectionTestUtils.setField(u, "longitud",  longitud);
        return u;
    }

    // =========================================================
    // guardarUbicacion — el repositorio lanza excepción (ej. BD caída)
    // =========================================================
    @Test
    @DisplayName("guardarUbicacion - propaga RuntimeException del repositorio")
    void testGuardarUbicacion_PropagaExcepcion() {
        Ubicacion input = crearUbicacion(null, 7L, -33.0, -71.0);

        when(ubicacionRepository.save(input))
                .thenThrow(new RuntimeException("BD no disponible"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> ubicacionService.guardarUbicacion(input)
        );

        assertEquals("BD no disponible", ex.getMessage());
        verify(ubicacionRepository, times(1)).save(input);
    }

    // =========================================================
    // guardarUbicacion — objeto con zonaRiesgo configurada
    // =========================================================
    @Test
    @DisplayName("guardarUbicacion - persiste correctamente con zonaRiesgo")
    void testGuardarUbicacion_ConZonaRiesgo() {
        Ubicacion input = crearUbicacion(null, 3L, -38.7, -72.6);
        ReflectionTestUtils.setField(input, "zonaRiesgo", "ALTA");

        Ubicacion persistida = crearUbicacion(5L, 3L, -38.7, -72.6);
        ReflectionTestUtils.setField(persistida, "zonaRiesgo", "ALTA");

        when(ubicacionRepository.save(input)).thenReturn(persistida);

        Ubicacion resultado = ubicacionService.guardarUbicacion(input);

        assertNotNull(resultado);
        assertEquals("ALTA", ReflectionTestUtils.getField(resultado, "zonaRiesgo"));
        assertEquals(5L,    ReflectionTestUtils.getField(resultado, "id"));
    }

    // =========================================================
    // obtenerPorReporte — repositorio lanza excepción inesperada
    // =========================================================
    @Test
    @DisplayName("obtenerPorReporte - propaga excepción inesperada del repositorio")
    void testObtenerPorReporte_PropagaExcepcion() {
        when(ubicacionRepository.findByIdReporte(55L))
                .thenThrow(new RuntimeException("Timeout de conexión"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> ubicacionService.obtenerPorReporte(55L)
        );

        assertEquals("Timeout de conexión", ex.getMessage());
        verify(ubicacionRepository, times(1)).findByIdReporte(55L);
    }

    // =========================================================
    // obtenerPorReporte — mensaje incluye exactamente el ID "0"
    // =========================================================
    @Test
    @DisplayName("obtenerPorReporte - mensaje de excepción incluye ID 0")
    void testObtenerPorReporte_MensajeIdCero() {
        when(ubicacionRepository.findByIdReporte(0L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> ubicacionService.obtenerPorReporte(0L)
        );

        assertTrue(ex.getMessage().contains("0"));
    }

    // =========================================================
    // guardarUbicacion — verifica que no se llama más de una vez al repo
    // =========================================================
    @Test
    @DisplayName("guardarUbicacion - delega exactamente una vez en el repositorio")
    void testGuardarUbicacion_DelegaUnaVez() {
        Ubicacion input     = crearUbicacion(null, 1L, -40.0, -75.0);
        Ubicacion persistida = crearUbicacion(9L,  1L, -40.0, -75.0);

        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(persistida);

        ubicacionService.guardarUbicacion(input);

        verify(ubicacionRepository, times(1)).save(input);
        verifyNoMoreInteractions(ubicacionRepository);
    }
}