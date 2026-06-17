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

@DisplayName("Pruebas Unitarias - UbicacionService")
class UbicacionServiceTest {

    private UbicacionRepository ubicacionRepository;
    private UbicacionService ubicacionService;

    @BeforeEach
    void setUp() {
        ubicacionRepository = mock(UbicacionRepository.class);
        ubicacionService = new UbicacionService(ubicacionRepository);
    }

    /**
     * Construye una Ubicacion usando ReflectionTestUtils para evitar
     * dependencia directa de los setters generados por Lombok,
     * que a veces el IDE no resuelve hasta que corre el annotation processor.
     */
    private Ubicacion crearUbicacion(Long id, Long idReporte, Double latitud, Double longitud) {
        Ubicacion u = new Ubicacion();
        if (id        != null) ReflectionTestUtils.setField(u, "id",        id);
        if (idReporte != null) ReflectionTestUtils.setField(u, "idReporte", idReporte);
        if (latitud   != null) ReflectionTestUtils.setField(u, "latitud",   latitud);
        if (longitud  != null) ReflectionTestUtils.setField(u, "longitud",  longitud);
        return u;
    }

    // =========================================================
    // guardarUbicacion — delega en el repositorio y retorna resultado
    // =========================================================
    @Test
    @DisplayName("guardarUbicacion - persiste y retorna la ubicacion con ID asignado")
    void testGuardarUbicacion_Exitoso() {
        Ubicacion input     = crearUbicacion(null, 5L, -36.826, -73.049);
        Ubicacion persistida = crearUbicacion(1L,  5L, -36.826, -73.049);

        when(ubicacionRepository.save(input)).thenReturn(persistida);

        Ubicacion resultado = ubicacionService.guardarUbicacion(input);

        assertNotNull(resultado);
        assertEquals(1L,      ReflectionTestUtils.getField(resultado, "id"));
        assertEquals(5L,      ReflectionTestUtils.getField(resultado, "idReporte"));
        assertEquals(-36.826, ReflectionTestUtils.getField(resultado, "latitud"));
        assertEquals(-73.049, ReflectionTestUtils.getField(resultado, "longitud"));
        verify(ubicacionRepository, times(1)).save(input);
    }

    // =========================================================
    // obtenerPorReporte — encontrada → retorna la entidad
    // =========================================================
    @Test
    @DisplayName("obtenerPorReporte - reporte existente retorna la ubicacion")
    void testObtenerPorReporte_Encontrada() {
        Ubicacion encontrada = crearUbicacion(2L, 10L, -33.456, -70.648);

        when(ubicacionRepository.findByIdReporte(10L)).thenReturn(Optional.of(encontrada));

        Ubicacion resultado = ubicacionService.obtenerPorReporte(10L);

        assertNotNull(resultado);
        assertEquals(2L,      ReflectionTestUtils.getField(resultado, "id"));
        assertEquals(10L,     ReflectionTestUtils.getField(resultado, "idReporte"));
        assertEquals(-33.456, ReflectionTestUtils.getField(resultado, "latitud"));
        assertEquals(-70.648, ReflectionTestUtils.getField(resultado, "longitud"));
        verify(ubicacionRepository, times(1)).findByIdReporte(10L);
    }

    // =========================================================
    // obtenerPorReporte — no encontrada → lanza EntityNotFoundException
    // =========================================================
    @Test
    @DisplayName("obtenerPorReporte - reporte inexistente lanza EntityNotFoundException")
    void testObtenerPorReporte_NoEncontrada() {
        when(ubicacionRepository.findByIdReporte(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> ubicacionService.obtenerPorReporte(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(ubicacionRepository, times(1)).findByIdReporte(99L);
    }

    // =========================================================
    // obtenerPorReporte — mensaje contiene el ID exacto del reporte
    // =========================================================
    @Test
    @DisplayName("obtenerPorReporte - mensaje de excepcion contiene el ID del reporte")
    void testObtenerPorReporte_MensajeContieneId() {
        when(ubicacionRepository.findByIdReporte(42L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> ubicacionService.obtenerPorReporte(42L)
        );

        assertEquals(
                "No se encontr\u00f3 ubicaci\u00f3n geogr\u00e1fica para el reporte con ID: 42",
                ex.getMessage()
        );
    }
}