package com.duoc.ms_usuarios.exception;



import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//Maneja errores globales
@RestControllerAdvice
public class GlobalExceptionHandler {
    //Capturar errores
    @ExceptionHandler(MethodArgumentNotValidException.class)

    //se ejecuta cuando ocurra algun error al validar
    public Map<String, String> manejarValidaciones(MethodArgumentNotValidException ex){
        //
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error)->{
            errores.put(error.getField(), error.getDefaultMessage());
        });
        return errores;
    }

    //Capturar cualquier tipo de error no identificado
    @ExceptionHandler(Exception.class)
    public Map<String, String> manejarErrorGeneral(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error inesperado");
        return error;
    }

}

