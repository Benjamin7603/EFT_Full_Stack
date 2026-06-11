package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.config.AdminInitializer;
import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - AdminInitializer")
class AdminInitializerTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        adminInitializer = new AdminInitializer();
    }

    @Test
    @DisplayName("Debe crear usuario admin inicial cuando no existe")
    void crearAdminInicial_noExiste_creaUsuarioAdmin() throws Exception {
        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner =
                adminInitializer.crearAdminInicial(usuarioRepository, passwordEncoder);

        runner.run();

        verify(usuarioRepository, times(1)).findByUsername("admin");

        verify(usuarioRepository, times(1)).save(argThat(usuario ->
                usuario.getNombre().equals("Administrador") &&
                        usuario.getApellido().equals("GeoFire") &&
                        usuario.getEmail().equals("admin@geofire.cl") &&
                        usuario.getTelefono().equals("+56 9 0000 0000") &&
                        usuario.getUsername().equals("admin") &&
                        usuario.getRol().equals("ADMIN") &&
                        Boolean.TRUE.equals(usuario.getActivo()) &&
                        passwordEncoder.matches("admin123", usuario.getPassword())
        ));
    }

    @Test
    @DisplayName("No debe crear usuario admin inicial si ya existe")
    void crearAdminInicial_yaExiste_noCreaUsuarioAdmin() throws Exception {
        Usuario adminExistente = new Usuario();
        adminExistente.setId(1L);
        adminExistente.setNombre("Administrador");
        adminExistente.setApellido("GeoFire");
        adminExistente.setEmail("admin@geofire.cl");
        adminExistente.setUsername("admin");
        adminExistente.setRol("ADMIN");
        adminExistente.setActivo(true);

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminExistente));

        CommandLineRunner runner =
                adminInitializer.crearAdminInicial(usuarioRepository, passwordEncoder);

        runner.run();

        verify(usuarioRepository, times(1)).findByUsername("admin");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("El password del admin inicial debe quedar encriptado")
    void crearAdminInicial_passwordQuedaEncriptado() throws Exception {
        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner runner =
                adminInitializer.crearAdminInicial(usuarioRepository, passwordEncoder);

        runner.run();

        verify(usuarioRepository).save(argThat(usuario ->
                usuario.getPassword() != null &&
                        !usuario.getPassword().equals("admin123") &&
                        (
                                usuario.getPassword().startsWith("$2a$") ||
                                        usuario.getPassword().startsWith("$2b$") ||
                                        usuario.getPassword().startsWith("$2y$")
                        ) &&
                        passwordEncoder.matches("admin123", usuario.getPassword())
        ));
    }
}