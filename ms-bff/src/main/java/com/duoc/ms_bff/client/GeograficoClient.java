package com.duoc.ms_bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-geografico")
public interface GeograficoClient {

    @GetMapping("/api/geografico/reporte/{idReporte}")
    Object obtenerUbicacionPorReporte(@PathVariable Long idReporte);
}