package com.duoc.ms_geografico;

import com.duoc.ms_geografico.controller.GeograficoController;
import com.duoc.ms_geografico.exception.GlobalExceptionHandler;
import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.repository.UbicacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - GeograficoController")
class GeograficoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UbicacionRepository ubicacionRepository;

    @InjectMocks
    private GeograficoController graficoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(graficoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================
    // POST /api/geografico/guardar
    // =========================================================
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

        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacionPersistida);

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ubicacionInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idReporte").value(10))
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(ubicacionRepository, times(1)).save(any(Ubicacion.class));
    }

    // =========================================================
    // GET /api/geografico/reporte/{idReporte} - Exitoso
    // =========================================================
    @Test
    void testObtenerPorReporte_Exitoso() throws Exception {
        Ubicacion ubicacionEncontrada = new Ubicacion();
        ubicacionEncontrada.setId(1L);
        ubicacionEncontrada.setIdReporte(10L);
        ubicacionEncontrada.setLatitud(-33.456);
        ubicacionEncontrada.setLongitud(-70.648);

        when(ubicacionRepository.findByIdReporte(10L)).thenReturn(Optional.of(ubicacionEncontrada));

        mockMvc.perform(get("/api/geografico/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idReporte").value(10))
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(ubicacionRepository, times(1)).findByIdReporte(10L);
    }

    // =========================================================
    // GET /api/geografico/reporte/{idReporte} - No Encontrado
    // =========================================================
    @Test
    void testObtenerPorReporte_NoEncontrado() throws Exception {
        when(ubicacionRepository.findByIdReporte(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/geografico/reporte/99"))
                .andExpect(status().isNotFound());

        verify(ubicacionRepository, times(1)).findByIdReporte(99L);
    }
}