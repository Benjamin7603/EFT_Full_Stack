package com.duoc.ms_geografico;

import com.duoc.ms_geografico.controller.GeograficoController;
import com.duoc.ms_geografico.exception.GlobalExceptionHandler;
import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.service.UbicacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - GeograficoController")
class GeograficoControllerTest {

    private MockMvc mockMvc;
    private UbicacionService ubicacionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ubicacionService = Mockito.mock(UbicacionService.class);

        GeograficoController geograficoController =
                new GeograficoController(ubicacionService);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(geograficoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGuardarUbicacion_Exitoso() throws Exception {
        Ubicacion ubicacionInput = new Ubicacion();
        ubicacionInput.setIdReporte(10L);
        ubicacionInput.setLatitud(-33.456);
        ubicacionInput.setLongitud(-70.648);

        Ubicacion ubicacionPersistida = new Ubicacion();
        ubicacionPersistida.setId(1L);
        ubicacionPersistida.setIdReporte(10L);
        ubicacionPersistida.setLatitud(-33.456);
        ubicacionPersistida.setLongitud(-70.648);

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class)))
                .thenReturn(ubicacionPersistida);

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ubicacionInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idReporte").value(10))
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(ubicacionService, times(1))
                .guardarUbicacion(any(Ubicacion.class));
    }

    @Test
    void testObtenerPorReporte_Exitoso() throws Exception {
        Ubicacion ubicacionEncontrada = new Ubicacion();
        ubicacionEncontrada.setId(1L);
        ubicacionEncontrada.setIdReporte(10L);
        ubicacionEncontrada.setLatitud(-33.456);
        ubicacionEncontrada.setLongitud(-70.648);

        when(ubicacionService.obtenerPorReporte(10L))
                .thenReturn(ubicacionEncontrada);

        mockMvc.perform(get("/api/geografico/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idReporte").value(10))
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(ubicacionService, times(1))
                .obtenerPorReporte(10L);
    }

    @Test
    void testObtenerPorReporte_NoEncontrado() throws Exception {
        when(ubicacionService.obtenerPorReporte(99L))
                .thenThrow(new EntityNotFoundException(
                        "No se encontró ubicación geográfica para el reporte con ID: 99"
                ));

        mockMvc.perform(get("/api/geografico/reporte/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(
                        "No se encontró ubicación geográfica para el reporte con ID: 99"
                ));

        verify(ubicacionService, times(1))
                .obtenerPorReporte(99L);
    }

    @Test
    void testGuardarUbicacion_DatosInvalidos() throws Exception {
        Ubicacion ubicacionInvalida = new Ubicacion();

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ubicacionInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.idReporte").value("El ID del reporte es obligatorio"))
                .andExpect(jsonPath("$.latitud").value("La latitud es obligatoria"))
                .andExpect(jsonPath("$.longitud").value("La longitud es obligatoria"));
    }
}