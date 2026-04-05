package com.duoc.ms_reportes.repository;

import com.duoc.ms_reportes.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    // Busca todos los reportes cuyo estado esté DENTRO de la lista que le enviemos
    List<Reporte> findByEstadoIn(List<String> estados);
}