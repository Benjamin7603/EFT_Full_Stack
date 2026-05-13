import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../assets/logo.png';
import './Dashboard.css'; // Importamos la magia visual

export default function Dashboard() {
    const navigate = useNavigate();
    const [userName, setUserName] = useState("Usuario");
    const [userInitial, setUserInitial] = useState("U");

    // Estado para guardar los reportes que vengan de Java
    const [reportes, setReportes] = useState([]);

    useEffect(() => {
        // 1. Obtener nombre del usuario simulado
        const storedNombre = localStorage.getItem('userNombre');
        const storedUsername = localStorage.getItem('userUsername');

        if (storedUsername) {
            setUserName(storedUsername);
            setUserInitial(storedUsername.charAt(0).toUpperCase());
        } else if (storedNombre) {
            setUserName(storedNombre);
            setUserInitial(storedNombre.charAt(0).toUpperCase());
        }

        // 2. CONEXIÓN REAL AL BACKEND: Buscar reportes
        fetchReportes();
    }, []);

    const fetchReportes = async () => {
        try {
            // Llama a tu microservicio pasando por el Gateway
            const response = await axios.get('http://localhost:8000/api/reportes');
            setReportes(response.data);
        } catch (error) {
            console.error("Error cargando reportes. Asegúrate de que el Gateway y ms-reportes estén encendidos.", error);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('userNombre');
        localStorage.removeItem('userUsername');
        navigate('/');
    };

    return (
        <div className="dashboard-container">
            {/* Barra de Navegación */}
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

            {/* Contenido Principal */}
            <main className="dash-content">

                {/* Banner de Bienvenida */}
                <div className="glass-card welcome-banner">
                    <div className="avatar-large">{userInitial}</div>
                    <div className="welcome-text">
                        <h1>¡Hola, {userName}!</h1>
                        <p>Bienvenido a tu panel GeoFire de gestión de alertas geográficas en tiempo real.</p>
                    </div>
                </div>

                {/* Estadísticas (Grid 4 columnas) */}
                <div className="stats-grid">
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">🔥</span>
                            <span>Alertas Activas Hoy</span>
                        </div>
                        <h3 className="stat-value">{reportes.length}</h3>
                    </div>
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">📝</span>
                            <span>Tus Reportes</span>
                        </div>
                        <h3 className="stat-value">0</h3>
                    </div>
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">🛡️</span>
                            <span>Personal Operativo</span>
                        </div>
                        <h3 className="stat-value">0</h3>
                    </div>
                    <div className="glass-card stat-item">
                        <div className="stat-header">
                            <span className="stat-icon">🚑</span>
                            <span>Unidades en Alerta</span>
                        </div>
                        <h3 className="stat-value">0</h3>
                    </div>
                </div>

                {/* Sección Media (Acciones y Mapa) */}
                <div className="middle-grid">

                    {/* Acciones Rápidas */}
                    <div className="glass-card actions-card">
                        <h3>Acciones Rápidas</h3>
                        <button className="btn-action btn-primary">
                            🚨 Reportar Incendio
                        </button>
                        <button className="btn-action btn-secondary">
                            🔔 Ver Notificaciones
                        </button>
                        <hr style={{width: '100%', border: 'none', borderTop: '1px solid #eee', margin: '15px 0'}} />
                        <div style={{display: 'flex', gap: '10px', color: '#666', fontSize: '0.9rem'}}>
                            <span style={{cursor:'pointer'}}>⚙️ Configuración</span>
                            <span style={{cursor:'pointer'}}>❓ Ayuda</span>
                        </div>
                    </div>

                    {/* Mapa Simulado */}
                    <div className="glass-card map-card">
                        <div className="map-badge">📍 Mapa de Alertas Activas</div>
                        <p style={{fontStyle: 'italic', color: '#666', fontSize: '1.2rem'}}>
                            [ El mapa interactivo de Google se cargará aquí ]
                        </p>
                    </div>

                </div>

                {/* Tabla de Reportes Real */}
                <div className="glass-card table-card">
                    <div className="table-header">
                        <h2>Últimos Reportes Recibidos</h2>
                        <button className="logout-btn" onClick={fetchReportes}>🔄 Actualizar</button>
                    </div>

                    {reportes.length === 0 ? (
                        <div className="empty-state">
                            <h3>No hay reportes en la base de datos en este momento.</h3>
                            <p>Cuando crees un reporte en la aplicación, aparecerá mágicamente aquí.</p>
                            <button className="btn-action btn-secondary" style={{marginTop: '15px', padding: '10px 20px'}}>
                                Crear mi primer reporte
                            </button>
                        </div>
                    ) : (
                        <div style={{overflowX: 'auto'}}>
                            <table className="reports-table">
                                <thead>
                                <tr>
                                    <th>ID Alerta</th>
                                    <th>Fecha</th>
                                    <th>Ubicación (Lat, Lng)</th>
                                    <th>Descripción</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>
                                <tbody>
                                {reportes.map((rep) => (
                                    <tr key={rep.id}>
                                        <td>#{rep.id}</td>
                                        <td>{new Date(rep.fechaReporte).toLocaleString()}</td>
                                        <td>{rep.latitud}, {rep.longitud}</td>
                                        <td>{rep.descripcion}</td>
                                        <td>
                                                <span className={`badge-riesgo ${rep.estado.toLowerCase()}`}>
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