package com.duoc.ms_usuarios.service;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return usuarioRepository.findById(id).orElse(null);
    }

    // ✅ NUEVO: buscar por username para el login
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    /**
     * Al guardar un usuario nuevo, encriptamos su contraseña con BCrypt
     * antes de persistir en la BD.
     */
    public Usuario guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario usuario) {
        return usuarioRepository.findById(id).map(user -> {
            user.setNombre(usuario.getNombre());
            user.setApellido(usuario.getApellido());
            user.setEmail(usuario.getEmail());
            user.setRol(usuario.getRol());
            // Solo actualiza contraseña si viene en el body
            if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
            return usuarioRepository.save(user);
        }).orElse(null);
    }

    public boolean eliminar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}