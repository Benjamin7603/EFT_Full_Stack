package com.duoc.ms_usuarios.controller;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import com.duoc.ms_usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Endpoints de usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Obtener listado de usuarios")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @Operation(summary = "Agregar un nuevo usuario")
    @PostMapping
    public Usuario agregar(@RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }

    @Operation(summary = "Actualizar un usuario existente")
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.actualizar(id, usuario);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
