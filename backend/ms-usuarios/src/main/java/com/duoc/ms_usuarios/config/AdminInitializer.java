package com.duoc.ms_usuarios.config;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner crearAdminInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            boolean existeAdminInicial = usuarioRepository.findByUsername("admin").isPresent();

            if (!existeAdminInicial) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("GeoFire");
                admin.setEmail("admin@geofire.cl");
                admin.setTelefono("+56 9 0000 0000");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol("ADMIN");
                admin.setActivo(true);

                usuarioRepository.save(admin);

                System.out.println("Usuario administrador inicial creado: admin / admin123");
            } else {
                System.out.println("Usuario administrador inicial ya existe. No se creó nuevamente.");
            }
        };
    }
}