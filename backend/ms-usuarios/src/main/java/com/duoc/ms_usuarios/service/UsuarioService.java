package com.duoc.ms_usuarios.service;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service para la gestión de usuarios.
 * Se encarga de las operaciones CRUD del microservicio, la búsqueda de credenciales
 * y el procesamiento seguro de contraseñas mediante encriptación.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return obtenerPorId(id);
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con username: " + username));
    }

    public Usuario guardar(Usuario usuario) {
        return guardar(usuario, null);
    }

    public Usuario guardar(Usuario usuario, String rolSesion) {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        if (esAdmin(rolSesion)) {
            String rolSolicitado = normalizarRol(usuario.getRol());

            if (rolSolicitado == null || rolSolicitado.isBlank()) {
                usuario.setRol("USER");
            } else {
                usuario.setRol(rolSolicitado);
            }
        } else {
            usuario.setRol("USER");
        }

        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario usuario) {
        return actualizar(id, usuario, null, null);
    }

    public Usuario actualizar(Long id, Usuario usuario, Long usuarioIdSesion, String rolSesion) {
        Usuario usuarioActual = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        boolean esAdminSesion = esAdmin(rolSesion);
        boolean esMismoUsuario = usuarioIdSesion != null && usuarioIdSesion.equals(id);

        if (!esAdminSesion && !esMismoUsuario) {
            throw new IllegalArgumentException("No tienes permisos para actualizar este usuario.");
        }

        String rolActual = normalizarRol(usuarioActual.getRol());
        String nuevoRolSolicitado = normalizarRol(usuario.getRol());

        if (!esAdminSesion) {
            nuevoRolSolicitado = rolActual;
        }

        if (esMismoUsuario && esAdmin(rolActual) && !esAdmin(nuevoRolSolicitado)) {
            throw new IllegalArgumentException("No puedes quitarte el rol ADMIN a tu propia cuenta.");
        }

        if (esAdmin(rolActual) && !esAdmin(nuevoRolSolicitado) && usuarioRepository.countByRol("ADMIN") <= 1) {
            throw new IllegalArgumentException("No puedes quitar el rol ADMIN al último administrador del sistema.");
        }

        usuarioActual.setNombre(usuario.getNombre());
        usuarioActual.setApellido(usuario.getApellido());
        usuarioActual.setEmail(usuario.getEmail());
        usuarioActual.setTelefono(usuario.getTelefono());

        if (nuevoRolSolicitado != null && !nuevoRolSolicitado.isBlank()) {
            usuarioActual.setRol(nuevoRolSolicitado);
        }

        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuarioActual.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        return usuarioRepository.save(usuarioActual);
    }

    public boolean eliminar(Long id) {
        return eliminar(id, null);
    }

    public boolean eliminar(Long id, Long usuarioIdSesion) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuarioIdSesion != null && usuarioIdSesion.equals(id)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }

        if (esAdmin(usuario.getRol()) && usuarioRepository.countByRol("ADMIN") <= 1) {
            throw new IllegalArgumentException("No puedes eliminar al último administrador del sistema.");
        }

        usuarioRepository.delete(usuario);
        return true;
    }

    private boolean esAdmin(String rol) {
        return "ADMIN".equalsIgnoreCase(rol);
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return null;
        }

        return rol.trim().toUpperCase();
    }
}