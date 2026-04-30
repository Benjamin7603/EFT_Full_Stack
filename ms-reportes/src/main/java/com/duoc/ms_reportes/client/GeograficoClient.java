package com.duoc.ms_reportes.client;

import com.duoc.ms_reportes.dto.UbicacionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MS-GEOGRAFICO", url = "${MS_GEOGRAFICO_URL}")
public interface GeograficoClient {

    @PostMapping("/api/geografico/guardar")
    UbicacionDTO guardarUbicacion(@RequestBody UbicacionDTO ubicacion);

}