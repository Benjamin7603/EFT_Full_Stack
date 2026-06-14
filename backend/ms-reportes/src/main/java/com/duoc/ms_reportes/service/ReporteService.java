package com.duoc.ms_reportes.service;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.NotificacionDTO;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.dto.UbicacionDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final GeograficoClient geograficoClient;
    private final NotificacionClient notificacionClient;
    private final RabbitTemplate rabbitTemplate; // <-- NUEVA VARIABLE

    public ReporteService(ReporteRepository reporteRepository,
                          GeograficoClient geograficoClient,
                          NotificacionClient notificacionClient,
                          RabbitTemplate rabbitTemplate) {
        this.reporteRepository = reporteRepository;
        this.geograficoClient = geograficoClient;
        this.notificacionClient = notificacionClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte crearReporteProcesado(ReporteDTO datosEntrada) {
        return crearReporteProcesado(datosEntrada, null);
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte crearReporteProcesado(ReporteDTO datosEntrada, String rolSesion) {
        Reporte nuevoReporte = new Reporte();

        nuevoReporte.setLatitud(datosEntrada.getLatitud());
        nuevoReporte.setLongitud(datosEntrada.getLongitud());
        nuevoReporte.setDescripcion(datosEntrada.getDescripcion());
        nuevoReporte.setUrlMedia(datosEntrada.getUrlMedia());
        nuevoReporte.setTipoUsuario(datosEntrada.getTipoUsuario());
        nuevoReporte.setUsuarioId(datosEntrada.getUsuarioId());

        String prioridad;

        if (esRolOperativo(rolSesion)) {
            prioridad = normalizarPrioridad(datosEntrada.getPrioridad());
        } else {
            prioridad = "BAJA";
        }

        nuevoReporte.setEstado("NUEVO");
        nuevoReporte.setPrioridad(prioridad);

        Reporte reporteGuardado = reporteRepository.save(nuevoReporte);


        try {
            UbicacionDTO ubicacion = new UbicacionDTO(
                    reporteGuardado.getId(),
                    reporteGuardado.getLatitud(),
                    reporteGuardado.getLongitud()
            );
            geograficoClient.guardarUbicacion(ubicacion);
        } catch (Exception e) {
            System.err.println("Atención: ms-geografico no está disponible en este momento.");
        }

        // --- ¡AQUÍ ESTÁ LA MAGIA DE RABBITMQ CON JSON! ---
        try {
            NotificacionDTO alerta = new NotificacionDTO(
                    "¡NUEVO INCENDIO REPORTADO! ID: " + reporteGuardado.getId() + " - Prioridad: " + reporteGuardado.getPrioridad(),
                    "ADMIN"
            );

            // 1. Instanciamos el transformador a JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            // 2. Transformamos el objeto a texto JSON
            String jsonAlerta = mapper.writeValueAsString(alerta);

            // 3. Enviamos el texto JSON en lugar del objeto Java
            rabbitTemplate.convertAndSend("notificaciones.queue", jsonAlerta);

            System.out.println("🐇 ¡Mensaje JSON enviado a RabbitMQ con éxito!");
        } catch (Exception e) {
            System.err.println("⚠️ Atención: No se pudo enviar el mensaje a RabbitMQ en este momento: " + e.getMessage());
        }

        return reporteGuardado;
    }

    @Cacheable(value = "reportesTodos", key = "'all'")
    public List<Reporte> listarTodos() {
        return reporteRepository.findAll();
    }

    @Cacheable(value = "reportesActivos", key = "'active'")
    public List<Reporte> listarActivos() {
        return reporteRepository.findByEstadoIn(List.of("NUEVO", "EN_PROGRESO"));
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte actualizarEstado(Long id, String nuevoEstado) {
        String estadoNormalizado = normalizarEstado(nuevoEstado);

        return reporteRepository.findById(id).map(reporte -> {
            reporte.setEstado(estadoNormalizado);
            return reporteRepository.save(reporte);
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte actualizarPrioridad(Long id, String nuevaPrioridad) {
        String prioridadNormalizada = normalizarPrioridad(nuevaPrioridad);

        return reporteRepository.findById(id).map(reporte -> {
            reporte.setPrioridad(prioridadNormalizada);
            return reporteRepository.save(reporte);
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

    public Reporte obtenerPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

    private boolean esRolOperativo(String rol) {
        if (rol == null || rol.isBlank()) {
            return false;
        }

        String rolNormalizado = rol.trim().toUpperCase();

        return rolNormalizado.equals("ADMIN")
                || rolNormalizado.equals("BOMBERO")
                || rolNormalizado.equals("BRIGADISTA")
                || rolNormalizado.equals("FUNCIONARIO");
    }

    private String normalizarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            throw new IllegalArgumentException("La prioridad es obligatoria para usuarios operativos.");
        }

        String prioridadNormalizada = prioridad.trim().toUpperCase();

        if (
                !prioridadNormalizada.equals("ALTA") &&
                        !prioridadNormalizada.equals("MEDIA") &&
                        !prioridadNormalizada.equals("BAJA")
        ) {
            throw new IllegalArgumentException("La prioridad debe ser ALTA, MEDIA o BAJA");
        }

        return prioridadNormalizada;
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado del reporte es obligatorio.");
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        if (
                !estadoNormalizado.equals("NUEVO") &&
                        !estadoNormalizado.equals("EN_PROGRESO") &&
                        !estadoNormalizado.equals("RESUELTO")
        ) {
            throw new IllegalArgumentException("El estado debe ser NUEVO, EN_PROGRESO o RESUELTO.");
        }

        return estadoNormalizado;
    }
    public byte[] generarExcelAuditoriaReportes() {
        List<Reporte> reportes = reporteRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Auditoria Reportes");

            CellStyle tituloStyle = workbook.createCellStyle();
            Font tituloFont = workbook.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 16);
            tituloStyle.setFont(tituloFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIndex = 0;

            Row tituloRow = sheet.createRow(rowIndex++);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("Auditoría / Resumen de Reportes GeoFire");
            tituloCell.setCellStyle(tituloStyle);

            rowIndex++;

            long totalReportes = reportes.size();
            long totalNuevo = reportes.stream().filter(r -> "NUEVO".equalsIgnoreCase(r.getEstado())).count();
            long totalEnProgreso = reportes.stream().filter(r -> "EN_PROGRESO".equalsIgnoreCase(r.getEstado())).count();
            long totalResuelto = reportes.stream().filter(r -> "RESUELTO".equalsIgnoreCase(r.getEstado())).count();

            long totalAlta = reportes.stream().filter(r -> "ALTA".equalsIgnoreCase(r.getPrioridad())).count();
            long totalMedia = reportes.stream().filter(r -> "MEDIA".equalsIgnoreCase(r.getPrioridad())).count();
            long totalBaja = reportes.stream().filter(r -> "BAJA".equalsIgnoreCase(r.getPrioridad())).count();

            rowIndex = crearFilaResumen(sheet, rowIndex, "Total reportes", totalReportes);
            rowIndex = crearFilaResumen(sheet, rowIndex, "Reportes NUEVO", totalNuevo);
            rowIndex = crearFilaResumen(sheet, rowIndex, "Reportes EN_PROGRESO", totalEnProgreso);
            rowIndex = crearFilaResumen(sheet, rowIndex, "Reportes RESUELTO", totalResuelto);
            rowIndex = crearFilaResumen(sheet, rowIndex, "Prioridad ALTA", totalAlta);
            rowIndex = crearFilaResumen(sheet, rowIndex, "Prioridad MEDIA", totalMedia);
            rowIndex = crearFilaResumen(sheet, rowIndex, "Prioridad BAJA", totalBaja);

            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);

            String[] columnas = {
                    "ID",
                    "Fecha(CL)",
                    "Descripción",
                    "Tipo usuario",
                    "Prioridad",
                    "Estado",
                    "Latitud",
                    "Longitud",
                    "Usuario ID"
            };

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            ZoneId zonaChile = ZoneId.of("America/Santiago");

            for (Reporte reporte : reportes) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(reporte.getId() != null ? reporte.getId() : 0);
                row.createCell(1).setCellValue(
                        reporte.getFechaReporte() != null
                                ? reporte.getFechaReporte()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(zonaChile)
                                .format(formatter)
                                : "Sin fecha"
                );
                row.createCell(2).setCellValue(reporte.getDescripcion() != null ? reporte.getDescripcion() : "");
                row.createCell(3).setCellValue(reporte.getTipoUsuario() != null ? reporte.getTipoUsuario() : "");
                row.createCell(4).setCellValue(reporte.getPrioridad() != null ? reporte.getPrioridad() : "");
                row.createCell(5).setCellValue(reporte.getEstado() != null ? reporte.getEstado() : "");
                row.createCell(6).setCellValue(reporte.getLatitud() != null ? reporte.getLatitud() : 0);
                row.createCell(7).setCellValue(reporte.getLongitud() != null ? reporte.getLongitud() : 0);
                row.createCell(8).setCellValue(reporte.getUsuarioId() != null ? reporte.getUsuarioId() : 0);
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el Excel de auditoría de reportes.", e);
        }
    }

    private int crearFilaResumen(Sheet sheet, int rowIndex, String etiqueta, long valor) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(etiqueta);
        row.createCell(1).setCellValue(valor);
        return rowIndex + 1;
    }
}