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
    /**
     * Constructor para la inyección de dependencias requeridas por el servicio.
     * @param usuarioRepository Repositorio para la persistencia de datos de usuario.
     * @param passwordEncoder Componente para el cifrado seguro de contraseñas.
     */
    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    /**
     * Obtiene la lista completa de usuarios.
     * @return Lista de objetos {@link Usuario}.
     */
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
    /**
     * Busca un usuario específico utilizando el id único para buscarlo.
     * @param id Identificador único del usuario.
     * @return Objeto {@link Usuario} encontrado, o {@code null} si no existe.
     */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Busca un usuario en la base de datos mediante su nombre de usuario (username).
     * Este método es usado para procesos de autenticación y login.
     * @param username Nombre de usuario a consultar.
     * @return Objeto {@link Usuario} encontrado, o {@code null} si no se encuentra.
     */
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuario no encontrado con username: " + username));
    }

    /**
     * Registra un nuevo usuario aplicando passwordEncoder para encriptar la contraseña.
     * Antes de guardar el usuario, verifica si se proporcionó una contraseña para encriptarla con BCrypt.
     * @param usuario Objeto {@link Usuario} con los datos a registrar.
     * @return El usuario guardado con su ID generado.
     */
    public Usuario guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }
    /**
     * Modifica los datos de un usuario buscado con su ID.
     * Permite la actualización de atributos y actualiza la contraseña
     * encriptada únicamente si se envía un nuevo valor en la petición.
     * @param id Identificador del usuario que se desea modificar.
     * @param usuario Objeto con los nuevos datos a aplicar.
     * @return Objeto {@link Usuario} actualizado y guardado, o {@code null} si el ID no existe.
     */
    public Usuario actualizar(Long id, Usuario usuario) {
        return usuarioRepository.findById(id).map(user -> {
            user.setNombre(usuario.getNombre());
            user.setApellido(usuario.getApellido());
            user.setEmail(usuario.getEmail());
            user.setRol(usuario.getRol());
            if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
            return usuarioRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }
    /**
     * Elimina un usuario buscado con su ID, validando su existencia.
     * @param id Identificador único del usuario a eliminar.
     * @return {@code true} si el usuario existía y fue removido exitosamente; {@code false} en caso contrario.
     */
    public boolean eliminar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}