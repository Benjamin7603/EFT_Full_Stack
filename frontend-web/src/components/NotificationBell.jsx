import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, CheckCheck, Trash2, ExternalLink } from "lucide-react";
import api from "../api/api";
import "./NotificationBell.css";

export default function NotificationBell({ destinatario }) {
    const navigate = useNavigate();
    const wrapperRef = useRef(null);

    const [open, setOpen] = useState(false);
    const [notificaciones, setNotificaciones] = useState([]);
    const [contador, setContador] = useState(0);
    const [loading, setLoading] = useState(false);

    const destino = destinatario || localStorage.getItem("rol") || "USER";

    const cargarContador = async () => {
        try {
            const response = await api.get(`/api/notificaciones/destinatario/${destino}/contador`);
            setContador(Number(response.data) || 0);
        } catch (error) {
            console.error("Error al cargar contador de notificaciones:", error);
        }
    };

    const cargarNoLeidas = async () => {
        try {
            setLoading(true);
            const response = await api.get(`/api/notificaciones/destinatario/${destino}/no-leidas`);
            setNotificaciones(Array.isArray(response.data) ? response.data : []);
        } catch (error) {
            console.error("Error al cargar notificaciones:", error);
        } finally {
            setLoading(false);
        }
    };

    const marcarComoLeida = async (id) => {
        try {
            await api.patch(`/api/notificaciones/${id}/leer`);
            await cargarNoLeidas();
            await cargarContador();
        } catch (error) {
            console.error("Error al marcar notificación como leída:", error);
        }
    };

    const marcarTodasComoLeidas = async () => {
        try {
            await api.patch(`/api/notificaciones/destinatario/${destino}/leer-todas`);
            await cargarNoLeidas();
            await cargarContador();
        } catch (error) {
            console.error("Error al marcar todas como leídas:", error);
        }
    };

    const eliminarNotificacion = async (id) => {
        try {
            await api.delete(`/api/notificaciones/${id}`);
            await cargarNoLeidas();
            await cargarContador();
        } catch (error) {
            console.error("Error al eliminar notificación:", error);
        }
    };

    const irAHistorial = () => {
        setOpen(false);
        navigate("/notificaciones");
    };

    const formatearFecha = (fecha) => {
        if (!fecha) return "Sin fecha";

        return new Date(fecha).toLocaleString("es-CL", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    useEffect(() => {
        cargarContador();

        const interval = setInterval(() => {
            cargarContador();
        }, 30000);

        return () => clearInterval(interval);
    }, [destino]);

    useEffect(() => {
        if (open) {
            cargarNoLeidas();
        }
    }, [open]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
                setOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <div className="notification-wrapper" ref={wrapperRef}>
            <button
                type="button"
                className={`notification-button ${contador > 0 ? "has-notifications" : ""}`}
                onClick={() => setOpen((prev) => !prev)}
                title="Notificaciones"
            >
                <Bell size={21} />
                {contador > 0 && <span className="notification-badge">{contador > 99 ? "99+" : contador}</span>}
            </button>

            {open && (
                <div className="notification-dropdown">
                    <div className="notification-header">
                        <div>
                            <h3>Notificaciones</h3>
                            <p>{contador} sin leer</p>
                        </div>

                        {contador > 0 && (
                            <button
                                type="button"
                                className="notification-mark-all"
                                onClick={marcarTodasComoLeidas}
                                title="Marcar todas como leídas"
                            >
                                <CheckCheck size={17} />
                            </button>
                        )}
                    </div>

                    <div className="notification-list">
                        {loading ? (
                            <div className="notification-empty">Cargando notificaciones...</div>
                        ) : notificaciones.length === 0 ? (
                            <div className="notification-empty">
                                No tienes notificaciones pendientes.
                            </div>
                        ) : (
                            notificaciones.slice(0, 5).map((noti) => (
                                <div key={noti.id} className="notification-item unread">
                                    <div className="notification-content">
                                        <strong>{noti.titulo || "Alerta GeoFire"}</strong>
                                        <p>{noti.mensaje}</p>
                                        <span>{formatearFecha(noti.fechaEnvio)}</span>
                                    </div>

                                    <div className="notification-actions">
                                        <button
                                            type="button"
                                            onClick={() => marcarComoLeida(noti.id)}
                                            title="Marcar como leída"
                                        >
                                            <CheckCheck size={16} />
                                        </button>

                                        <button
                                            type="button"
                                            onClick={() => eliminarNotificacion(noti.id)}
                                            title="Eliminar"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    <button
                        type="button"
                        className="notification-view-all"
                        onClick={irAHistorial}
                    >
                        Ver todo el historial
                        <ExternalLink size={15} />
                    </button>
                </div>
            )}
        </div>
    );
}