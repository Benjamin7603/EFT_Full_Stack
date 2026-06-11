import { useEffect, useRef, useState } from "react";
import { Bell, CheckCheck, Trash2 } from "lucide-react";
import api from "../api/api";
import "./NotificationBell.css";

export default function NotificationBell({ destinatario }) {
    const [open, setOpen] = useState(false);
    const [notificaciones, setNotificaciones] = useState([]);
    const [contador, setContador] = useState(0);
    const [loading, setLoading] = useState(false);
    const bellRef = useRef(null);

    const cargarContador = async () => {
        if (!destinatario) return;

        try {
            const response = await api.get(
                `/api/notificaciones/destinatario/${destinatario}/contador`
            );

            setContador(response.data?.noLeidas || 0);
        } catch (error) {
            console.error("Error cargando contador de notificaciones:", error);
        }
    };

    const cargarNoLeidas = async () => {
        if (!destinatario) return;

        try {
            setLoading(true);

            const response = await api.get(
                `/api/notificaciones/destinatario/${destinatario}/no-leidas`
            );

            setNotificaciones(response.data || []);
        } catch (error) {
            console.error("Error cargando notificaciones:", error);
        } finally {
            setLoading(false);
        }
    };

    const abrirDropdown = async () => {
        const nuevoEstado = !open;
        setOpen(nuevoEstado);

        if (nuevoEstado) {
            await cargarNoLeidas();
            await cargarContador();
        }
    };

    const marcarComoLeida = async (id) => {
        try {
            await api.patch(`/api/notificaciones/${id}/leer`);

            setNotificaciones((prev) =>
                prev.filter((notificacion) => notificacion.id !== id)
            );

            setContador((prev) => Math.max(prev - 1, 0));
        } catch (error) {
            console.error("Error marcando notificación como leída:", error);
        }
    };

    const marcarTodasComoLeidas = async () => {
        try {
            await api.patch(
                `/api/notificaciones/destinatario/${destinatario}/leer-todas`
            );

            setNotificaciones([]);
            setContador(0);
        } catch (error) {
            console.error("Error marcando todas como leídas:", error);
        }
    };

    const eliminarNotificacion = async (id) => {
        try {
            await api.delete(`/api/notificaciones/${id}`);

            setNotificaciones((prev) =>
                prev.filter((notificacion) => notificacion.id !== id)
            );

            setContador((prev) => Math.max(prev - 1, 0));
        } catch (error) {
            console.error("Error eliminando notificación:", error);
        }
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

        const intervalo = setInterval(() => {
            cargarContador();
        }, 30000);

        return () => clearInterval(intervalo);
    }, [destinatario]);

    useEffect(() => {
        const cerrarAlClickAfuera = (event) => {
            if (bellRef.current && !bellRef.current.contains(event.target)) {
                setOpen(false);
            }
        };

        document.addEventListener("mousedown", cerrarAlClickAfuera);

        return () => {
            document.removeEventListener("mousedown", cerrarAlClickAfuera);
        };
    }, []);

    return (
        <div className="notification-wrapper" ref={bellRef}>
            <button
                type="button"
                className={`notification-button ${open ? "notification-active" : ""}`}
                onClick={abrirDropdown}
                title="Notificaciones"
            >
                <Bell size={20} strokeWidth={2.4} />

                {contador > 0 && (
                    <span className="notification-badge">
                        {contador > 9 ? "9+" : contador}
                    </span>
                )}
            </button>

            {open && (
                <div className="notification-dropdown">
                    <div className="notification-header">
                        <div>
                            <h4>Notificaciones</h4>
                            <span>{contador} no leídas</span>
                        </div>

                        {notificaciones.length > 0 && (
                            <button
                                type="button"
                                className="notification-read-all"
                                onClick={marcarTodasComoLeidas}
                                title="Marcar todas como leídas"
                            >
                                <CheckCheck size={18} />
                            </button>
                        )}
                    </div>

                    <div className="notification-list">
                        {loading ? (
                            <p className="notification-empty">Cargando...</p>
                        ) : notificaciones.length === 0 ? (
                            <p className="notification-empty">
                                No tienes notificaciones pendientes.
                            </p>
                        ) : (
                            notificaciones.map((notificacion) => (
                                <div
                                    key={notificacion.id}
                                    className={`notification-item prioridad-${(notificacion.prioridad || "MEDIA").toLowerCase()}`}
                                >
                                    <div className="notification-content">
                                        <div className="notification-title-row">
                                            <strong>
                                                {notificacion.titulo || "Alerta GeoFire"}
                                            </strong>

                                            <span className="notification-priority">
                                                {notificacion.prioridad || "MEDIA"}
                                            </span>
                                        </div>

                                        <p>{notificacion.mensaje}</p>

                                        <small>
                                            {formatearFecha(notificacion.fechaEnvio)}
                                        </small>
                                    </div>

                                    <div className="notification-actions">
                                        <button
                                            type="button"
                                            onClick={() => marcarComoLeida(notificacion.id)}
                                            title="Marcar como leída"
                                        >
                                            <CheckCheck size={16} />
                                        </button>

                                        <button
                                            type="button"
                                            onClick={() => eliminarNotificacion(notificacion.id)}
                                            title="Eliminar"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}