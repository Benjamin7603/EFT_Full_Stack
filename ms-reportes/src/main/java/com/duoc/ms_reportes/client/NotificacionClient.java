package com.duoc.ms_reportes.client;

import com.duoc.ms_reportes.dto.NotificacionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MS-NOTIFICACIONES")
public interface NotificacionClient {

    @PostMapping("/api/notificaciones/enviar")
    NotificacionDTO enviarAlerta(@RequestBody NotificacionDTO notificacion);

}