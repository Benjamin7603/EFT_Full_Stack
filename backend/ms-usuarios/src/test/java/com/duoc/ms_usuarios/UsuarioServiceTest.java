package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.repository.UsuarioRepository;
import com.duoc.ms_usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        usuarioBase.setUsername("juan123");
        usuarioBase.setPassword("miPasswordSegura125");
        usuarioBase.setRol("USER");
    }

    // =========================================================
    // 1. PRUEBAS PARA: listar()
    // =========================================================

    @Test
    @DisplayName("Debería retornar una lista con todos los usuarios")
    void testListar_retornaUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioBase));

        List<Usuario> resultado = usuarioService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("juan123", resultado.get(0).getUsername());
    }

    // =========================================================
    // 2. PRUEBAS PARA: buscarPorId()
    // =========================================================

    @Test
    @DisplayName("Buscar ID existente debe retornar el Usuario")
    void testBuscarPorId_encontrado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Buscar ID inexistente debe retornar null (Camino .orElse(null))")
    void testBuscarPorId_noEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.buscarPorId(99L);

        assertNull(resultado);
    }

    // =========================================================
    // 3. PRUEBAS PARA: buscarPorUsername()
    // =========================================================

    @Test
    @DisplayName("Buscar Username existente debe retornar el Usuario")
    void testBuscarPorUsername_encontrado() {
        when(usuarioRepository.findByUsername("juan123")).thenReturn(Optional.of(usuarioBase));

        Usuario resultado = usuarioService.buscarPorUsername("juan123");

        assertNotNull(resultado);
        assertEquals("juan123", resultado.getUsername());
    }

    @Test
    @DisplayName("Buscar Username inexistente debe retornar null")
    void testBuscarPorUsername_noEncontrado() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.buscarPorUsername("fantasma");

        assertNull(resultado);
    }

    // =========================================================
    // 4. PRUEBAS PARA: guardar()
    // =========================================================

    @Test
    @DisplayName("Guardar usuario con password válido debe encriptarla con BCrypt")
    void testGuardar_conPassword_encriptaYGuarda() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioBase);

        assertNotNull(guardado);
        assertTrue(guardado.getPassword().startsWith("$2a$") || guardado.getPassword().startsWith("$2b$"));
        assertTrue(passwordEncoder.matches("miPasswordSegura125", guardado.getPassword()));
    }

    @Test
    @DisplayName("Guardar usuario con password vacía o nula no debe encriptar")
    void testGuardar_passwordVacia_noEncripta() {
        usuarioBase.setPassword("   ");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuarioBase);

        assertEquals("   ", guardado.getPassword());
    }

    // =========================================================
    // 5. PRUEBAS PARA: actualizar()
    // =========================================================

    @Test
    @DisplayName("Actualizar usuario existente modificando password debe guardarlo encriptado")
    void testActualizar_usuarioExiste_conNuevaPassword() {
        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Carlos");
        datosNuevos.setApellido("Soto");
        datosNuevos.setEmail("carlos@mail.com");
        datosNuevos.setRol("ADMIN");
        datosNuevos.setPassword("nuevaClave");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario modificado = usuarioService.actualizar(1L, datosNuevos);

        assertNotNull(modificado);
        assertEquals("Carlos", modificado.getNombre());
        assertEquals("ADMIN", modificado.getRol());
        assertTrue(passwordEncoder.matches("nuevaClave", modificado.getPassword()));
    }

    @Test
    @DisplayName("Actualizar usuario existente SIN cambiar password mantiene la original")
    void testActualizar_usuarioExiste_sinPassword() {
        Usuario datosNuevos = new Usuario();
        datosNuevos.setNombre("Carlos");
        datosNuevos.setPassword("");

        String passwordOriginal = usuarioBase.getPassword();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario modificado = usuarioService.actualizar(1L, datosNuevos);

        assertEquals(passwordOriginal, modificado.getPassword());
    }

    @Test
    @DisplayName("Actualizar usuario que no existe debe retornar null")
    void testActualizar_usuarioNoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        Usuario modificado = usuarioService.actualizar(1L, usuarioBase);

        assertNull(modificado);
    }

    // =========================================================
    // 6. PRUEBAS PARA: eliminar()
    // =========================================================

    @Test
    @DisplayName("Eliminar usuario que existe debe retornar true y borrar")
    void testEliminar_existe_retornaTrue() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        boolean eliminado = usuarioService.eliminar(1L);

        assertTrue(eliminado);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar usuario que NO existe debe retornar false")
    void testEliminar_noExiste_retornaFalse() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        boolean eliminado = usuarioService.eliminar(99L);

        assertFalse(eliminado);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}
