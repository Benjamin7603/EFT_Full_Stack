package com.duoc.ms_bff.controller;

import com.duoc.ms_bff.client.ReportesClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BffController {

    private final ReportesClient reportesClient;

    public BffController(ReportesClient reportesClient) {
        this.reportesClient = reportesClient;
    }

    @GetMapping("/bff/estado")
    public String estado() {
        return "BFF funcionando correctamente 🚀";
    }

    @GetMapping("/bff/reportes")
    public Object obtenerReportes() {
        return reportesClient.obtenerReportes();
    }
}