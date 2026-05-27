import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import logoImg from '../assets/logo.png';
import { LogOut } from "lucide-react";
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

    // WEATHER
    const [weather, setWeather] = useState(null);

    // BUSCADOR
    const [busqueda, setBusqueda] = useState("");
    const [resultadosBusqueda, setResultadosBusqueda] = useState([]);

    useEffect(() => {

        const storedNombre = localStorage.getItem('userNombre');
        const storedUsername = localStorage.getItem('userUsername');

        if (storedUsername) {
            setUserName(storedUsername);
            setUserInitial(storedUsername.charAt(0).toUpperCase());
        }
        else if (storedNombre) {
            setUserName(storedNombre);
            setUserInitial(storedNombre.charAt(0).toUpperCase());
        }

        fetchReportes();
        fetchWeather();

    }, []);

    const tokenConfig = () => {

        const token = localStorage.getItem('token');

        return {
            headers: {
                Authorization: `Bearer ${token}`
            }
        };
    };

    const fetchReportes = async () => {

        try {

            const response = await axios.get(
                'http://localhost:8000/api/reportes',
                tokenConfig()
            );

            setReportes(response.data);

        }
        catch (error) {

            console.error("Error cargando reportes:", error);
        }
    };

    // WEATHER API
    const fetchWeather = async () => {

        try {

            const response = await axios.get(
                "https://api.open-meteo.com/v1/forecast",
                {
                    params: {
                        latitude: -41.4693,
                        longitude: -72.9424,
                        current: [
                            "temperature_2m",
                            "relative_humidity_2m",
                            "wind_speed_10m",
                            "uv_index",
                            "weather_code",
                            "precipitation"
                        ].join(","),
                        timezone: "auto"
                    }
                }
            );

            setWeather(response.data.current);

        }
        catch (error) {

            console.error("Error obteniendo clima:", error);
        }
    };

    const handleLogout = () => {

        localStorage.clear();
        navigate('/login');
    };

    // BUSCADOR OPEN STREET MAP
    const buscarDireccion = async () => {

        if (!busqueda) return;

        try {

            const response = await axios.get(
                `https://nominatim.openstreetmap.org/search?format=json&q=${busqueda}&addressdetails=1&limit=5`
            );

            setResultadosBusqueda(response.data);

        }
        catch (error) {

            console.error("Error buscando dirección:", error);
        }
    };

    const seleccionarUbicacion = (lat, lon, nombreLugar) => {

        setNuevoReporte({
            ...nuevoReporte,
            latitud: parseFloat(lat),
            longitud: parseFloat(lon)
        });

        setBusqueda(nombreLugar);

        setResultadosBusqueda([]);
    };

    const handleEnviarReporte = async (e) => {

        e.preventDefault();

        try {

            const payload = {
                ...nuevoReporte,
                urlMedia: "",
                usuarioId: parseInt(localStorage.getItem('userId')) || 1
            };

            await axios.post(
                'http://localhost:8000/api/reportes',
                payload,
                tokenConfig()
            );

            alert("✅ ¡Reporte enviado correctamente!");

            setMostrarModal(false);

            setNuevoReporte({
                ...nuevoReporte,
                descripcion: "",
                prioridad: "MEDIA"
            });

            setBusqueda("");

            fetchReportes();

        }
        catch (error) {

            console.error("Error al enviar reporte:", error.response || error);

            if (error.response?.status === 400 && error.response?.data) {

                const mensajes = Object.values(error.response.data).join('\n');

                alert("⚠️ Error en el formulario:\n" + mensajes);
            }
            else if (
                error.response?.status === 401 ||
                error.response?.status === 403
            ) {

                alert("🔒 Permiso Denegado. Vuelve a iniciar sesión.");
            }
            else {

                alert("🚨 Error conectando con el servidor.");
            }
        }
    };

    const reportesFiltrados = reportes.filter(rep => {

        const pasaEstado =
            filtroEstado === "TODOS" ||
            rep.estado === filtroEstado;

        const pasaPrioridad =
            filtroPrioridad === "TODOS" ||
            rep.prioridad === filtroPrioridad;

        return pasaEstado && pasaPrioridad;
    });

    // RIESGO SIMPLE
    const calcularRiesgo = () => {

        if (!weather) return "Moderado";

        const temp = weather.temperature_2m;
        const viento = weather.wind_speed_10m;
        const humedad = weather.relative_humidity_2m;
        const uv = weather.uv_index;

        if (
            temp >= 30 ||
            viento >= 25 ||
            humedad <= 30 ||
            uv >= 8
        ) {
            return "Alto";
        }

        if (
            temp >= 24 ||
            viento >= 15 ||
            humedad <= 50 ||
            uv >= 5
        ) {
            return "Moderado";
        }

        return "Bajo";
    };

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

                    <button
                        onClick={handleLogout}
                        className="btn-logout"
                    >
                        <LogOut size={20} />
                    </button>
                </div>

            </nav>

            <main className="dash-content">

                <div className="dashboard-header">

                    <div>
                        <h1>Panel de Control GeoFire</h1>

                        <p>
                            Monitoreo en tiempo real de incendios y emergencias.
                        </p>
                    </div>

                    <button
                        className="btn-nuevo-reporte"
                        onClick={() => setMostrarModal(true)}
                    >
                        + NUEVO REPORTE
                    </button>

                </div>

                {weather && (

                    <div className="weather-banner">

                        <div className="weather-title">
                            ☀️ Condiciones Climáticas Puerto Montt
                        </div>

                        <div className="weather-content">

                            <div className="weather-item weather-temp">

                                <div className="weather-main">

                                    <strong>
                                        {Math.round(weather.temperature_2m)}°C
                                    </strong>

                                </div>

                                <small>TEMPERATURA</small>

                            </div>

                            <div className="weather-item  weather-hide-mobile">

                                <div className="weather-main">

                                    <span className="weather-icon">💨</span>

                                    <span>
                            {Math.round(weather.wind_speed_10m)} km/h
                        </span>

                                </div>

                                <small>VELOCIDAD VIENTO</small>

                            </div>

                            <div className="weather-item  weather-hide-mobile">

                                <div className="weather-main">

                                    <span className="weather-icon">💧</span>

                                    <span>
                            {weather.relative_humidity_2m}%
                        </span>

                                </div>

                                <small>HUMEDAD</small>

                            </div>

                            <div className="weather-item">

                                <div className="weather-main">

                                    <span className="weather-icon">⛅</span>

                                    <span>Despejado</span>

                                </div>

                                <small>CONDICIÓN CLIMÁTICA</small>

                            </div>

                            <div className="weather-item">

                                <div className="weather-main">

                                    <span className="weather-icon">☀️</span>

                                    <span>
                            UV {weather.uv_index}
                        </span>

                                </div>

                                <small>RADIACIÓN UV</small>

                            </div>

                            <div className="weather-item  weather-hide-mobile">

                                <div className="weather-main">

                                    <span className="weather-icon">🌧️</span>

                                    <span>
                            {weather.precipitation} mm
                        </span>

                                </div>

                                <small>PRECIPITACIÓN</small>

                            </div>

                            <div
                                className={`weather-item weather-risk risk-${calcularRiesgo().toLowerCase()}`}
                            >

                                <div className="weather-main">

                                    <span className="weather-icon">🔥</span>

                                    <span>
                            Riesgo {calcularRiesgo()}
                        </span>

                                </div>

                                <small>NIVEL DE RIESGO</small>

                            </div>

                        </div>

                    </div>


                )}

                <div className="filters-container">

                    <div className="filter-group">

                        <label>Estado:</label>

                        <select
                            className="filter-select"
                            value={filtroEstado}
                            onChange={(e) => setFiltroEstado(e.target.value)}
                        >
                            <option value="TODOS">Todos los Estados</option>
                            <option value="NUEVO">Nuevo</option>
                            <option value="EN_PROGRESO">En Progreso</option>
                            <option value="RESUELTO">Resuelto</option>
                        </select>

                    </div>

                    <div className="filter-group">

                        <label>Prioridad:</label>

                        <select
                            className="filter-select"
                            value={filtroPrioridad}
                            onChange={(e) => setFiltroPrioridad(e.target.value)}
                        >
                            <option value="TODOS">Todas las Prioridades</option>
                            <option value="ALTA">Alta</option>
                            <option value="MEDIA">Media</option>
                            <option value="BAJA">Baja</option>
                        </select>

                    </div>

                </div>

                <section className="map-section">

                    <div className="map-card">

                        <div className="map-badge">
                            📍 Mapa de Incidentes
                        </div>

                        <MapContainer
                            center={[-41.4693, -72.9424]}
                            zoom={13}
                            style={{
                                height: '400px',
                                width: '100%',
                                borderRadius: '12px'
                            }}
                        >

                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; OpenStreetMap contributors'
                            />

                            {reportesFiltrados.map((rep) => (

                                <Marker
                                    key={rep.id}
                                    position={[rep.latitud, rep.longitud]}
                                >

                                    <Popup>
                                        <strong>{rep.descripcion}</strong>
                                        <br />
                                        Prioridad: {rep.prioridad}
                                        <br />
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
                                <p>
                                    {
                                        reportes.filter(
                                            r => r.estado !== 'RESUELTO'
                                        ).length
                                    }
                                </p>
                            </div>

                        </div>

                        <div className="stat-card">

                            <span className="stat-icon">✅</span>

                            <div className="stat-info">
                                <h3>Incidentes Resueltos</h3>
                                <p>
                                    {
                                        reportes.filter(
                                            r => r.estado === 'RESUELTO'
                                        ).length
                                    }
                                </p>
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
                                <th>Ubicación</th>
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

                                    <td>
                                        {new Date(rep.fechaReporte)
                                            .toLocaleString('es-CL')}
                                    </td>

                                    <td>
                                        {rep.latitud}, {rep.longitud}
                                    </td>

                                    <td>{rep.descripcion}</td>

                                    <td>{rep.tipoUsuario}</td>

                                    <td>

                                        <span
                                            className={`badge-riesgo ${
                                                rep.prioridad === 'ALTA'
                                                    ? 'proceso'
                                                    : rep.prioridad === 'BAJA'
                                                        ? 'resuelto'
                                                        : 'nuevo'
                                            }`}
                                        >
                                            {rep.prioridad}
                                        </span>

                                    </td>

                                    <td>

                                        <span
                                            className={`badge-riesgo ${
                                                rep.estado === 'NUEVO'
                                                    ? 'nuevo'
                                                    : rep.estado === 'EN_PROGRESO'
                                                        ? 'proceso'
                                                        : 'resuelto'
                                            }`}
                                        >
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
            {/* MODAL CON NUEVA BÚSQUEDA */}
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

                            {/* --- AQUÍ ESTÁ EL NUEVO BUSCADOR --- */}
                            <div className="input-group">
                                <label>Buscar Ubicación</label>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <input
                                        type="text"
                                        className="input-field"
                                        placeholder="Ej: Mall Paseo Costanera, Puerto Montt"
                                        value={busqueda}
                                        onChange={(e) => setBusqueda(e.target.value)}
                                        style={{ flex: 1 }}
                                    />
                                    <button
                                        type="button"
                                        onClick={buscarDireccion}
                                        className="btn-guardar"
                                        style={{ width: 'auto', padding: '10px 20px', display: 'flex', alignItems: 'center' }}
                                    >
                                        🔍 Buscar
                                    </button>
                                </div>

                                {/* Resultados desplegables */}
                                {resultadosBusqueda.length > 0 && (
                                    <ul style={{
                                        background: 'white', border: '1px solid #ccc',
                                        borderRadius: '8px', listStyle: 'none',
                                        padding: '0', marginTop: '5px', maxHeight: '150px',
                                        overflowY: 'auto'
                                    }}>
                                        {resultadosBusqueda.map((lugar) => (
                                            <li
                                                key={lugar.place_id}
                                                style={{ padding: '10px', borderBottom: '1px solid #eee', cursor: 'pointer', fontSize: '0.9rem' }}
                                                onClick={() => seleccionarUbicacion(lugar.lat, lugar.lon, lugar.display_name)}
                                            >
                                                {lugar.display_name}
                                            </li>
                                        ))}
                                    </ul>
                                )}
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