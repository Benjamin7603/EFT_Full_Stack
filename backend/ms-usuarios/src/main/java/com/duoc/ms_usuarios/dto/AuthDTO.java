package com.duoc.ms_usuarios.dto;

// Usar como clase interna estática para no crear dos archivos separados

public class AuthDTO {

    public record LoginRequest(String username, String password) {}

    public record LoginResponse(
            String token,
            String username,
            String rol,
            Long usuarioId,
            String nombre
    ) {}
}