package com.duoc.ms_usuarios.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Capturar errores de validación (el que ya tenías)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> manejarValidaciones(MethodArgumentNotValidException ex){
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error)->{
            errores.put(error.getField(), error.getDefaultMessage());
        });
        return errores;
    }

    // 2. CAPTURAR ERRORES GENERALES (Descomentado y mejorado)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error inesperado en el servidor");
        error.put("detalle", ex.getMessage());

        // Usamos ResponseEntity para devolver un error 500 real
        // Esto ayuda a que Swagger sepa que algo salió mal y no se confunda
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}