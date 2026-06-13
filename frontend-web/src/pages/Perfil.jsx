import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    BadgeCheck,
    CalendarClock,
    LayoutDashboard,
    Lock,
    LogOut,
    Mail,
    Phone,
    RotateCcw,
    Save,
    ShieldCheck,
    UserRound,
} from "lucide-react";

import api from "../api/api";

import "./Dashboard.css";
import "./Perfil.css";
import Navbar from "../components/Navbar.jsx";

export default function Perfil() {
    const navigate = useNavigate();

    const getTokenData = () => {
        try {
            const token = localStorage.getItem("token");

            if (!token) {
                return {};
            }

            const payload = token.split(".")[1];
            const decoded = JSON.parse(atob(payload));

            return {
                usuarioId: decoded.usuarioId,
                username: decoded.sub,
                rol: decoded.rol,
            };
        } catch {
            return {};
        }
    };

    const tokenData = getTokenData();

    const usuarioIdSesion =
        localStorage.getItem("usuarioId") ||
        localStorage.getItem("userId") ||
        tokenData.usuarioId ||
        "";

    const usernameSesion =
        localStorage.getItem("username") ||
        localStorage.getItem("userUsername") ||
        tokenData.username ||
        "";

    const rolSesion =
        localStorage.getItem("rol") ||
        tokenData.rol ||
        "USER";

    const ultimoAcceso = localStorage.getItem("ultimoAcceso");

    const ultimoAccesoFormateado = ultimoAcceso
        ? new Date(ultimoAcceso).toLocaleString("es-CL", {
            dateStyle: "medium",
            timeStyle: "short",
        })
        : "No disponible";

    const emptyForm = {
        nombre: "",
        apellido: "",
        email: "",
        telefono: "",
        password: "",
        rol: rolSesion,
    };

    const [usuarioActual, setUsuarioActual] = useState(null);
    const [form, setForm] = useState(emptyForm);
    const [initialForm, setInitialForm] = useState(emptyForm);

    const [loading, setLoading] = useState(false);
    const [loadingPerfil, setLoadingPerfil] = useState(true);
    const [loadingReportes, setLoadingReportes] = useState(true);

    const [mensaje, setMensaje] = useState("");
    const [error, setError] = useState("");
    const [misReportes, setMisReportes] = useState([]);

    const usernameVisible =
        usuarioActual?.username ||
        usernameSesion ||
        "usuario";

    const inicial = (form.nombre || usernameVisible || "U")
        .trim()
        .charAt(0)
        .toUpperCase();

    const nombreVisible = form.nombre || usernameVisible || "Usuario";

    const obtenerClasePrioridad = (prioridad) => {
        if (prioridad === "ALTA") return "proceso";
        if (prioridad === "BAJA") return "resuelto";
        return "nuevo";
    };

    const obtenerClaseEstado = (estado) => {
        if (estado === "NUEVO") return "nuevo";
        if (estado === "EN_PROGRESO") return "proceso";
        return "resuelto";
    };

    const formatearCoordenadas = (latitud, longitud) => {
        if (latitud === null || latitud === undefined || longitud === null || longitud === undefined) {
            return "Sin ubicación";
        }

        const lat = Number(latitud);
        const lon = Number(longitud);

        if (Number.isNaN(lat) || Number.isNaN(lon)) {
            return "Sin ubicación";
        }

        return `${lat.toFixed(5)}, ${lon.toFixed(5)}`;
    };

    useEffect(() => {
        const cargarPerfil = async () => {
            setLoadingPerfil(true);
            setLoadingReportes(true);
            setError("");
            setMensaje("");

            if (!usuarioIdSesion && !usernameSesion) {
                setError("No se encontró información del usuario en la sesión. Vuelve a iniciar sesión.");
                setLoadingPerfil(false);
                setLoadingReportes(false);
                return;
            }

            try {
                const usuarioResponse = await api.get("/api/usuarios/me");
                const usuarioEncontrado = usuarioResponse.data;

                if (!usuarioEncontrado || !usuarioEncontrado.id) {
                    setError("No se encontró la información del usuario autenticado.");
                    setLoadingPerfil(false);
                    setLoadingReportes(false);
                    return;
                }

                setUsuarioActual(usuarioEncontrado);

                const datosPerfil = {
                    nombre: usuarioEncontrado.nombre || "",
                    apellido: usuarioEncontrado.apellido || "",
                    email: usuarioEncontrado.email || "",
                    telefono: usuarioEncontrado.telefono || "",
                    password: "",
                    rol: usuarioEncontrado.rol || rolSesion,
                };

                setForm(datosPerfil);
                setInitialForm(datosPerfil);

                localStorage.setItem("usuarioId", String(usuarioEncontrado.id));
                localStorage.setItem("username", usuarioEncontrado.username || usernameSesion || "");
                localStorage.setItem("userUsername", usuarioEncontrado.username || usernameSesion || "");
                localStorage.setItem("nombre", usuarioEncontrado.nombre || "");
                localStorage.setItem("userNombre", usuarioEncontrado.nombre || "");
                localStorage.setItem("rol", usuarioEncontrado.rol || rolSesion || "USER");

                try {
                    const reportesResponse = await api.get("/api/reportes");
                    const reportes = reportesResponse.data || [];

                    const reportesUsuario = reportes
                        .filter((reporte) => String(reporte.usuarioId) === String(usuarioEncontrado.id))
                        .sort((a, b) => new Date(b.fechaReporte) - new Date(a.fechaReporte));

                    setMisReportes(reportesUsuario);
                } catch (reportesError) {
                    console.error("Error cargando historial de reportes:", reportesError);
                    setMisReportes([]);
                }
            } catch (err) {
                setError(
                    err.response?.data?.error ||
                    err.response?.data?.mensaje ||
                    "No se pudo cargar la información del perfil."
                );
            } finally {
                setLoadingPerfil(false);
                setLoadingReportes(false);
            }
        };

        cargarPerfil();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value,
        });

        setMensaje("");
        setError("");
    };

    const resetForm = () => {
        setForm(initialForm);
        setMensaje("");
        setError("");
    };

    const cerrarSesion = () => {
        localStorage.clear();
        navigate("/login");
    };

    const guardarCambios = async (e) => {
        e.preventDefault();

        const usuarioIdActual = usuarioActual?.id || usuarioIdSesion;

        if (!usuarioIdActual) {
            setError("No se encontró el ID del usuario en la sesión. Vuelve a iniciar sesión.");
            return;
        }

        if (!form.nombre.trim()) {
            setError("El nombre es obligatorio.");
            return;
        }

        if (!form.email.trim()) {
            setError("El correo electrónico es obligatorio para actualizar el perfil.");
            return;
        }

        setLoading(true);
        setMensaje("");
        setError("");

        try {
            const payload = {
                nombre: form.nombre.trim(),
                apellido: form.apellido.trim(),
                email: form.email.trim(),
                telefono: form.telefono.trim(),
                rol: form.rol || rolSesion,
            };

            if (form.password.trim()) {
                payload.password = form.password.trim();
            }

            const response = await api.put(`/api/usuarios/${usuarioIdActual}`, payload);
            const usuarioActualizado = response.data;

            setUsuarioActual(usuarioActualizado);

            const datosActualizados = {
                nombre: usuarioActualizado.nombre || form.nombre,
                apellido: usuarioActualizado.apellido || form.apellido,
                email: usuarioActualizado.email || form.email,
                telefono: usuarioActualizado.telefono || form.telefono,
                password: "",
                rol: usuarioActualizado.rol || form.rol,
            };

            setForm(datosActualizados);
            setInitialForm(datosActualizados);

            localStorage.setItem("usuarioId", usuarioActualizado.id || usuarioIdActual);
            localStorage.setItem("username", usuarioActualizado.username || usernameVisible);
            localStorage.setItem("userUsername", usuarioActualizado.username || usernameVisible);
            localStorage.setItem("nombre", datosActualizados.nombre);
            localStorage.setItem("rol", datosActualizados.rol);

            setMensaje("Perfil actualizado correctamente.");
        } catch (err) {
            setError(
                err.response?.data?.error ||
                err.response?.data?.mensaje ||
                err.response?.data?.detalle ||
                "No se pudo actualizar el perfil."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="perfil-container">
            <Navbar active="perfil" showProfile={false} />
            <main className="perfil-content">
                <div className="perfil-header-wrapper">
                    <section className="perfil-header-card">
                        <div className="perfil-avatar">{inicial}</div>

                        <div className="perfil-header-info">
                            <span className="perfil-kicker">Centro de cuenta</span>
                            <h1>Mi cuenta GeoFire</h1>
                            <p>
                                Administra tus datos personales, credenciales y preferencias de
                                acceso dentro de la plataforma.
                            </p>
                        </div>
                    </section>
                </div>

                <section className="perfil-grid">
                    <aside className="perfil-info-card">
                        <div className="perfil-summary">
                            <div className="perfil-summary-avatar">{inicial}</div>

                            <h2>{loadingPerfil ? "Cargando..." : nombreVisible}</h2>
                            <p>@{usernameVisible}</p>

                            <span className="perfil-status">
                                <BadgeCheck size={16} />
                                Cuenta activa
                            </span>
                        </div>

                        <div className="perfil-info-list">
                            <div className="perfil-info-item">
                                <span>
                                    <UserRound size={16} />
                                    Username
                                </span>
                                <strong>{usernameVisible || "No disponible"}</strong>
                            </div>

                            <div className="perfil-info-item">
                                <span>
                                    <ShieldCheck size={16} />
                                    Rol
                                </span>
                                <strong className="perfil-role">
                                    {form.rol || rolSesion}
                                </strong>
                            </div>

                            <div className="perfil-info-item">
                                <span>
                                    <CalendarClock size={16} />
                                    Último acceso
                                </span>
                                <strong>{ultimoAccesoFormateado}</strong>
                            </div>
                        </div>

                        <div className="perfil-quick-actions">
                            <button
                                type="button"
                                className="perfil-quick-btn"
                                onClick={() => navigate("/dashboard")}
                            >
                                <LayoutDashboard size={17} />
                                Dashboard
                            </button>

                            <button
                                type="button"
                                className="perfil-quick-btn danger"
                                onClick={cerrarSesion}
                            >
                                <LogOut size={17} />
                                Cerrar sesión
                            </button>
                        </div>
                    </aside>

                    <form
                        className="perfil-form-card"
                        onSubmit={guardarCambios}
                        autoComplete="off"
                    >
                        <div className="perfil-form-title">
                            <div>
                                <span className="perfil-kicker">Configuración</span>
                                <h2>Editar datos de la cuenta</h2>
                            </div>
                        </div>

                        {mensaje && <div className="perfil-alert success">{mensaje}</div>}
                        {error && <div className="perfil-alert error">{error}</div>}

                        <div className="perfil-section">
                            <h3>
                                <UserRound size={18} />
                                Datos personales
                            </h3>

                            <div className="perfil-row">
                                <div className="perfil-input-group">
                                    <label>Nombre</label>
                                    <input
                                        name="nombre"
                                        value={form.nombre}
                                        onChange={handleChange}
                                        className="perfil-input"
                                        placeholder="Tu nombre"
                                        required
                                        disabled={loadingPerfil}
                                        autoComplete="off"
                                    />
                                </div>

                                <div className="perfil-input-group">
                                    <label>Apellido</label>
                                    <input
                                        name="apellido"
                                        value={form.apellido}
                                        onChange={handleChange}
                                        className="perfil-input"
                                        placeholder="Tu apellido"
                                        disabled={loadingPerfil}
                                        autoComplete="off"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="perfil-section">
                            <h3>
                                <Mail size={18} />
                                Datos de contacto
                            </h3>

                            <div className="perfil-input-group">
                                <label>Correo electrónico</label>
                                <div className="perfil-input-icon">
                                    <Mail size={18} />
                                    <input
                                        name="email"
                                        value={form.email}
                                        onChange={handleChange}
                                        className="perfil-input"
                                        placeholder="correo@ejemplo.com"
                                        type="email"
                                        required
                                        disabled={loadingPerfil}
                                        autoComplete="new-email"
                                    />
                                </div>
                            </div>

                            <div className="perfil-input-group">
                                <label>Teléfono</label>
                                <div className="perfil-input-icon">
                                    <Phone size={18} />
                                    <input
                                        name="telefono"
                                        value={form.telefono}
                                        onChange={handleChange}
                                        className="perfil-input"
                                        placeholder="+56 9 1234 5678"
                                        disabled={loadingPerfil}
                                        autoComplete="off"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="perfil-section">
                            <h3>
                                <Lock size={18} />
                                Seguridad
                            </h3>

                            <div className="perfil-input-group">
                                <label>Nueva contraseña</label>
                                <div className="perfil-input-icon">
                                    <Lock size={18} />
                                    <input
                                        name="password"
                                        value={form.password}
                                        onChange={handleChange}
                                        className="perfil-input"
                                        placeholder="Dejar vacío para mantener la actual"
                                        type="password"
                                        disabled={loadingPerfil}
                                        autoComplete="new-password"
                                    />
                                </div>
                            </div>

                            <p className="perfil-helper">
                                Solo escribe una nueva contraseña si deseas cambiarla. Si dejas
                                este campo vacío, se mantendrá la contraseña actual.
                            </p>
                        </div>

                        <div className="perfil-actions">
                            <button
                                type="button"
                                className="btn-secundario"
                                onClick={resetForm}
                                disabled={loading || loadingPerfil}
                            >
                                <RotateCcw size={17} />
                                Restablecer
                            </button>

                            <button
                                type="submit"
                                className="btn-guardar-perfil"
                                disabled={loading || loadingPerfil}
                            >
                                <Save size={17} />
                                {loading ? "Guardando..." : "Guardar cambios"}
                            </button>
                        </div>
                    </form>
                </section>

                <section className="table-card">
                    <div className="table-header">
                        <h2>Historial de mis reportes</h2>
                    </div>

                    {loadingReportes ? (
                        <p>Cargando historial de reportes...</p>
                    ) : misReportes.length === 0 ? (
                        <p>Aún no has generado reportes desde esta cuenta.</p>
                    ) : (
                        <div className="table-responsive">
                            <table className="reports-table">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Fecha</th>
                                    <th>Ubicación</th>
                                    <th>Descripción</th>
                                    <th>Prioridad</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>

                                <tbody>
                                {misReportes.map((reporte) => (
                                    <tr key={reporte.id}>
                                        <td>#{reporte.id}</td>

                                        <td>
                                            {reporte.fechaReporte
                                                ? new Date(reporte.fechaReporte).toLocaleString("es-CL")
                                                : "Sin fecha"}
                                        </td>

                                        <td>
                                            {formatearCoordenadas(reporte.latitud, reporte.longitud)}
                                        </td>

                                        <td>{reporte.descripcion}</td>

                                        <td>
                                                <span
                                                    className={`badge-riesgo ${obtenerClasePrioridad(
                                                        reporte.prioridad
                                                    )}`}
                                                >
                                                    {reporte.prioridad || "SIN PRIORIDAD"}
                                                </span>
                                        </td>

                                        <td>
                                                <span
                                                    className={`badge-riesgo ${obtenerClaseEstado(
                                                        reporte.estado
                                                    )}`}
                                                >
                                                    {reporte.estado || "SIN ESTADO"}
                                                </span>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}