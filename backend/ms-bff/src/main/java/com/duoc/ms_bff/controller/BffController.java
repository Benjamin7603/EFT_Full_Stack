package com.duoc.ms_bff.controller;

import com.duoc.ms_bff.client.GeograficoClient;
import com.duoc.ms_bff.client.ReportesClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class BffController {

    private final ReportesClient reportesClient;
    private final GeograficoClient geograficoClient;

    public BffController(ReportesClient reportesClient, GeograficoClient geograficoClient) {
        this.reportesClient = reportesClient;
        this.geograficoClient = geograficoClient;
    }

    @GetMapping("/bff/estado")
    public String estado() {
        return "BFF funcionando correctamente 🚀";
    }

    @GetMapping("/bff/reportes")
    public Object obtenerReportes() {
        return reportesClient.obtenerReportes();
    }

    @GetMapping("/bff/geografico/reporte/{idReporte}")
    public Object obtenerUbicacionPorReporte(@PathVariable Long idReporte) {
        return geograficoClient.obtenerUbicacionPorReporte(idReporte);
    }

    @PostMapping("/bff/reportar-incendio")
    public Object reportarIncendio(@RequestBody Object reporte) {
        return reportesClient.crearReporte(reporte);
    }

    // --- LA GRAN MEJORA: ASINCRONÍA + TOLERANCIA A FALLOS ---
    @GetMapping("/bff/incendio/{id}")
    public Object obtenerIncendioCompleto(@PathVariable Long id) {

        // 1. Capturamos el contexto y el Token JWT del hilo principal para no perderlo
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        // 2. Llamada asíncrona a MS-Reportes
        CompletableFuture<Object> reporteFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes); // Pasamos el JWT al nuevo hilo
            try {
                return reportesClient.obtenerReportePorId(id);
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        // 3. Llamada asíncrona a MS-Geografico con Tolerancia a Fallos
        CompletableFuture<Object> ubicacionFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            try {
                return geograficoClient.obtenerUbicacionPorReporte(id);
            } catch (Exception e) {
                // Si falla el mapa, no botamos la app, enviamos un aviso amigable
                return Map.of("alerta", "Ubicación temporalmente no disponible");
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        // 4. Esperamos a que ambos respondan simultáneamente (¡Mucho más rápido!)
        CompletableFuture.allOf(reporteFuture, ubicacionFuture).join();

        return Map.of(
                "reporte", reporteFuture.join(),
                "ubicacion", ubicacionFuture.join()
        );
    }
}