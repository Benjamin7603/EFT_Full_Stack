package com.duoc.ms_usuarios.repository;

import com.duoc.ms_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ✅ NUEVO: necesario para el login por username
    Optional<Usuario> findByUsername(String username);
}