package com.duoc.ms_geografico.controller;

import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.repository.UbicacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geografico")
@Tag(name = "Geográfico", description = "Endpoints para la gestión de coordenadas y zonas de riesgo de incendios")
public class GeograficoController {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Operation(summary = "Guardar ubicación", description = "Registra una nueva coordenada geográfica asociada a un reporte en Supabase")
    @PostMapping("/guardar")
    public Ubicacion guardarUbicacion(@Valid @RequestBody Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    @Operation(summary = "Obtener ubicación por reporte", description = "Busca la ubicación geográfica vinculada a un ID de reporte específico")
    @GetMapping("/reporte/{idReporte}")
    public Ubicacion obtenerPorReporte(@PathVariable Long idReporte) {
        return ubicacionRepository.findByIdReporte(idReporte)
                .orElseThrow(() -> new EntityNotFoundException("Ubicación no encontrada para el reporte: " + idReporte));
    }
}