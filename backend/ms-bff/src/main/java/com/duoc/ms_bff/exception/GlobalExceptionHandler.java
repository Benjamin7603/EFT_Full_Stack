package com.duoc.ms_bff.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.duoc.ms_bff.controller")
public class GlobalExceptionHandler {

    /**
     * Captura los errores generados por Feign al comunicarse con los microservicios.
     * Si ms-reportes o ms-geografico devuelven un 404, este método lo intercepta
     * y le reenvía el mismo 404 limpio a la app de React.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> manejarErroresMicroservicios(FeignException ex) {
        Map<String, String> error = new HashMap<>();

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }

        error.put("error", "Error en la comunicación con los servicios municipales");
        error.put("detalle", "El servicio interno respondió con estado: " + status.value());

        return new ResponseEntity<>(error, status);
    }

    /**
     * Captura cualquier otro error inesperado dentro del código del propio BFF.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error inesperado en el servidor de agregación (BFF)");
        error.put("detalle", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}