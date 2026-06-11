package com.duoc.ms_usuarios.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException; // <-- ¡NUEVO IMPORT!
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

// Solo escucha excepciones de NUESTROS controladores, no de Spring Security
@RestControllerAdvice(basePackages = "com.duoc.ms_usuarios.controller")
public class GlobalExceptionHandler {

    // 1. Errores de validación (@NotBlank, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }

    // 2. Entidad no encontrada
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 3. ¡NUEVO! Capturar correos y nombres de usuario duplicados
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarRestriccionUnica(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();

        // Buscamos si el error de la base de datos menciona la llave del correo o username
        if (ex.getMessage() != null && ex.getMessage().contains("usuarios_email_key")) {
            error.put("error", "Este correo electrónico ya está registrado en el sistema.");
        } else if (ex.getMessage() != null && ex.getMessage().contains("usuarios_username_key")) {
            error.put("error", "Este nombre de usuario ya está en uso.");
        } else {
            error.put("error", "Error: Un dato único ya existe en el sistema.");
        }

        // Retornamos 409 Conflict y la variable "error" que React espera leer
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    // 4. Errores generales — solo los de nuestro código
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error inesperado en el servidor");
        error.put("detalle", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}