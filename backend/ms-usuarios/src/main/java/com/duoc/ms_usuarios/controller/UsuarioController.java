package com.duoc.ms_usuarios.controller;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST con las operaciones CRUD para Usuario.
 * Integra validaciones de datos mediante {@link Valid}
 * y Swagger para la documentación de la API.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Endpoints de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Obtener listado de usuarios")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @Operation(summary = "Agregar un nuevo usuario")
    @PostMapping
    public Usuario agregar(@Valid @RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }

    @Operation(summary = "Actualizar un usuario existente")
    @PutMapping("/{id}")
    public Usuario actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdSesion,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        return usuarioService.actualizar(id, usuario, usuarioIdSesion, rolSesion);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdSesion
    ) {
        usuarioService.eliminar(id, usuarioIdSesion);
    }
}