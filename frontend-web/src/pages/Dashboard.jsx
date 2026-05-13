import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import logoImg from '../assets/logo.png';
import './Dashboard.css';

import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

export default function Dashboard() {
    const navigate = useNavigate();
    const [userName, setUserName] = useState("Usuario");
    const [userInitial, setUserInitial] = useState("U");
    const [reportes, setReportes] = useState([]);

    const [filtroEstado, setFiltroEstado] = useState("TODOS");
    const [filtroPrioridad, setFiltroPrioridad] = useState("TODOS");

    const [mostrarModal, setMostrarModal] = useState(false);
    const [nuevoReporte, setNuevoReporte] = useState({
        descripcion: "",
        latitud: -41.4693,
        longitud: -72.9424,
        prioridad: "MEDIA",
        tipoUsuario: "CIUDADANO",
        usuarioId: 1
    });

    useEffect(() => {
        const storedNombre = localStorage.getItem('userNombre');
        const storedUsername = localStorage.getItem('userUsername');

        if (storedUsername) {
            setUserName(storedUsername);
            setUserInitial(storedUsername.charAt(0).toUpperCase());
        } else if (storedNombre) {
            setUserName(storedNombre);
            setUserInitial(storedNombre.charAt(0).toUpperCase());
        }

        fetchReportes();
    }, []);

    // 1. FUNCIÓN CLAVE: Adjunta el Token de seguridad a las peticiones de Axios
    const tokenConfig = () => {
        const token = localStorage.getItem('token');
        return {
            headers: {
                Authorization: `Bearer ${token}`
            }
        };
    };

    // 2. FETCH SEGURO: Pide los reportes enviando el Token a Java
    const fetchReportes = async () => {
        try {
            const response = await axios.get('http://localhost:8000/api/reportes', tokenConfig());
            setReportes(response.data);
        } catch (error) {
            console.error("Error cargando reportes:", error);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/login');
    };

    // 3. ENVÍO SEGURO Y VALIDADO DEL DTO
    const handleEnviarReporte = async (e) => {
        e.preventDefault();
        try {
            // Preparamos los datos tal y como los pide el DTO en Java
            const payload = {
                ...nuevoReporte,
                urlMedia: "", // Para que el backend no rechace el DTO
                usuarioId: parseInt(localStorage.getItem('userId')) || 1 // Idealmente amarrado al login
            };

            await axios.post('http://localhost:8000/api/reportes', payload, tokenConfig());

            alert("✅ ¡Reporte enviado correctamente!");
            setMostrarModal(false);

            // Limpia descripción y prioridad, mantiene latitud y longitud
            setNuevoReporte({ ...nuevoReporte, descripcion: "", prioridad: "MEDIA" });

            fetchReportes();
        } catch (error) {
            console.error("Error al enviar reporte:", error.response || error);

            // Manejador Inteligente de Errores:
            if (error.response?.status === 400 && error.response?.data) {
                // Lee el JSON de errores del GlobalExceptionHandler y lo muestra bonito
                const mensajes = Object.values(error.response.data).join('\n');
                alert("⚠️ Error en el formulario:\n" + mensajes);
            }
            else if (error.response?.status === 401 || error.response?.status === 403) {
                alert("🔒 Permiso Denegado. Por favor, cierra sesión y vuelve a ingresar para validar tu Token.");
            }
            else {
                alert("🚨 Hubo un error al conectar con el servidor. Revisa si Java está corriendo.");
            }
        }
    };

    const reportesFiltrados = reportes.filter(rep => {
        const pasaEstado = filtroEstado === "TODOS" || rep.estado === filtroEstado;
        const pasaPrioridad = filtroPrioridad === "TODOS" || rep.prioridad === filtroPrioridad;
        return pasaEstado && pasaPrioridad;
    });

    return (
        <div className="dashboard-container">
            <nav className="dash-navbar">
                <div className="nav-brand">
                    <img src={logoImg} alt="GeoFire" />
                    <h2>GeoFire</h2>
                </div>
                <div className="user-profile">
                    <div className="avatar">{userInitial}</div>
                    <span>Hola, {userName}</span>
                    <button onClick={handleLogout} className="btn-logout">Salir</button>
                </div>
            </nav>

            <main className="dash-content">
                <div className="dashboard-header">
                    <div>
                        <h1>Panel de Control GeoFire</h1>
                        <p>Monitoreo en tiempo real de incendios y emergencias.</p>
                    </div>
                    <button className="btn-nuevo-reporte" onClick={() => setMostrarModal(true)}>
                        + NUEVO REPORTE
                    </button>
                </div>

                <div className="filters-container">
                    <div className="filter-group">
                        <label>Estado:</label>
                        <select className="filter-select" value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                            <option value="TODOS">Todos los Estados</option>
                            <option value="NUEVO">Nuevo</option>
                            <option value="EN_PROGRESO">En Progreso</option>
                            <option value="RESUELTO">Resuelto</option>
                        </select>
                    </div>
                    <div className="filter-group">
                        <label>Prioridad:</label>
                        <select className="filter-select" value={filtroPrioridad} onChange={(e) => setFiltroPrioridad(e.target.value)}>
                            <option value="TODOS">Todas las Prioridades</option>
                            <option value="ALTA">Alta</option>
                            <option value="MEDIA">Media</option>
                            <option value="BAJA">Baja</option>
                        </select>
                    </div>
                </div>

                <section className="map-section">
                    <div className="map-card">
                        <div className="map-badge">📍 Mapa de Incidentes</div>
                        <MapContainer center={[-41.4693, -72.9424]} zoom={13} style={{ height: '400px', width: '100%', borderRadius: '12px' }}>
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; OpenStreetMap contributors'
                            />
                            {reportesFiltrados.map((rep) => (
                                <Marker key={rep.id} position={[rep.latitud, rep.longitud]}>
                                    <Popup>
                                        <strong>{rep.descripcion}</strong><br />
                                        Prioridad: {rep.prioridad}<br />
                                        Estado: {rep.estado}
                                    </Popup>
                                </Marker>
                            ))}
                        </MapContainer>
                    </div>

                    <div className="stats-grid">
                        <div className="stat-card">
                            <span className="stat-icon">🔥</span>
                            <div className="stat-info">
                                <h3>Alertas Activas</h3>
                                <p>{reportes.filter(r => r.estado !== 'RESUELTO').length}</p>
                            </div>
                        </div>
                        <div className="stat-card">
                            <span className="stat-icon">✅</span>
                            <div className="stat-info">
                                <h3>Incidentes Resueltos</h3>
                                <p>{reportes.filter(r => r.estado === 'RESUELTO').length}</p>
                            </div>
                        </div>
                        <div className="stat-card">
                            <span className="stat-icon">👥</span>
                            <div className="stat-info">
                                <h3>Total Reportes</h3>
                                <p>{reportes.length}</p>
                            </div>
                        </div>
                    </div>
                </section>

                <div className="table-card">
                    <div className="table-header">
                        <h2>Historial de Reportes</h2>
                    </div>
                    {reportesFiltrados.length === 0 ? (
                        <p>No hay reportes que coincidan con los filtros.</p>
                    ) : (
                        <table className="reports-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Fecha</th>
                                <th>Ubicación (Lat, Lng)</th>
                                <th>Descripción</th>
                                <th>Tipo Usuario</th>
                                <th>Prioridad</th>
                                <th>Estado</th>
                            </tr>
                            </thead>
                            <tbody>
                            {reportesFiltrados.map((rep) => (
                                <tr key={rep.id}>
                                    <td>#{rep.id}</td>
                                    <td>{new Date(rep.fechaReporte).toLocaleString('es-CL')}</td>
                                    <td>{rep.latitud}, {rep.longitud}</td>
                                    <td>{rep.descripcion}</td>
                                    <td>{rep.tipoUsuario}</td>
                                    <td>
                                            <span className={`badge-riesgo ${rep.prioridad === 'ALTA' ? 'proceso' : rep.prioridad === 'BAJA' ? 'resuelto' : 'nuevo'}`}>
                                                {rep.prioridad}
                                            </span>
                                    </td>
                                    <td>
                                            <span className={`badge-riesgo ${
                                                rep.estado === 'NUEVO' ? 'nuevo' :
                                                    rep.estado === 'EN_PROGRESO' ? 'proceso' : 'resuelto'
                                            }`}>
                                                {rep.estado}
                                            </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </main>

            {/* MODAL */}
            {mostrarModal && (
                <div className="modal-overlay">
                    <div className="modal-card">
                        <div className="modal-header">
                            <h2>Crear Alerta Geográfica</h2>
                            <button className="close-btn" onClick={() => setMostrarModal(false)}>&times;</button>
                        </div>
                        <form className="modal-form" onSubmit={handleEnviarReporte}>
                            <div className="input-group">
                                <label>Descripción de la emergencia</label>
                                <input
                                    type="text"
                                    className="input-field"
                                    placeholder="Ej: Incendio forestal cerca de la ruta 5"
                                    value={nuevoReporte.descripcion}
                                    onChange={(e) => setNuevoReporte({...nuevoReporte, descripcion: e.target.value})}
                                    required
                                />
                            </div>
                            <div className="location-inputs">
                                <div className="input-group">
                                    <label>Latitud</label>
                                    <input
                                        type="number" step="any"
                                        className="input-field"
                                        value={nuevoReporte.latitud}
                                        onChange={(e) => setNuevoReporte({...nuevoReporte, latitud: parseFloat(e.target.value)})}
                                        required
                                    />
                                </div>
                                <div className="input-group">
                                    <label>Longitud</label>
                                    <input
                                        type="number" step="any"
                                        className="input-field"
                                        value={nuevoReporte.longitud}
                                        onChange={(e) => setNuevoReporte({...nuevoReporte, longitud: parseFloat(e.target.value)})}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="input-group">
                                <label>Prioridad</label>
                                <select
                                    className="input-field"
                                    value={nuevoReporte.prioridad}
                                    onChange={(e) => setNuevoReporte({...nuevoReporte, prioridad: e.target.value})}
                                >
                                    <option value="ALTA">Alta</option>
                                    <option value="MEDIA">Media</option>
                                    <option value="BAJA">Baja</option>
                                </select>
                            </div>

                            <div className="modal-actions">
                                <button type="button" className="btn-cancelar" onClick={() => setMostrarModal(false)}>Cancelar</button>
                                <button type="submit" className="btn-guardar">Enviar Reporte</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}