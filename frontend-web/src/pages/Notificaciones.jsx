import { useEffect, useMemo, useState } from "react";
import { Bell, CheckCheck, Trash2, RefreshCw, Search, Inbox } from "lucide-react";
import Swal from "sweetalert2";
import Navbar from "../components/Navbar";
import api from "../api/api";
import "./Notificaciones.css";
import "../App.css";

export default function Notificaciones() {
    const [notificaciones, setNotificaciones] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filtro, setFiltro] = useState("TODAS");
    const [busqueda, setBusqueda] = useState("");
    const [error, setError] = useState("");

    const destinatario = useMemo(() => {
        return (localStorage.getItem("rol") || obtenerRolDesdeToken() || "USER").toUpperCase();
    }, []);

    function obtenerRolDesdeToken() {
        try {
            const token = localStorage.getItem("token");
            if (!token) return null;

            const payload = JSON.parse(atob(token.split(".")[1]));
            return payload.rol || null;
        } catch {
            return null;
        }
    }

    const cargarNotificaciones = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get(`/api/notificaciones/destinatario/${destinatario}`);
            setNotificaciones(Array.isArray(response.data) ? response.data : []);
        } catch (error) {
            console.error("Error al cargar historial de notificaciones:", error);
            setError("No se pudo cargar el historial de notificaciones.");
        } finally {
            setLoading(false);
        }
    };

    const marcarComoLeida = async (id) => {
        try {
            await api.patch(`/api/notificaciones/${id}/leer`);
            await cargarNotificaciones();
        } catch (error) {
            console.error("Error al marcar como leída:", error);
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudo marcar la notificación como leída.",
                confirmButtonColor: "#FF7043",
            });
        }
    };

    const marcarTodasComoLeidas = async () => {
        try {
            await api.patch(`/api/notificaciones/destinatario/${destinatario}/leer-todas`);
            await cargarNotificaciones();

            Swal.fire({
                icon: "success",
                title: "Listo",
                text: "Todas las notificaciones fueron marcadas como leídas.",
                confirmButtonColor: "#FF7043",
                timer: 1800,
                showConfirmButton: false,
            });
        } catch (error) {
            console.error("Error al marcar todas como leídas:", error);
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudieron marcar todas las notificaciones.",
                confirmButtonColor: "#FF7043",
            });
        }
    };

    const eliminarNotificacion = async (id) => {
        const result = await Swal.fire({
            title: "¿Eliminar notificación?",
            text: "Esta acción no se puede deshacer.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#FF7043",
            cancelButtonColor: "#6b7280",
            confirmButtonText: "Sí, eliminar",
            cancelButtonText: "Cancelar",
        });

        if (!result.isConfirmed) return;

        try {
            await api.delete(`/api/notificaciones/${id}`);
            await cargarNotificaciones();

            Swal.fire({
                icon: "success",
                title: "Eliminada",
                text: "La notificación fue eliminada.",
                confirmButtonColor: "#FF7043",
                timer: 1500,
                showConfirmButton: false,
            });
        } catch (error) {
            console.error("Error al eliminar notificación:", error);
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudo eliminar la notificación.",
                confirmButtonColor: "#FF7043",
            });
        }
    };

    const formatearFecha = (fecha) => {
        if (!fecha) return "Sin fecha";

        return new Date(fecha).toLocaleString("es-CL", {
            day: "2-digit",
            month: "long",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    const obtenerClasePrioridad = (prioridad) => {
        const valor = (prioridad || "MEDIA").toUpperCase();

        if (valor === "ALTA") return "noti-prioridad alta";
        if (valor === "BAJA") return "noti-prioridad baja";

        return "noti-prioridad media";
    };

    const notificacionesFiltradas = useMemo(() => {
        return notificaciones.filter((noti) => {
            const coincideFiltro =
                filtro === "TODAS" ||
                (filtro === "NO_LEIDAS" && !noti.leida) ||
                (filtro === "LEIDAS" && noti.leida);

            const texto = `${noti.titulo || ""} ${noti.mensaje || ""} ${noti.tipo || ""} ${noti.prioridad || ""}`.toLowerCase();
            const coincideBusqueda = texto.includes(busqueda.trim().toLowerCase());

            return coincideFiltro && coincideBusqueda;
        });
    }, [notificaciones, filtro, busqueda]);

    const total = notificaciones.length;
    const noLeidas = notificaciones.filter((n) => !n.leida).length;
    const leidas = notificaciones.filter((n) => n.leida).length;

    useEffect(() => {
        cargarNotificaciones();
    }, []);

    return (
        <div className="notificaciones-page">
            <Navbar />

            <main className="notificaciones-container">
                <section className="notificaciones-hero">
                    <div>
                        <span className="notificaciones-kicker">Centro de alertas</span>
                        <h1>Historial de Notificaciones</h1>
                        <p>
                            Revisa las alertas generadas por reportes de incendios y eventos importantes
                            asociados al rol <strong>{destinatario}</strong>.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="noti-refresh-btn"
                        onClick={cargarNotificaciones}
                        disabled={loading}
                    >
                        <RefreshCw size={18} className={loading ? "spinning" : ""} />
                        Actualizar
                    </button>
                </section>

                <section className="notificaciones-stats">
                    <article className="noti-stat-card">
                        <div className="noti-stat-icon total">
                            <Bell size={22} />
                        </div>
                        <div>
                            <span>Total</span>
                            <strong>{total}</strong>
                        </div>
                    </article>

                    <article className="noti-stat-card">
                        <div className="noti-stat-icon unread">
                            <Inbox size={22} />
                        </div>
                        <div>
                            <span>No leídas</span>
                            <strong>{noLeidas}</strong>
                        </div>
                    </article>

                    <article className="noti-stat-card">
                        <div className="noti-stat-icon read">
                            <CheckCheck size={22} />
                        </div>
                        <div>
                            <span>Leídas</span>
                            <strong>{leidas}</strong>
                        </div>
                    </article>
                </section>

                <section className="notificaciones-toolbar">
                    <div className="noti-search">
                        <Search size={18} />
                        <input
                            type="text"
                            placeholder="Buscar por título, mensaje, tipo o prioridad..."
                            value={busqueda}
                            onChange={(e) => setBusqueda(e.target.value)}
                        />
                    </div>

                    <div className="noti-filters">
                        <button
                            type="button"
                            className={filtro === "TODAS" ? "active" : ""}
                            onClick={() => setFiltro("TODAS")}
                        >
                            Todas
                        </button>

                        <button
                            type="button"
                            className={filtro === "NO_LEIDAS" ? "active" : ""}
                            onClick={() => setFiltro("NO_LEIDAS")}
                        >
                            No leídas
                        </button>

                        <button
                            type="button"
                            className={filtro === "LEIDAS" ? "active" : ""}
                            onClick={() => setFiltro("LEIDAS")}
                        >
                            Leídas
                        </button>
                    </div>

                    {noLeidas > 0 && (
                        <button
                            type="button"
                            className="noti-mark-all-btn"
                            onClick={marcarTodasComoLeidas}
                        >
                            <CheckCheck size={17} />
                            Marcar todas
                        </button>
                    )}
                </section>

                {error && <div className="noti-error">{error}</div>}

                <section className="notificaciones-list">
                    {loading ? (
                        <div className="noti-empty">
                            <RefreshCw size={30} className="spinning" />
                            <h3>Cargando historial...</h3>
                            <p>Estamos obteniendo tus notificaciones.</p>
                        </div>
                    ) : notificacionesFiltradas.length === 0 ? (
                        <div className="noti-empty">
                            <Inbox size={38} />
                            <h3>No hay notificaciones para mostrar</h3>
                            <p>Prueba cambiando el filtro o actualizando el historial.</p>
                        </div>
                    ) : (
                        notificacionesFiltradas.map((noti) => (
                            <article
                                key={noti.id}
                                className={`noti-card ${noti.leida ? "read" : "unread"}`}
                            >
                                <div className="noti-card-main">
                                    <div className="noti-card-top">
                                        <div>
                                            <h3>{noti.titulo || "Alerta GeoFire"}</h3>
                                            <span className="noti-date">{formatearFecha(noti.fechaEnvio)}</span>
                                        </div>

                                        <div className="noti-badges">
                                            <span className={obtenerClasePrioridad(noti.prioridad)}>
                                                {noti.prioridad || "MEDIA"}
                                            </span>

                                            <span className={`noti-status ${noti.leida ? "leida" : "pendiente"}`}>
                                                {noti.leida ? "Leída" : "No leída"}
                                            </span>
                                        </div>
                                    </div>

                                    <p className="noti-message">{noti.mensaje}</p>

                                    <div className="noti-meta">
                                        <span>Tipo: {noti.tipo || "SISTEMA"}</span>
                                        {noti.reporteId && <span>Reporte ID: {noti.reporteId}</span>}
                                        <span>Destinatario: {noti.destinatario || destinatario}</span>
                                    </div>
                                </div>

                                <div className="noti-card-actions">
                                    {!noti.leida && (
                                        <button
                                            type="button"
                                            className="noti-action read"
                                            onClick={() => marcarComoLeida(noti.id)}
                                            title="Marcar como leída"
                                        >
                                            <CheckCheck size={18} />
                                        </button>
                                    )}

                                    <button
                                        type="button"
                                        className="noti-action delete"
                                        onClick={() => eliminarNotificacion(noti.id)}
                                        title="Eliminar"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </article>
                        ))
                    )}
                </section>
            </main>
        </div>
    );
}