package com.duoc.ms_usuarios.controller;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Obtener usuario actual")
    @GetMapping("/me")
    public Usuario obtenerPerfilActual(
            @RequestHeader("X-Usuario-Id") Long usuarioId
    ) {
        Usuario usuario = usuarioService.obtenerPorId(usuarioId);
        usuario.setPassword(null);
        return usuario;
    }

    @Operation(summary = "Obtener listado de usuarios")
    @GetMapping
    public ResponseEntity<List<Usuario>> listar(
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        if (rolSesion == null || !"ADMIN".equalsIgnoreCase(rolSesion)) {
            return ResponseEntity.status(403).build();
        }

        List<Usuario> usuarios = usuarioService.listar();

        usuarios.forEach(usuario -> usuario.setPassword(null));

        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Agregar un nuevo usuario")
    @PostMapping
    public Usuario agregar(
            @Valid @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        Usuario usuarioGuardado = usuarioService.guardar(usuario, rolSesion);
        usuarioGuardado.setPassword(null);
        return usuarioGuardado;
    }

    @Operation(summary = "Actualizar un usuario existente")
    @PutMapping("/{id}")
    public Usuario actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdSesion,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        Usuario usuarioActualizado = usuarioService.actualizar(id, usuario, usuarioIdSesion, rolSesion);
        usuarioActualizado.setPassword(null);
        return usuarioActualizado;
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdSesion
    ) {
        usuarioService.eliminar(id, usuarioIdSesion);
    }

    @Operation(summary = "Agregar un nuevo usuario desde panel administrador")
    @PostMapping("/admin")
    public Usuario agregarDesdeAdmin(
            @Valid @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        if (rolSesion == null || !"ADMIN".equalsIgnoreCase(rolSesion)) {
            throw new IllegalArgumentException("Solo un administrador puede crear usuarios con rol personalizado.");
        }

        Usuario usuarioGuardado = usuarioService.guardar(usuario, rolSesion);
        usuarioGuardado.setPassword(null);
        return usuarioGuardado;
    }
}