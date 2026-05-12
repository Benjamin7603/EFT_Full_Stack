package com.duoc.ms_geografico.repository;

import com.duoc.ms_geografico.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    Optional<Ubicacion> findByIdReporte(Long idReporte);
}