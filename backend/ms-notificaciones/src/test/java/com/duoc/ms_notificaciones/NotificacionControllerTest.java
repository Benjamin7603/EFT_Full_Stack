package com.duoc.ms_notificaciones;


import com.duoc.ms_notificaciones.controller.NotificacionController;
import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.repository.NotificacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - NotificacionController")
class NotificacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionController notificacionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(notificacionController).build();
    }

    // =========================================================
    // POST /api/notificaciones/enviar
    // =========================================================
    @Test
    @DisplayName("Debe procesar el envio de alerta exitosamente y retornar HTTP 200")
    void testEnviarAlerta_Exitoso() throws Exception {
        Notificacion notificacionInput = new Notificacion();
        notificacionInput.setDestinatario("brigadista@incendios.cl");
        notificacionInput.setMensaje("Foco de incendio detectado en sector sur");

        Notificacion notificacionPersistida = new Notificacion();
        notificacionPersistida.setId(1L);
        notificacionPersistida.setDestinatario("brigadista@incendios.cl");
        notificacionPersistida.setMensaje("Foco de incendio detectado en sector sur");

        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionPersistida);

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificacionInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.destinatario").value("brigadista@incendios.cl"))
                .andExpect(jsonPath("$.mensaje").value("Foco de incendio detectado en sector sur"));

        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }
}
