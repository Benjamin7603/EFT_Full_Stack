package com.duoc.ms_usuarios.controller;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
/**
 * Controlador REST con las operaciones CRUD para Usuario.
 * Integra validaciones de datos mediante {@link Valid}
 * Swagger para la documentación de la API.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Endpoints de usuarios")
public class UsuarioController {
    /**
     * Servicio inyectado automáticamente por Spring para
     * delegar las operaciones de persistencia, validación y
     * control de acceso sobre la entidad Usuario.
     */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Recupera todos los usuarios registrados en el sistema.
     * @return {@link List} que contiene todos los objetos {@link Usuario}.
     */
    @Operation(summary = "Obtener listado de usuarios")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     * @param usuario Objeto {@link Usuario} que contiene los datos del registro.
     * Se valida mediante la anotación {@link Valid}.
     * @return El objeto {@link Usuario} guardado, incluyendo su ID.
     */
    @Operation(summary = "Agregar un nuevo usuario")
    @PostMapping
    public Usuario agregar(@Valid @RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }

    /**
     * Actualiza la información de un usuario existente basándose en su ID.
     * @param id Identificador único del usuario a modificar.
     * @param usuario Objeto con las nuevas propiedades a asignar en la entidad destino.
     * @return Objeto {@link Usuario} modificado con los nuevos datos guardados,
     * o {@code null} si el ID buscado no existe.
     */
    @Operation(summary = "Actualizar un usuario existente")
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.actualizar(id, usuario);
    }

    /**
     * Remueve de permanentemente un usuario del sistema mediante su ID.
     * * @param id Identificador único del usuario a eliminar.
     */
    @Operation(summary = "Eliminar un usuario por ID")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}