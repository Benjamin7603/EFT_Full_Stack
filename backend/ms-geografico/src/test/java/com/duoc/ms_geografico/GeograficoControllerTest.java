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
        GeograficoController geograficoController = new GeograficoController(ubicacionService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(geograficoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================
    // POST /api/geografico/guardar — exitoso
    // =========================================================
    @Test
    @DisplayName("POST /guardar - guarda ubicacion y retorna datos persistidos")
    void testGuardarUbicacion_Exitoso() throws Exception {
        Ubicacion input = new Ubicacion();
        input.setIdReporte(10L);
        input.setLatitud(-33.456);
        input.setLongitud(-70.648);

        Ubicacion persistida = new Ubicacion();
        persistida.setId(1L);
        persistida.setIdReporte(10L);
        persistida.setLatitud(-33.456);
        persistida.setLongitud(-70.648);

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class))).thenReturn(persistida);

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idReporte").value(10))
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(ubicacionService, times(1)).guardarUbicacion(any(Ubicacion.class));
    }

    // =========================================================
    // POST /api/geografico/guardar — validacion falla (campos nulos)
    // =========================================================
    @Test
    @DisplayName("POST /guardar - datos invalidos retorna 400 con mensajes de validacion")
    void testGuardarUbicacion_DatosInvalidos() throws Exception {
        Ubicacion invalida = new Ubicacion(); // sin idReporte, latitud ni longitud

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.idReporte").value("El ID del reporte es obligatorio"))
                .andExpect(jsonPath("$.latitud").value("La latitud es obligatoria"))
                .andExpect(jsonPath("$.longitud").value("La longitud es obligatoria"));

        verify(ubicacionService, never()).guardarUbicacion(any());
    }

    // =========================================================
    // POST /api/geografico/guardar — service lanza EntityNotFoundException
    // =========================================================
    @Test
    @DisplayName("POST /guardar - EntityNotFoundException retorna 404")
    void testGuardarUbicacion_EntidadNoEncontrada() throws Exception {
        Ubicacion input = new Ubicacion();
        input.setIdReporte(10L);
        input.setLatitud(-33.456);
        input.setLongitud(-70.648);

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class)))
                .thenThrow(new EntityNotFoundException("Coordenada inexistente"));

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Coordenada inexistente"));
    }

    // =========================================================
    // POST /api/geografico/guardar — service lanza Exception generica
    // =========================================================
    @Test
    @DisplayName("POST /guardar - Exception generica retorna 500")
    void testGuardarUbicacion_ErrorGeneral() throws Exception {
        Ubicacion input = new Ubicacion();
        input.setIdReporte(10L);
        input.setLatitud(-33.456);
        input.setLongitud(-70.648);

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class)))
                .thenThrow(new RuntimeException("Fallo de conexion de red"));

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-geografico"))
                .andExpect(jsonPath("$.detalle").value("Fallo de conexion de red"));
    }

    // =========================================================
    // GET /api/geografico/reporte/{idReporte} — exitoso
    // =========================================================
    @Test
    @DisplayName("GET /reporte/{id} - retorna ubicacion encontrada")
    void testObtenerPorReporte_Exitoso() throws Exception {
        Ubicacion encontrada = new Ubicacion();
        encontrada.setId(1L);
        encontrada.setIdReporte(10L);
        encontrada.setLatitud(-33.456);
        encontrada.setLongitud(-70.648);

        when(ubicacionService.obtenerPorReporte(10L)).thenReturn(encontrada);

        mockMvc.perform(get("/api/geografico/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idReporte").value(10))
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(ubicacionService, times(1)).obtenerPorReporte(10L);
    }

    // =========================================================
    // GET /api/geografico/reporte/{idReporte} — no encontrado → 404
    // =========================================================
    @Test
    @DisplayName("GET /reporte/{id} - reporte inexistente retorna 404")
    void testObtenerPorReporte_NoEncontrado() throws Exception {
        when(ubicacionService.obtenerPorReporte(99L))
                .thenThrow(new EntityNotFoundException(
                        "No se encontr\u00f3 ubicaci\u00f3n geogr\u00e1fica para el reporte con ID: 99"));

        mockMvc.perform(get("/api/geografico/reporte/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(
                        "No se encontr\u00f3 ubicaci\u00f3n geogr\u00e1fica para el reporte con ID: 99"));

        verify(ubicacionService, times(1)).obtenerPorReporte(99L);
    }
}