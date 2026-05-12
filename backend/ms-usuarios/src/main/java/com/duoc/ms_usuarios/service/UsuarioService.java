package com.duoc.ms_usuarios.service;


import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;


    public UsuarioService(UsuarioRepository usuarioRepository){this.usuarioRepository=usuarioRepository;}

    public List<Usuario> listar(){return usuarioRepository.findAll();}
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
    public Usuario guardar(Usuario usuario){return usuarioRepository.save(usuario);}

    public Usuario actualizar(Long id, Usuario usuario) {
        return usuarioRepository.findById(id).map(user -> {
            user.setNombre(usuario.getNombre());
            user.setApellido(usuario.getApellido());
            user.setEmail(usuario.getEmail());
            user.setRol(usuario.getRol());
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
