// eslint-disable-next-line no-unused-vars
import React from 'react';
import { useEffect, useMemo, useState } from "react";
import { BarChart3, Download, RefreshCcw } from "lucide-react";
import Swal from "sweetalert2";
import api from "../api/api";
import "./TablaGestionReportes.css";

const estadosDisponibles = ["NUEVO", "EN_PROGRESO", "RESUELTO"];
const prioridadesDisponibles = ["ALTA", "MEDIA", "BAJA"];

export default function TablaGestionReportes({
                                                 titulo = "Gestión de reportes",
                                                 kicker = "Operación",
                                                 limite = null,
                                                 reportesExternos = null,
                                                 loadingExterno = false,
                                                 onActualizarExterno = null,
                                                 mostrarBotonActualizar = true,
                                                 mostrarBotonExcel = true,
                                             }) {
    const usaDatosExternos = Array.isArray(reportesExternos);

    const [reportesInternos, setReportesInternos] = useState([]);
    const [loadingInterno, setLoadingInterno] = useState(false);

    const loading = usaDatosExternos ? loadingExterno : loadingInterno;
    const reportesBase = usaDatosExternos ? reportesExternos : reportesInternos;

    const cargarReportes = async () => {
        if (usaDatosExternos) {
            if (onActualizarExterno) {
                await onActualizarExterno();
            }
            return;
        }

        setLoadingInterno(true);

        try {
            const response = await api.get("/api/reportes");
            setReportesInternos(response.data || []);
        } catch (error) {
            console.error("Error cargando reportes:", error);

            Swal.fire({
                icon: "error",
                title: "Error al cargar reportes",
                text:
                    error.response?.data?.error ||
                    error.response?.data?.mensaje ||
                    "No se pudieron cargar los reportes.",
                confirmButtonColor: "#dc3545",
            });
        } finally {
            setLoadingInterno(false);
        }
    };

    useEffect(() => {
        if (!usaDatosExternos) {
            cargarReportes();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [usaDatosExternos]);

    const reportesMostrados = useMemo(() => {
        const ordenados = [...reportesBase].sort(
            (a, b) => new Date(b.fechaReporte) - new Date(a.fechaReporte)
        );

        if (limite) {
            return ordenados.slice(0, limite);
        }

        return ordenados;
    }, [reportesBase, limite]);

    const refrescarDespuesDeCambio = async () => {
        if (usaDatosExternos && onActualizarExterno) {
            await onActualizarExterno();
        } else {
            await cargarReportes();
        }
    };

    const actualizarEstadoReporte = async (idReporte, nuevoEstado) => {
        try {
            await api.patch(`/api/reportes/${idReporte}/estado`, null, {
                params: { nuevoEstado },
            });

            await refrescarDespuesDeCambio();

            Swal.fire({
                icon: "success",
                title: "Estado actualizado",
                text: `El reporte #${idReporte} ahora está en estado ${nuevoEstado}.`,
                confirmButtonColor: "#FF7043",
            });
        } catch (error) {
            console.error("Error actualizando estado:", error);

            Swal.fire({
                icon: "error",
                title: "Error al actualizar estado",
                text:
                    error.response?.data?.error ||
                    error.response?.data?.mensaje ||
                    error.response?.data?.detalle ||
                    "No se pudo actualizar el estado del reporte.",
                confirmButtonColor: "#dc3545",
            });
        }
    };

    const actualizarPrioridadReporte = async (idReporte, nuevaPrioridad) => {
        try {
            await api.patch(`/api/reportes/${idReporte}/prioridad`, null, {
                params: { nuevaPrioridad },
            });

            await refrescarDespuesDeCambio();

            Swal.fire({
                icon: "success",
                title: "Prioridad actualizada",
                text: `El reporte #${idReporte} ahora tiene prioridad ${nuevaPrioridad}.`,
                confirmButtonColor: "#FF7043",
            });
        } catch (error) {
            console.error("Error actualizando prioridad:", error);

            Swal.fire({
                icon: "error",
                title: "Error al actualizar prioridad",
                text:
                    error.response?.data?.error ||
                    error.response?.data?.mensaje ||
                    error.response?.data?.detalle ||
                    "No se pudo actualizar la prioridad del reporte.",
                confirmButtonColor: "#dc3545",
            });
        }
    };

    const getEstadoClass = (estado) => {
        if (estado === "NUEVO") return "nuevo";
        if (estado === "EN_PROGRESO") return "proceso";
        return "resuelto";
    };

    const getPrioridadClass = (prioridad) => {
        if (prioridad === "ALTA") return "proceso";
        if (prioridad === "BAJA") return "resuelto";
        return "nuevo";
    };
    const obtenerRolSesion = () => {
        let rol = localStorage.getItem("rol");

        if (!rol) {
            try {
                const token = localStorage.getItem("token");

                if (token) {
                    const payload = token.split(".")[1];
                    const decoded = JSON.parse(atob(payload));
                    rol = decoded.rol;
                }
            } catch {
                rol = null;
            }
        }

        return rol ? rol.trim().toUpperCase() : "USER";
    };

    const puedeDescargarAuditoria = () => {
        return ["ADMIN", "FUNCIONARIO"].includes(obtenerRolSesion());
    };

    const descargarAuditoriaExcel = async () => {
        try {
            const response = await api.get("/api/reportes/auditoria/excel", {
                responseType: "blob",
            });

            const blob = new Blob([response.data], {
                type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            });

            const contentDisposition = response.headers["content-disposition"];
            let nombreArchivo = "auditoria_reportes_geofire.xlsx";

            if (contentDisposition) {
                const match = contentDisposition.match(/filename="?([^"]+)"?/);
                if (match?.[1]) {
                    nombreArchivo = match[1];
                }
            }

            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = url;
            link.setAttribute("download", nombreArchivo);
            document.body.appendChild(link);
            link.click();

            link.remove();
            window.URL.revokeObjectURL(url);

            Swal.fire({
                icon: "success",
                title: "Excel descargado",
                text: "La auditoría de reportes fue descargada correctamente.",
                confirmButtonColor: "#FF7043",
            });
        } catch (error) {
            console.error("Error descargando auditoría:", error);

            Swal.fire({
                icon: "error",
                title: "No se pudo descargar",
                text:
                    error.response?.data?.error ||
                    "No tienes permisos o no se pudo generar el archivo Excel.",
                confirmButtonColor: "#dc3545",
            });
        }
    };

    return (
        <section className="admin-card tabla-reportes-card">
            <div className="admin-card-header">
                <div>
                    <span className="admin-kicker">{kicker}</span>
                    <h2>{titulo}</h2>
                </div>

                <div className="tabla-reportes-header-actions">
                    <div className="admin-mini-label">
                        <BarChart3 size={17} />
                        {limite
                            ? `Últimos ${reportesMostrados.length} reportes`
                            : `${reportesMostrados.length} reportes`}
                    </div>

                    {mostrarBotonActualizar && (
                        <button
                            className="btn-admin-refresh"
                            type="button"
                            onClick={cargarReportes}
                            disabled={loading}
                        >
                            <RefreshCcw size={17} />
                            {loading ? "Actualizando..." : "Actualizar"}
                        </button>
                    )}
                    {mostrarBotonExcel && puedeDescargarAuditoria() && (
                        <button
                            className="btn-admin-primary tabla-reportes-export-btn"
                            type="button"
                            onClick={descargarAuditoriaExcel}
                        >
                            <Download size={17} />
                            Descargar Excel
                        </button>
                    )}
                </div>
            </div>

            {loading ? (
                <p className="admin-empty">Cargando reportes...</p>
            ) : reportesMostrados.length === 0 ? (
                <p className="admin-empty">No hay reportes registrados.</p>
            ) : (
                <div className="admin-table-wrapper">
                    <table className="admin-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Fecha</th>
                            <th>Descripción</th>
                            <th>Tipo usuario</th>
                            <th>Prioridad</th>
                            <th>Estado</th>
                            <th>Cambiar prioridad</th>
                            <th>Cambiar estado</th>
                        </tr>
                        </thead>

                        <tbody>
                        {reportesMostrados.map((reporte) => (
                            <tr key={reporte.id}>
                                <td>#{reporte.id}</td>

                                <td>
                                    {reporte.fechaReporte
                                        ? new Date(`${reporte.fechaReporte}Z`).toLocaleString("es-CL", {
                                            timeZone: "America/Santiago",
                                        })
                                        : "Sin fecha"}
                                </td>

                                <td>{reporte.descripcion}</td>

                                <td>{reporte.tipoUsuario || "CIUDADANO"}</td>

                                <td>
                                        <span
                                            className={`badge-riesgo ${getPrioridadClass(
                                                reporte.prioridad
                                            )}`}
                                        >
                                            {reporte.prioridad || "BAJA"}
                                        </span>
                                </td>

                                <td>
                                        <span
                                            className={`badge-riesgo ${getEstadoClass(
                                                reporte.estado
                                            )}`}
                                        >
                                            {reporte.estado || "NUEVO"}
                                        </span>
                                </td>

                                <td>
                                    <select
                                        className="admin-status-select"
                                        value={reporte.prioridad || "BAJA"}
                                        onChange={(e) =>
                                            actualizarPrioridadReporte(
                                                reporte.id,
                                                e.target.value
                                            )
                                        }
                                    >
                                        {prioridadesDisponibles.map((prioridad) => (
                                            <option key={prioridad} value={prioridad}>
                                                {prioridad}
                                            </option>
                                        ))}
                                    </select>
                                </td>

                                <td>
                                    <select
                                        className="admin-status-select"
                                        value={reporte.estado || "NUEVO"}
                                        onChange={(e) =>
                                            actualizarEstadoReporte(
                                                reporte.id,
                                                e.target.value
                                            )
                                        }
                                    >
                                        {estadosDisponibles.map((estado) => (
                                            <option key={estado} value={estado}>
                                                {estado}
                                            </option>
                                        ))}
                                    </select>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}