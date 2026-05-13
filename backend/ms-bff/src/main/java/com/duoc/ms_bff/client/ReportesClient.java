package com.duoc.ms_bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-reportes", url = "${MS_REPORTES_URL}")
public interface ReportesClient {

    @GetMapping("/api/reportes")
    Object obtenerReportes();

    @GetMapping("/api/reportes/{id}")
    Object obtenerReportePorId(@PathVariable Long id);

    @PostMapping("/api/reportes")
    Object crearReporte(@RequestBody Object reporte);
}