import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    BarChart3,
    Edit,
    Flame,
    Plus,
    RefreshCcw,
    Save,
    ShieldCheck,
    Trash2,
    UserCog,
    Users,
    X,
} from "lucide-react";
import Swal from "sweetalert2";

import api from "../api/api";

import "./Dashboard.css";
import "./Admin.css";
import "../App.css";
import Navbar from "../components/Navbar.jsx";

const rolesDisponibles = [
    "ADMIN",
    "FUNCIONARIO",
    "BOMBERO",
    "BRIGADISTA",
    "USER",
];

const emptyUserForm = {
    nombre: "",
    apellido: "",
    email: "",
    telefono: "",
    username: "",
    password: "",
    rol: "USER",
};

export default function Admin() {
    const navigate = useNavigate();

    const [usuarios, setUsuarios] = useState([]);
    const [reportes, setReportes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [mostrarModal, setMostrarModal] = useState(false);
    const [modoModal, setModoModal] = useState("crear");
    const [usuarioEditando, setUsuarioEditando] = useState(null);
    const [formUsuario, setFormUsuario] = useState(emptyUserForm);

    const [mensaje, setMensaje] = useState("");
    const [error, setError] = useState("");


    const rolSesion = (localStorage.getItem("rol") || "USER").toUpperCase();
    const usuarioIdSesion =
        localStorage.getItem("usuarioId") ||
        localStorage.getItem("userId");


    const totalUsuarios = usuarios.length;

    const totalAdministradores = usuarios.filter(
        (usuario) => usuario.rol === "ADMIN"
    ).length;

    const totalOperativos = usuarios.filter((usuario) =>
        ["FUNCIONARIO", "BOMBERO", "BRIGADISTA"].includes(usuario.rol)
    ).length;

    const reportesActivos = reportes.filter(
        (reporte) => reporte.estado !== "RESUELTO"
    );

    const ultimosReportes = useMemo(() => {
        return [...reportes]
            .sort((a, b) => new Date(b.fechaReporte) - new Date(a.fechaReporte))
            .slice(0, 8);
    }, [reportes]);

    const ultimoAdministrador = (usuario) => {
        const admins = usuarios.filter((u) => u.rol === "ADMIN");
        return usuario?.rol === "ADMIN" && admins.length === 1;
    };

    useEffect(() => {
        if (rolSesion !== "ADMIN") {
            Swal.fire({
                icon: "warning",
                title: "Acceso restringido",
                text: "Solo los administradores pueden ingresar al panel de administración.",
                confirmButtonColor: "#FF7043",
            }).then(() => navigate("/dashboard"));

            return;
        }

        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        setLoading(true);
        setError("");
        setMensaje("");

        try {
            const [usuariosResponse, reportesResponse] = await Promise.all([
                api.get("/api/usuarios"),
                api.get("/api/reportes"),
            ]);

            setUsuarios(usuariosResponse.data || []);
            setReportes(reportesResponse.data || []);
        } catch (err) {
            console.error("Error cargando datos de administración:", err);
            setError(
                err.response?.data?.error ||
                err.response?.data?.mensaje ||
                "No se pudieron cargar los datos del panel administrador."
            );
        } finally {
            setLoading(false);
        }
    };



    const abrirCrearUsuario = () => {
        setModoModal("crear");
        setUsuarioEditando(null);
        setFormUsuario(emptyUserForm);
        setMensaje("");
        setError("");
        setMostrarModal(true);
    };

    const abrirEditarUsuario = (usuario) => {
        setModoModal("editar");
        setUsuarioEditando(usuario);

        setFormUsuario({
            nombre: usuario.nombre || "",
            apellido: usuario.apellido || "",
            email: usuario.email || "",
            telefono: usuario.telefono || "",
            username: usuario.username || "",
            password: "",
            rol: usuario.rol || "USER",
        });

        setMensaje("");
        setError("");
        setMostrarModal(true);
    };

    const cerrarModal = () => {
        setMostrarModal(false);
        setUsuarioEditando(null);
        setFormUsuario(emptyUserForm);
        setSaving(false);
    };

    const handleChange = (e) => {
        setFormUsuario({
            ...formUsuario,
            [e.target.name]: e.target.value,
        });

        setError("");
        setMensaje("");
    };

    const validarFormularioUsuario = () => {
        if (!formUsuario.nombre.trim()) {
            setError("El nombre del usuario es obligatorio.");
            return false;
        }

        if (!formUsuario.username.trim()) {
            setError("El username del usuario es obligatorio.");
            return false;
        }

        if (!formUsuario.email.trim()) {
            setError("El correo electrónico del usuario es obligatorio.");
            return false;
        }

        if (modoModal === "crear" && !formUsuario.password.trim()) {
            setError("La contraseña es obligatoria al crear un usuario.");
            return false;
        }

        if (!formUsuario.rol.trim()) {
            setError("Debes seleccionar un rol para el usuario.");
            return false;
        }

        return true;
    };

    const guardarUsuario = async (e) => {
        e.preventDefault();

        if (!validarFormularioUsuario()) {
            return;
        }

        setSaving(true);
        setError("");
        setMensaje("");
        if (
            modoModal === "editar" &&
            String(usuarioEditando?.id) === String(usuarioIdSesion) &&
            formUsuario.rol !== "ADMIN"
        ) {
            setError("No puedes quitarte el rol ADMIN a tu propia cuenta.");
            return;
        }
        if (
            modoModal === "editar" &&
            ultimoAdministrador(usuarioEditando) &&
            formUsuario.rol !== "ADMIN"
        ) {
            setError("No puedes quitar el rol ADMIN al último administrador del sistema.");
            return;
        }
        try {
            const payload = {
                nombre: formUsuario.nombre.trim(),
                apellido: formUsuario.apellido.trim(),
                email: formUsuario.email.trim(),
                telefono: formUsuario.telefono.trim(),
                username: formUsuario.username.trim(),
                rol: formUsuario.rol,
            };

            if (formUsuario.password.trim()) {
                payload.password = formUsuario.password.trim();
            }

            if (modoModal === "crear") {
                await api.post("/api/usuarios/admin", payload);
                setMensaje("Usuario creado correctamente.");
            } else {
                await api.put(`/api/usuarios/${usuarioEditando.id}`, payload);
                setMensaje("Usuario actualizado correctamente.");
            }

            cerrarModal();
            await cargarDatos();

            Swal.fire({
                icon: "success",
                title: modoModal === "crear" ? "Usuario creado" : "Usuario actualizado",
                text:
                    modoModal === "crear"
                        ? "El usuario fue registrado correctamente."
                        : "Los datos del usuario fueron actualizados.",
                confirmButtonColor: "#FF7043",
            });
        } catch (err) {
            console.error("Error guardando usuario:", err);
            setError(
                err.response?.data?.error ||
                err.response?.data?.mensaje ||
                err.response?.data?.detalle ||
                "No se pudo guardar el usuario."
            );
        } finally {
            setSaving(false);
        }
    };

    const eliminarUsuario = async (usuario) => {
        const usuarioIdSesion =
            localStorage.getItem("usuarioId") ||
            localStorage.getItem("userId");

        if (String(usuario.id) === String(usuarioIdSesion)) {
            Swal.fire({
                icon: "warning",
                title: "Acción no permitida",
                text: "No puedes eliminar tu propio usuario desde el panel administrador.",
                confirmButtonColor: "#FF7043",
            });

            return;
        }
        const confirmacion = await Swal.fire({
            icon: "warning",
            title: "¿Eliminar usuario?",
            text: `Se eliminará la cuenta de ${usuario.username}. Esta acción no se puede deshacer.`,
            showCancelButton: true,
            confirmButtonText: "Sí, eliminar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#dc3545",
            cancelButtonColor: "#6c757d",
        });

        if (!confirmacion.isConfirmed) {
            return;
        }

        try {
            await api.delete(`/api/usuarios/${usuario.id}`);
            await cargarDatos();

            Swal.fire({
                icon: "success",
                title: "Usuario eliminado",
                text: "La cuenta fue eliminada correctamente.",
                confirmButtonColor: "#FF7043",
            });
        } catch (err) {
            console.error("Error eliminando usuario:", err);

            Swal.fire({
                icon: "error",
                title: "No se pudo eliminar",
                text:
                    err.response?.data?.error ||
                    err.response?.data?.mensaje ||
                    "Ocurrió un error al eliminar el usuario.",
                confirmButtonColor: "#dc3545",
            });
        }
    };

    const actualizarEstadoReporte = async (idReporte, nuevoEstado) => {
        try {
            await api.patch(`/api/reportes/${idReporte}/estado`, null, {
                params: {
                    nuevoEstado,
                },
            });

            await cargarDatos();

            Swal.fire({
                icon: "success",
                title: "Estado actualizado",
                text: `El reporte #${idReporte} ahora está en estado ${nuevoEstado}.`,
                confirmButtonColor: "#FF7043",
            });
        } catch (err) {
            console.error("Error actualizando estado:", err);

            Swal.fire({
                icon: "error",
                title: "Error al actualizar",
                text:
                    err.response?.data?.error ||
                    err.response?.data?.mensaje ||
                    "No se pudo actualizar el estado del reporte.",
                confirmButtonColor: "#dc3545",
            });
        }
    };

    const getRoleClass = (rol) => {
        switch (rol) {
            case "ADMIN":
                return "admin-role admin";
            case "FUNCIONARIO":
                return "admin-role funcionario";
            case "BOMBERO":
                return "admin-role bombero";
            case "BRIGADISTA":
                return "admin-role brigadista";
            default:
                return "admin-role user";
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

    return (
        <div className="admin-container">
            <Navbar active="admin" showAdmin={false} />

            <main className="admin-content">
                <section className="admin-header-card">
                    <div>
                        <span className="admin-kicker">Panel administrativo</span>
                        <h1>Administración GeoFire</h1>
                        <p>
                            Gestiona usuarios, roles operativos y seguimiento general de
                            reportes dentro de la plataforma.
                        </p>
                    </div>

                    <button
                        className="btn-admin-refresh"
                        type="button"
                        onClick={cargarDatos}
                        disabled={loading}
                    >
                        <RefreshCcw size={18} />
                        {loading ? "Actualizando..." : "Actualizar"}
                    </button>
                </section>

                {error && <div className="admin-alert error">{error}</div>}
                {mensaje && <div className="admin-alert success">{mensaje}</div>}

                <section className="admin-stats-grid">
                    <div className="admin-stat-card">
                        <div className="admin-stat-icon">
                            <Users size={28} />
                        </div>
                        <div>
                            <span>Total usuarios</span>
                            <strong>{totalUsuarios}</strong>
                        </div>
                    </div>

                    <div className="admin-stat-card">
                        <div className="admin-stat-icon">
                            <ShieldCheck size={28} />
                        </div>
                        <div>
                            <span>Administradores</span>
                            <strong>{totalAdministradores}</strong>
                        </div>
                    </div>

                    <div className="admin-stat-card">
                        <div className="admin-stat-icon">
                            <UserCog size={28} />
                        </div>
                        <div>
                            <span>Equipo operativo</span>
                            <strong>{totalOperativos}</strong>
                        </div>
                    </div>

                    <div className="admin-stat-card">
                        <div className="admin-stat-icon danger">
                            <Flame size={28} />
                        </div>
                        <div>
                            <span>Reportes activos</span>
                            <strong>{reportesActivos.length}</strong>
                        </div>
                    </div>
                </section>

                <section className="admin-card">
                    <div className="admin-card-header">
                        <div>
                            <span className="admin-kicker">Usuarios</span>
                            <h2>Gestión de usuarios</h2>
                        </div>

                        <button
                            className="btn-admin-primary"
                            type="button"
                            onClick={abrirCrearUsuario}
                        >
                            <Plus size={18} />
                            Crear usuario
                        </button>
                    </div>

                    {loading ? (
                        <p className="admin-empty">Cargando usuarios...</p>
                    ) : usuarios.length === 0 ? (
                        <p className="admin-empty">No hay usuarios registrados.</p>
                    ) : (
                        <div className="admin-table-wrapper">
                            <table className="admin-table">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Username</th>
                                    <th>Email</th>
                                    <th>Teléfono</th>
                                    <th>Rol</th>
                                    <th>Acciones</th>
                                </tr>
                                </thead>

                                <tbody>
                                {usuarios.map((usuario) => (
                                    <tr key={usuario.id}>
                                        <td>#{usuario.id}</td>
                                        <td>
                                            {usuario.nombre} {usuario.apellido}
                                        </td>
                                        <td>@{usuario.username}</td>
                                        <td>{usuario.email}</td>
                                        <td>{usuario.telefono || "No registrado"}</td>
                                        <td>
                                                <span className={getRoleClass(usuario.rol)}>
                                                    {usuario.rol || "USER"}
                                                </span>
                                        </td>
                                        <td>
                                            <div className="admin-actions">
                                                <button
                                                    className="admin-action-btn edit"
                                                    type="button"
                                                    title="Editar usuario"
                                                    onClick={() => abrirEditarUsuario(usuario)}
                                                >
                                                    <Edit size={16} />
                                                </button>

                                                <button
                                                    className="admin-action-btn delete"
                                                    type="button"
                                                    title={
                                                        String(usuario.id) === String(usuarioIdSesion)
                                                            ? "No puedes eliminar tu propia cuenta"
                                                            : "Eliminar usuario"
                                                    }
                                                    disabled={String(usuario.id) === String(usuarioIdSesion)}
                                                    onClick={() => eliminarUsuario(usuario)}
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>

                <section className="admin-card">
                    <div className="admin-card-header">
                        <div>
                            <span className="admin-kicker">Operación</span>
                            <h2>Gestión de reportes recientes</h2>
                        </div>

                        <div className="admin-mini-label">
                            <BarChart3 size={17} />
                            Últimos {ultimosReportes.length} reportes
                        </div>
                    </div>

                    {loading ? (
                        <p className="admin-empty">Cargando reportes...</p>
                    ) : ultimosReportes.length === 0 ? (
                        <p className="admin-empty">No hay reportes registrados.</p>
                    ) : (
                        <div className="admin-table-wrapper">
                            <table className="admin-table">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Fecha</th>
                                    <th>Descripción</th>
                                    <th>Prioridad</th>
                                    <th>Estado</th>
                                    <th>Cambiar estado</th>
                                </tr>
                                </thead>

                                <tbody>
                                {ultimosReportes.map((reporte) => (
                                    <tr key={reporte.id}>
                                        <td>#{reporte.id}</td>
                                        <td>
                                            {reporte.fechaReporte
                                                ? new Date(reporte.fechaReporte).toLocaleString("es-CL")
                                                : "Sin fecha"}
                                        </td>
                                        <td>{reporte.descripcion}</td>
                                        <td>
                                                <span
                                                    className={`badge-riesgo ${getPrioridadClass(
                                                        reporte.prioridad
                                                    )}`}
                                                >
                                                    {reporte.prioridad || "MEDIA"}
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
                                                value={reporte.estado || "NUEVO"}
                                                onChange={(e) =>
                                                    actualizarEstadoReporte(
                                                        reporte.id,
                                                        e.target.value
                                                    )
                                                }
                                            >
                                                <option value="NUEVO">NUEVO</option>
                                                <option value="EN_PROGRESO">EN_PROGRESO</option>
                                                <option value="RESUELTO">RESUELTO</option>
                                            </select>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </main>

            {mostrarModal && (
                <div className="admin-modal-overlay">
                    <div className="admin-modal-card">
                        <div className="admin-modal-header">
                            <div>
                                <span className="admin-kicker">
                                    {modoModal === "crear" ? "Nuevo usuario" : "Editar usuario"}
                                </span>
                                <h2>
                                    {modoModal === "crear"
                                        ? "Crear usuario"
                                        : `Editar @${usuarioEditando?.username}`}
                                </h2>
                            </div>

                            <button
                                type="button"
                                className="admin-modal-close"
                                onClick={cerrarModal}
                            >
                                <X size={22} />
                            </button>
                        </div>

                        {error && <div className="admin-alert error">{error}</div>}

                        <form className="admin-form" onSubmit={guardarUsuario}>
                            <div className="admin-form-row">
                                <div className="admin-input-group">
                                    <label>Nombre</label>
                                    <input
                                        name="nombre"
                                        value={formUsuario.nombre}
                                        onChange={handleChange}
                                        placeholder="Nombre"
                                        className="admin-input"
                                    />
                                </div>

                                <div className="admin-input-group">
                                    <label>Apellido</label>
                                    <input
                                        name="apellido"
                                        value={formUsuario.apellido}
                                        onChange={handleChange}
                                        placeholder="Apellido"
                                        className="admin-input"
                                    />
                                </div>
                            </div>

                            <div className="admin-form-row">
                                <div className="admin-input-group">
                                    <label>Username</label>
                                    <input
                                        name="username"
                                        value={formUsuario.username}
                                        onChange={handleChange}
                                        placeholder="usuario"
                                        className="admin-input"
                                        disabled={modoModal === "editar"}
                                    />
                                </div>

                                <div className="admin-input-group">
                                    <label>Rol</label>
                                    <select
                                        name="rol"
                                        value={formUsuario.rol}
                                        onChange={handleChange}
                                        className="admin-input"
                                        disabled={
                                            modoModal === "editar" &&
                                            String(usuarioEditando?.id) === String(usuarioIdSesion)
                                        }
                                    >
                                        {rolesDisponibles.map((rol) => (
                                            <option key={rol} value={rol}>
                                                {rol}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            <div className="admin-input-group">
                                <label>Correo electrónico</label>
                                <input
                                    name="email"
                                    value={formUsuario.email}
                                    onChange={handleChange}
                                    placeholder="correo@ejemplo.com"
                                    type="email"
                                    className="admin-input"
                                />
                            </div>

                            <div className="admin-input-group">
                                <label>Teléfono</label>
                                <input
                                    name="telefono"
                                    value={formUsuario.telefono}
                                    onChange={handleChange}
                                    placeholder="+56 9 1234 5678"
                                    className="admin-input"
                                />
                            </div>

                            <div className="admin-input-group">
                                <label>
                                    {modoModal === "crear"
                                        ? "Contraseña"
                                        : "Nueva contraseña"}
                                </label>
                                <input
                                    name="password"
                                    value={formUsuario.password}
                                    onChange={handleChange}
                                    placeholder={
                                        modoModal === "crear"
                                            ? "Contraseña inicial"
                                            : "Dejar vacío para mantener la actual"
                                    }
                                    type="password"
                                    className="admin-input"
                                    autoComplete="new-password"
                                />
                            </div>

                            <div className="admin-modal-actions">
                                <button
                                    type="button"
                                    className="admin-btn-secondary"
                                    onClick={cerrarModal}
                                    disabled={saving}
                                >
                                    Cancelar
                                </button>

                                <button
                                    type="submit"
                                    className="btn-admin-primary"
                                    disabled={saving}
                                >
                                    <Save size={17} />
                                    {saving ? "Guardando..." : "Guardar usuario"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}