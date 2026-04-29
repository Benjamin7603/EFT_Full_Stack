package com.duoc.ms_bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "ms-reportes")
public interface ReportesClient {

    @GetMapping("/api/reportes")
    Object obtenerReportes();
}