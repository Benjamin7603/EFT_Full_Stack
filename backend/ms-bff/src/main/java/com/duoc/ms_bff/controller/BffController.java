package com.duoc.ms_bff.controller;

import com.duoc.ms_bff.client.GeograficoClient;
import com.duoc.ms_bff.client.ReportesClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping("/bff/incendio/{id}")
    public Object obtenerIncendioCompleto(@PathVariable Long id) {

        Object reporte = reportesClient.obtenerReportePorId(id);
        Object ubicacion = geograficoClient.obtenerUbicacionPorReporte(id);

        return Map.of(
                "reporte", reporte,
                "ubicacion", ubicacion
        );
    }

    @PostMapping("/bff/reportar-incendio")
    public Object reportarIncendio(@RequestBody Object reporte) {
        return reportesClient.crearReporte(reporte);
    }
}