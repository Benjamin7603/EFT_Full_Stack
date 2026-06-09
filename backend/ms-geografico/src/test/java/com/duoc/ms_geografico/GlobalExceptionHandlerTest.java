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

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private UbicacionService ubicacionService;
    private GlobalExceptionHandler exceptionHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ubicacionService = mock(UbicacionService.class);
        exceptionHandler = new GlobalExceptionHandler();
        objectMapper = new ObjectMapper();

        GeograficoController controller = new GeograficoController(ubicacionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void testManejarValidaciones_Estructura() throws Exception {
        BindException bindException = new BindException(new Ubicacion(), "ubicacion");

        Method metodoReal = GeograficoController.class.getMethod("guardarUbicacion", Ubicacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodoReal, 0);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarValidaciones(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void testManejarNoEncontrado() throws Exception {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setIdReporte(10L);
        ubicacion.setLatitud(-33.456);
        ubicacion.setLongitud(-70.648);

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class)))
                .thenThrow(new EntityNotFoundException("Coordenada inexistente"));

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ubicacion)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Coordenada inexistente"));
    }

    @Test
    void testManejarErrorGeneral() throws Exception {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setIdReporte(10L);
        ubicacion.setLatitud(-33.456);
        ubicacion.setLongitud(-70.648);

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class)))
                .thenThrow(new RuntimeException("Fallo de conexion de red"));

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ubicacion)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-geografico"))
                .andExpect(jsonPath("$.detalle").value("Fallo de conexion de red"));
    }
}