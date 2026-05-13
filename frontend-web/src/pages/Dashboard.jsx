// src/pages/Dashboard.jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../assets/logo.png';
import ReporteModal from '../components/ReporteModal';
import './Dashboard.css';

export default function Dashboard() {
    const navigate = useNavigate();
    const [userName, setUserName] = useState('Usuario');
    const [userInitial, setUserInitial] = useState('U');
    const [reportes, setReportes] = useState([]);
    const [mostrarModal, setMostrarModal] = useState(false);

    useEffect(() => {
        const storedUsername = localStorage.getItem('userUsername');
        const storedNombre   = localStorage.getItem('userNombre');

        if (storedUsername) {
            setUserName(storedUsername);
            setUserInitial(storedUsername.charAt(0).toUpperCase());
        } else if (storedNombre) {
            setUserName(storedNombre);
            setUserInitial(storedNombre.charAt(0).toUpperCase());
        }

        fetchReportes();
    }, []);

    const fetchReportes = async () => {
        try {
            const response = await axios.get('http://localhost:8000/api/reportes');
            setReportes(response.data);
        } catch (error) {
            console.error('Error cargando reportes. ¿Está el Gateway corriendo?', error);
        }
    };

    // Callback que llama el modal cuando el POST fue exitoso
    const handleReporteCreado = () => {
        fetchReportes(); // refresca la tabla automáticamente
    };

    const handleLogout = () => {
        localStorage.removeItem('userNombre');
        localStorage.removeItem('userUsername');
        navigate('/');
    };

    // Contadores derivados de los datos reales
    const reportesActivos  = reportes.filter(r => r.estado === 'NUEVO' || r.estado === 'EN_PROGRESO').length;
    const reportesResueltos = reportes.filter(r => r.estado === 'RESUELTO').length;

    return (
        <div className="dashboard-container">

            {/* Modal — se renderiza encima de todo cuando mostrarModal es true */}
            {mostrarModal && (
                <ReporteModal
                    onClose={() => setMostrarModal(false)}
                    onReporteCreado={handleReporteCreado}
                />
            )}

            {/* Navbar */}
            <nav className="dash-navbar">
                <div className="nav-brand">
                    <img src={logoImg} alt="Logo" />
                    <h2>GeoFire</h2>
                </div>
                <div className="nav-user">
                    <div className="avatar">{userInitial}</div>
                    <button className="logout-btn" onClick={handleLogout}>Cerrar Sesión</button>
                </div>
            </nav>

            <main className="dash-content">

                {/* Banner de Bienvenida */}
                <div className="glass-card welcome-banner">
                    <div className="avatar-large">{userInitial}</div>
                    <div className="welcome-text">
                        <h1>¡Hola, {userName}!</h1>
                        <p>Bienvenido a tu panel GeoFire de gestión de alertas geográficas en tiempo real.</p>
                    </div>
                </div>

                {/* Estadísticas — ahora con datos reales */}
                <div className="stats-grid">
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">🔥</span>
                            <span>Total Reportes</span>
                        </div>
                        <h3 className="stat-value">{reportes.length}</h3>
                    </div>
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">⚡</span>
                            <span>Activos / En Progreso</span>
                        </div>
                        <h3 className="stat-value">{reportesActivos}</h3>
                    </div>
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">✅</span>
                            <span>Resueltos</span>
                        </div>
                        <h3 className="stat-value">{reportesResueltos}</h3>
                    </div>
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">🚑</span>
                            <span>Unidades en Alerta</span>
                        </div>
                        <h3 className="stat-value">0</h3>
                    </div>
                </div>

                {/* Sección Media */}
                <div className="middle-grid">

                    {/* Acciones Rápidas */}
                    <div className="glass-card actions-card">
                        <h3>Acciones Rápidas</h3>
                        {/* Al hacer click abre el modal */}
                        <button
                            className="btn-action btn-primary"
                            onClick={() => setMostrarModal(true)}
                        >
                            🚨 Reportar Incendio
                        </button>
                        <button className="btn-action btn-secondary">
                            🔔 Ver Notificaciones
                        </button>
                        <hr style={{ width: '100%', border: 'none', borderTop: '1px solid #eee', margin: '15px 0' }} />
                        <div style={{ display: 'flex', gap: '10px', color: '#666', fontSize: '0.9rem' }}>
                            <span style={{ cursor: 'pointer' }}>⚙️ Configuración</span>
                            <span style={{ cursor: 'pointer' }}>❓ Ayuda</span>
                        </div>
                    </div>

                    {/* Mapa simulado */}
                    <div className="glass-card map-card">
                        <div className="map-badge">📍 Mapa de Alertas Activas</div>
                        <p style={{ fontStyle: 'italic', color: '#666', fontSize: '1.2rem' }}>
                            [ El mapa interactivo de Google se cargará aquí ]
                        </p>
                    </div>

                </div>

                {/* Tabla de Reportes */}
                <div className="glass-card table-card">
                    <div className="table-header">
                        <h2>Últimos Reportes Recibidos</h2>
                        <button className="logout-btn" onClick={fetchReportes}>🔄 Actualizar</button>
                    </div>

                    {reportes.length === 0 ? (
                        <div className="empty-state">
                            <h3>No hay reportes aún.</h3>
                            <p>Cuando crees un reporte aparecerá aquí automáticamente.</p>
                            <button
                                className="btn-action btn-secondary"
                                style={{ marginTop: '15px', padding: '10px 20px' }}
                                onClick={() => setMostrarModal(true)}
                            >
                                🚨 Crear mi primer reporte
                            </button>
                        </div>
                    ) : (
                        <div style={{ overflowX: 'auto' }}>
                            <table className="reports-table">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Fecha</th>
                                    <th>Ubicación (Lat, Lng)</th>
                                    <th>Descripción</th>
                                    <th>Tipo</th>
                                    <th>Prioridad</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>
                                <tbody>
                                {reportes.map((rep) => (
                                    <tr key={rep.id}>
                                        <td>#{rep.id}</td>
                                        <td>{new Date(rep.fechaReporte).toLocaleString('es-CL')}</td>
                                        <td>{rep.latitud}, {rep.longitud}</td>
                                        <td>{rep.descripcion}</td>
                                        <td>{rep.tipoUsuario}</td>
                                        <td>
                                                <span className={`badge-riesgo ${rep.prioridad === 'ALTA' ? 'proceso' : 'nuevo'}`}>
                                                    {rep.prioridad}
                                                </span>
                                        </td>
                                        <td>
                                                <span className={`badge-riesgo ${
                                                    rep.estado === 'NUEVO'       ? 'nuevo'   :
                                                        rep.estado === 'EN_PROGRESO' ? 'proceso' : 'resuelto'
                                                }`}>
                                                    {rep.estado}
                                                </span>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>

            </main>
        </div>
    );
}