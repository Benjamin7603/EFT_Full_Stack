package com.duoc.ms_geografico;

import com.duoc.ms_geografico.controller.GeograficoController;
import com.duoc.ms_geografico.exception.GlobalExceptionHandler;
import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.repository.UbicacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler (Calibrado 90%-95%)")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private UbicacionRepository ubicacionRepository;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() throws Exception {
        ubicacionRepository = mock(UbicacionRepository.class);
        exceptionHandler = new GlobalExceptionHandler();

        GeograficoController controllerReal = new GeograficoController();
        java.lang.reflect.Field field = GeograficoController.class.getDeclaredField("ubicacionRepository");
        field.setAccessible(true);
        field.set(controllerReal, ubicacionRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controllerReal)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    // =========================================================
    // MethodArgumentNotValidException
    // =========================================================
    @Test
    void testManejarValidaciones_Estructura() throws Exception {
        BindException bindException = new BindException(new Ubicacion(), "ubicacion");
        Method metodoReal = GeograficoController.class.getMethod("guardarUbicacion", Ubicacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodoReal, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> respuesta = exceptionHandler.manejarValidaciones(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    // =========================================================
    // EntityNotFoundException
    // =========================================================
    @Test
    void testManejarNoEncontrado() throws Exception {
        Ubicacion validadorDto = new Ubicacion();
        validadorDto.setIdReporte(10L);
        validadorDto.setLatitud(-33.456);
        validadorDto.setLongitud(-70.648);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonValido = mapper.writeValueAsString(validadorDto);

        when(ubicacionRepository.save(any(Ubicacion.class))).thenThrow(new EntityNotFoundException("Coordenada inexistente"));

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Coordenada inexistente"));
    }

    // =========================================================
    // Exception Genérica
    // =========================================================
    @Test
    void testManejarErrorGeneral() throws Exception {
        Ubicacion validadorDto = new Ubicacion();
        validadorDto.setIdReporte(10L);
        validadorDto.setLatitud(-33.456);
        validadorDto.setLongitud(-70.648);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonValido = mapper.writeValueAsString(validadorDto);

        when(ubicacionRepository.save(any(Ubicacion.class))).thenThrow(new RuntimeException("Fallo de conexion de red"));

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-geografico"))
                .andExpect(jsonPath("$.detalle").value("Fallo de conexion de red"));
    }
}