package com.duoc.ms_geografico.service;

import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.repository.UbicacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionService(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

    public Ubicacion guardarUbicacion(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    public Ubicacion obtenerPorReporte(Long idReporte) {
        return ubicacionRepository.findByIdReporte(idReporte)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ubicación geográfica para el reporte con ID: " + idReporte));
    }
}