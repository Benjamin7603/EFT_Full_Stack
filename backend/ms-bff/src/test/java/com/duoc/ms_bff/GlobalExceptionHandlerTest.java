package com.duoc.ms_bff.exception;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("FeignException con status HTTP conocido retorna ese mismo status")
    void testManejarFeignExceptionConStatusConocido() {
        FeignException feignEx = FeignException.errorStatus(
                "GET",
                feign.Response.builder()
                        .status(404)
                        .reason("Not Found")
                        .request(Request.create(
                                Request.HttpMethod.GET,
                                "/api/test",
                                Map.of(),
                                null,
                                StandardCharsets.UTF_8,
                                null))
                        .build()
        );

        ResponseEntity<Map<String, String>> response =
                handler.manejarErroresMicroservicios(feignEx);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Error en la comunicaci\u00f3n con los servicios municipales",
                response.getBody().get("error")
        );
        assertEquals(
                "El servicio interno respondi\u00f3 con estado: 404",
                response.getBody().get("detalle")
        );
    }

    @Test
    @DisplayName("FeignException con status inválido retorna BAD_GATEWAY")
    void testManejarFeignExceptionConStatusInvalido() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET,
                "/api/test",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        FeignException feignEx = new FeignException(
                -1,
                "sin status valido",
                dummyRequest,
                null,
                null
        ) {};

        ResponseEntity<Map<String, String>> response =
                handler.manejarErroresMicroservicios(feignEx);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Error en la comunicaci\u00f3n con los servicios municipales",
                response.getBody().get("error")
        );
        assertEquals(
                "El servicio interno respondi\u00f3 con estado: 502",
                response.getBody().get("detalle")
        );
    }

    @Test
    @DisplayName("Exception genérica retorna INTERNAL_SERVER_ERROR")
    void testManejarErrorGeneral() {
        Exception ex = new RuntimeException("Error inesperado de prueba");

        ResponseEntity<Map<String, String>> response =
                handler.manejarErrorGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Error inesperado en el servidor de agregaci\u00f3n (BFF)",
                response.getBody().get("mensaje")
        );
        assertEquals(
                "Error inesperado de prueba",
                response.getBody().get("detalle")
        );
    }
}