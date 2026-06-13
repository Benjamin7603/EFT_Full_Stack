package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import com.duoc.ms_usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - UsuarioService")
class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;
    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);

        usuarioBase = new Usuario();
        usuarioBase.setId(1L);
        usuarioBase.setNombre("Juan");
        usuarioBase.setApellido("Pérez");
        usuarioBase.setEmail("juan@mail.com");
        usuarioBase.setTelefono("+56 9 1234 5678");
        usuarioBase.setUsername("juan123");
        usuarioBase.setPassword("miPasswordSegura125");
        usuarioBase.setRol("USER");
        usuarioBase.setActivo(true);
    }

    @Test
    @DisplayName("listar() debe retornar todos los usuarios")
    void testListar_retornaUsuarios() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuarioBase));

        List<Usuario> resultado = usuarioService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("juan123", resultado.get(0).getUsername());

        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("buscarPorId() con ID existente debe retornar usuario")
    void testBuscarPorId_encontrado() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("buscarPorId() con ID inexistente debe lanzar EntityNotFoundException")
    void testBuscarPorId_noEncontrado() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                usuarioService.buscarPorId(99L)
        );
    }

    @Test
    @DisplayName("buscarPorUsername() existente debe retornar usuario")
    void testBuscarPorUsername_encontrado() {
        when(usuarioRepository.findByUsername("juan123"))
                .thenReturn(Optional.of(usuarioBase));

        Usuario resultado = usuarioService.buscarPorUsername("juan123");

        assertNotNull(resultado);
        assertEquals("juan123", resultado.getUsername());
    }

    @Test
    @DisplayName("buscarPorUsername() inexistente debe lanzar EntityNotFoundException")
    void testBuscarPorUsername_noEncontrado() {
        when(usuarioRepository.findByUsername("fantasma"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                usuarioService.buscarPorUsername("fantasma")
        );
    }

    @Test
    @DisplayName("guardar() con password válido debe encriptar y guardar")
    void testGuardar_conPassword_encriptaYGuarda() {
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioBase);

        assertNotNull(guardado);
        assertTrue(
                guardado.getPassword().startsWith("$2a$") ||
                        guardado.getPassword().startsWith("$2b$")
        );
        assertTrue(passwordEncoder.matches("miPasswordSegura125", guardado.getPassword()));
        assertEquals("USER", guardado.getRol());
        assertTrue(guardado.getActivo());
    }

    @Test
    @DisplayName("guardar() con password vacía no debe encriptar")
    void testGuardar_passwordVacia_noEncripta() {
        usuarioBase.setPassword("   ");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioBase);

        assertEquals("   ", guardado.getPassword());
    }

    @Test
    @DisplayName("guardar() asigna rol USER y activo true si vienen nulos")
    void testGuardar_asignaDefaults() {
        usuarioBase.setRol(null);
        usuarioBase.setActivo(null);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioBase);

        assertEquals("USER", guardado.getRol());
        assertTrue(guardado.getActivo());
    }

    @Test
    @DisplayName("actualizar() como ADMIN debe modificar datos, teléfono, rol y password")
    void testActualizar_admin_conNuevaPassword() {
        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Carlos");
        datosNuevos.setApellido("Soto");
        datosNuevos.setEmail("carlos@mail.com");
        datosNuevos.setTelefono("+56 9 9999 9999");
        datosNuevos.setRol("ADMIN");
        datosNuevos.setPassword("nuevaClave");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario modificado = usuarioService.actualizar(1L, datosNuevos, 99L, "ADMIN");

        assertNotNull(modificado);
        assertEquals("Carlos", modificado.getNombre());
        assertEquals("Soto", modificado.getApellido());
        assertEquals("carlos@mail.com", modificado.getEmail());
        assertEquals("+56 9 9999 9999", modificado.getTelefono());
        assertEquals("ADMIN", modificado.getRol());
        assertTrue(passwordEncoder.matches("nuevaClave", modificado.getPassword()));
    }
    @Test
    @DisplayName("obtenerPorId() con ID existente debe retornar usuario")
    void testObtenerPorId_encontrado() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        Usuario resultado = usuarioService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("juan123", resultado.getUsername());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("obtenerPorId() con ID inexistente debe lanzar EntityNotFoundException")
    void testObtenerPorId_noEncontrado() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                usuarioService.obtenerPorId(99L)
        );

        verify(usuarioRepository, times(1)).findById(99L);
    }
    @Test
    @DisplayName("actualizar() sin password mantiene la password original")
    void testActualizar_sinPassword_mantieneOriginal() {
        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Carlos");
        datosNuevos.setApellido("Soto");
        datosNuevos.setEmail("carlos@mail.com");
        datosNuevos.setTelefono("+56 9 1111 1111");
        datosNuevos.setRol("USER");
        datosNuevos.setPassword("");

        String passwordOriginal = usuarioBase.getPassword();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario modificado = usuarioService.actualizar(1L, datosNuevos, 99L, "ADMIN");

        assertEquals(passwordOriginal, modificado.getPassword());
        assertEquals("Carlos", modificado.getNombre());
    }

    @Test
    @DisplayName("actualizar() usuario común solo puede actualizar su propia cuenta y no cambia rol")
    void testActualizar_usuarioComun_suPropiaCuenta_noCambiaRol() {
        usuarioBase.setRol("USER");

        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Juan Actualizado");
        datosNuevos.setApellido("Pérez");
        datosNuevos.setEmail("nuevo@mail.com");
        datosNuevos.setTelefono("+56 9 2222 2222");
        datosNuevos.setRol("ADMIN");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario modificado = usuarioService.actualizar(1L, datosNuevos, 1L, "USER");

        assertEquals("Juan Actualizado", modificado.getNombre());
        assertEquals("USER", modificado.getRol());
    }

    @Test
    @DisplayName("actualizar() usuario común no puede actualizar otra cuenta")
    void testActualizar_usuarioComun_otraCuenta_lanzaError() {
        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Otro");
        datosNuevos.setRol("USER");

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuarioBase));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.actualizar(2L, datosNuevos, 1L, "USER")
        );

        assertEquals("No tienes permisos para actualizar este usuario.", ex.getMessage());
    }

    @Test
    @DisplayName("actualizar() no permite quitarse ADMIN a sí mismo")
    void testActualizar_adminNoPuedeQuitarseSuRol() {
        usuarioBase.setRol("ADMIN");

        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Admin");
        datosNuevos.setApellido("Principal");
        datosNuevos.setEmail("admin@mail.com");
        datosNuevos.setRol("USER");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.actualizar(1L, datosNuevos, 1L, "ADMIN")
        );

        assertEquals("No puedes quitarte el rol ADMIN a tu propia cuenta.", ex.getMessage());
    }

    @Test
    @DisplayName("actualizar() no permite quitar ADMIN al último administrador")
    void testActualizar_noPuedeQuitarUltimoAdmin() {
        usuarioBase.setRol("ADMIN");

        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Admin");
        datosNuevos.setApellido("Principal");
        datosNuevos.setEmail("admin@mail.com");
        datosNuevos.setRol("USER");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        when(usuarioRepository.countByRol("ADMIN"))
                .thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.actualizar(1L, datosNuevos, 99L, "ADMIN")
        );

        assertEquals("No puedes quitar el rol ADMIN al último administrador del sistema.", ex.getMessage());
    }

    @Test
    @DisplayName("actualizar() usuario inexistente debe lanzar EntityNotFoundException")
    void testActualizar_usuarioNoExiste() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                usuarioService.actualizar(1L, usuarioBase, 99L, "ADMIN")
        );
    }

    @Test
    @DisplayName("eliminar() usuario existente debe retornar true")
    void testEliminar_existe_retornaTrue() {
        Usuario usuarioAEliminar = new Usuario();
        usuarioAEliminar.setId(2L);
        usuarioAEliminar.setRol("USER");

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuarioAEliminar));

        boolean eliminado = usuarioService.eliminar(2L, 1L);

        assertTrue(eliminado);
        verify(usuarioRepository, times(1)).delete(usuarioAEliminar);
    }

    @Test
    @DisplayName("eliminar() no permite eliminar la propia cuenta")
    void testEliminar_propiaCuenta_lanzaError() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioBase));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.eliminar(1L, 1L)
        );

        assertEquals("No puedes eliminar tu propia cuenta.", ex.getMessage());
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("eliminar() no permite eliminar al último administrador")
    void testEliminar_ultimoAdmin_lanzaError() {
        Usuario admin = new Usuario();
        admin.setId(2L);
        admin.setRol("ADMIN");

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(admin));

        when(usuarioRepository.countByRol("ADMIN"))
                .thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.eliminar(2L, 1L)
        );

        assertEquals("No puedes eliminar al último administrador del sistema.", ex.getMessage());
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("eliminar() usuario inexistente debe lanzar EntityNotFoundException")
    void testEliminar_noExiste_lanzaError() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                usuarioService.eliminar(99L, 1L)
        );

        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }
}