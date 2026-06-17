package com.duoc.ms_bff;

import com.duoc.ms_bff.client.GeograficoClient;
import com.duoc.ms_bff.client.ReportesClient;
import com.duoc.ms_bff.controller.BffController;
import com.duoc.ms_bff.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - BffController + GlobalExceptionHandler + FeignConfig")
class BffControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportesClient reportesClient;

    @Mock
    private GeograficoClient geograficoClient;

    @InjectMocks
    private BffController bffController;

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders
                .standaloneSetup(bffController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
        RequestContextHolder.resetRequestAttributes();
    }

    // =========================================================
    // GET /bff/estado
    // =========================================================
    @Test
    @DisplayName("GET /bff/estado - retorna mensaje de estado")
    void testEstado() throws Exception {
        mockMvc.perform(get("/bff/estado")
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BFF funcionando correctamente")));
    }

    // =========================================================
    // GET /bff/reportes
    // =========================================================
    @Test
    @DisplayName("GET /bff/reportes - retorna lista de reportes")
    void testObtenerReportes() throws Exception {
        List<Map<String, Object>> mockReportes = List.of(
                Map.of("id", 1, "descripcion", "Incendio forestal activo")
        );
        when(reportesClient.obtenerReportes()).thenReturn(mockReportes);

        mockMvc.perform(get("/bff/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].descripcion").value("Incendio forestal activo"));

        verify(reportesClient, times(1)).obtenerReportes();
    }

    // =========================================================
    // GET /bff/geografico/reporte/{idReporte}
    // =========================================================
    @Test
    @DisplayName("GET /bff/geografico/reporte/{id} - retorna ubicacion por reporte")
    void testObtenerUbicacionPorReporte() throws Exception {
        Map<String, Object> mockUbicacion = Map.of("latitud", -33.456, "longitud", -70.648);
        when(geograficoClient.obtenerUbicacionPorReporte(10L)).thenReturn(mockUbicacion);

        mockMvc.perform(get("/bff/geografico/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(10L);
    }

    // =========================================================
    // POST /bff/reportar-incendio
    // =========================================================
    @Test
    @DisplayName("POST /bff/reportar-incendio - crea reporte exitosamente")
    void testReportarIncendio() throws Exception {
        Map<String, Object> nuevoReporte = Map.of("severidad", "ALTA", "descripcion", "Foco norte");
        Map<String, Object> respuesta = Map.of("id", 99, "severidad", "ALTA");

        when(reportesClient.crearReporte(any())).thenReturn(respuesta);

        mockMvc.perform(post("/bff/reportar-incendio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoReporte)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.severidad").value("ALTA"));

        verify(reportesClient, times(1)).crearReporte(any());
    }

    // =========================================================
    // GET /bff/incendio/{id} — ambos servicios OK
    // =========================================================
    @Test
    @DisplayName("GET /bff/incendio/{id} - retorna reporte y ubicacion combinados")
    void testObtenerIncendioCompleto() throws Exception {
        Map<String, Object> mockReporte = Map.of("id", 5, "severidad", "ALTA");
        Map<String, Object> mockUbicacion = Map.of("latitud", -36.826, "longitud", -73.049);

        when(reportesClient.obtenerReportePorId(5L)).thenReturn(mockReporte);
        when(geograficoClient.obtenerUbicacionPorReporte(5L)).thenReturn(mockUbicacion);

        mockMvc.perform(get("/bff/incendio/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporte.id").value(5))
                .andExpect(jsonPath("$.reporte.severidad").value("ALTA"))
                .andExpect(jsonPath("$.ubicacion.latitud").value(-36.826))
                .andExpect(jsonPath("$.ubicacion.longitud").value(-73.049));

        verify(reportesClient, times(1)).obtenerReportePorId(5L);
        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(5L);
    }

    // =========================================================
    // GET /bff/incendio/{id} — geografico falla → fallback amigable
    // =========================================================
    @Test
    @DisplayName("GET /bff/incendio/{id} - retorna alerta cuando geografico falla")
    void testObtenerIncendioCompletoConFalloGeografico() throws Exception {
        Map<String, Object> mockReporte = Map.of("id", 7, "severidad", "MEDIA");

        when(reportesClient.obtenerReportePorId(7L)).thenReturn(mockReporte);
        when(geograficoClient.obtenerUbicacionPorReporte(7L))
                .thenThrow(new RuntimeException("MS-Geografico no disponible"));

        mockMvc.perform(get("/bff/incendio/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporte.id").value(7))
                .andExpect(jsonPath("$.reporte.severidad").value("MEDIA"))
                .andExpect(jsonPath("$.ubicacion.alerta")
                        .value("Ubicaci\u00f3n temporalmente no disponible"));

        verify(reportesClient, times(1)).obtenerReportePorId(7L);
        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(7L);
    }

    // =========================================================
    // GlobalExceptionHandler — FeignException con status HTTP conocido (404)
    // =========================================================
    @Test
    @DisplayName("GlobalExceptionHandler - FeignException con status 404 conocido")
    void testManejarFeignExceptionConStatusConocido() {
        FeignException feignEx = FeignException.errorStatus(
                "GET",
                feign.Response.builder()
                        .status(404)
                        .reason("Not Found")
                        .request(Request.create(
                                Request.HttpMethod.GET,
                                "/api/test",
                                Map.of(),
                                null,
                                StandardCharsets.UTF_8,
                                null))
                        .build()
        );

        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.manejarErroresMicroservicios(feignEx);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error en la comunicaci\u00f3n con los servicios municipales",
                response.getBody().get("error"));
        assertTrue(response.getBody().get("detalle").contains("404"));
    }

    // =========================================================
    // GlobalExceptionHandler — FeignException con status -1 → null → BAD_GATEWAY
    // =========================================================
    @Test
    @DisplayName("GlobalExceptionHandler - FeignException con status invalido usa BAD_GATEWAY")
    void testManejarFeignExceptionConStatusInvalido() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET,
                "/api/test",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null);

        // Subclase anónima que devuelve -1 para forzar HttpStatus.resolve() → null
        FeignException feignEx = new FeignException(-1, "sin status valido", dummyRequest, null, null) {};

        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.manejarErroresMicroservicios(feignEx);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    // =========================================================
    // GlobalExceptionHandler — Exception generica → 500
    // =========================================================
    @Test
    @DisplayName("GlobalExceptionHandler - Exception generica retorna 500")
    void testManejarErrorGeneral() {
        Exception ex = new RuntimeException("Error inesperado de prueba");

        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.manejarErrorGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error inesperado en el servidor de agregaci\u00f3n (BFF)",
                response.getBody().get("mensaje"));
        assertEquals("Error inesperado de prueba", response.getBody().get("detalle"));
    }

    // =========================================================
    // FeignConfig — interceptor con Bearer token presente
    // =========================================================
    @Test
    @DisplayName("FeignConfig - interceptor propaga Authorization con Bearer token")
    void testFeignConfigConJwt() {
        com.duoc.ms_bff.config.FeignConfig feignConfig = new com.duoc.ms_bff.config.FeignConfig();
        feign.RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer token.jwt.valido");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        feign.RequestTemplate template = new feign.RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertTrue(template.headers().get("Authorization").contains("Bearer token.jwt.valido"));

        RequestContextHolder.resetRequestAttributes();
    }

    // =========================================================
    // FeignConfig — interceptor sin header Authorization
    // =========================================================
    @Test
    @DisplayName("FeignConfig - interceptor no agrega Authorization si no hay token")
    void testFeignConfigSinJwt() {
        com.duoc.ms_bff.config.FeignConfig feignConfig = new com.duoc.ms_bff.config.FeignConfig();
        feign.RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        feign.RequestTemplate template = new feign.RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Authorization"));

        RequestContextHolder.resetRequestAttributes();
    }

    // =========================================================
    // FeignConfig — interceptor sin RequestAttributes (attributes == null)
    // =========================================================
    @Test
    @DisplayName("FeignConfig - interceptor no falla si no hay contexto de request")
    void testFeignConfigSinRequestAttributes() {
        com.duoc.ms_bff.config.FeignConfig feignConfig = new com.duoc.ms_bff.config.FeignConfig();
        feign.RequestInterceptor interceptor = feignConfig.requestInterceptor();

        RequestContextHolder.resetRequestAttributes(); // sin contexto activo

        feign.RequestTemplate template = new feign.RequestTemplate();

        assertDoesNotThrow(() -> interceptor.apply(template));
        assertFalse(template.headers().containsKey("Authorization"));
    }

    // =========================================================
    // FeignConfig — interceptor con Authorization que NO es Bearer
    // =========================================================
    @Test
    @DisplayName("FeignConfig - interceptor ignora Authorization que no es Bearer")
    void testFeignConfigConAuthNoBearer() {
        com.duoc.ms_bff.config.FeignConfig feignConfig = new com.duoc.ms_bff.config.FeignConfig();
        feign.RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        feign.RequestTemplate template = new feign.RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Authorization"));

        RequestContextHolder.resetRequestAttributes();
    }
}