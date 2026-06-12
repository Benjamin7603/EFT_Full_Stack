import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import api from "../api/api";
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import Swal from 'sweetalert2';

import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

import 'leaflet/dist/leaflet.css';
import './Dashboard.css';
import '../App.css';
import Navbar from "../components/Navbar.jsx";

let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});

L.Marker.prototype.options.icon = DefaultIcon;

export default function Dashboard() {
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

    const getUsuarioIdSesion = () => {
        return (
            localStorage.getItem("usuarioId") ||
            localStorage.getItem("userId") ||
            tokenData.usuarioId ||
            null
        );
    };

    const usuarioIdSesion = getUsuarioIdSesion();


    const [reportes, setReportes] = useState([]);
    const [reportesEstadisticas, setReportesEstadisticas] = useState([]);

    const [filtroEstado, setFiltroEstado] = useState("TODOS");
    const [filtroPrioridad, setFiltroPrioridad] = useState("TODOS");

    const [mostrarModal, setMostrarModal] = useState(false);

    const [nuevoReporte, setNuevoReporte] = useState({
        descripcion: "",
        latitud: null,
        longitud: null,
        prioridad: "MEDIA",
        tipoUsuario: "CIUDADANO",
        usuarioId: usuarioIdSesion ? Number(usuarioIdSesion) : null
    });

    const [weather, setWeather] = useState(null);

    const [busqueda, setBusqueda] = useState("");
    const [resultadosBusqueda, setResultadosBusqueda] = useState([]);

    const [errorModal, setErrorModal] = useState("");


    const fetchReportes = async () => {
        try {
            const [activosResponse, todosResponse] = await Promise.all([
                api.get("/api/reportes/activos"),
                api.get("/api/reportes")
            ]);

            setReportes(activosResponse.data || []);
            setReportesEstadisticas(todosResponse.data || []);
        }
        catch (error) {
            console.error("Error cargando reportes:", error);
        }
    };

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

    const resetFormularioReporte = () => {
        const usuarioIdActual = getUsuarioIdSesion();

        setNuevoReporte({
            descripcion: "",
            latitud: null,
            longitud: null,
            prioridad: "MEDIA",
            tipoUsuario: "CIUDADANO",
            usuarioId: usuarioIdActual ? Number(usuarioIdActual) : null
        });

        setBusqueda("");
        setResultadosBusqueda([]);
        setErrorModal("");
    };

    useEffect(() => {
        const inicializarDashboard = async () => {


            if (tokenData.usuarioId && !localStorage.getItem("usuarioId")) {
                localStorage.setItem("usuarioId", tokenData.usuarioId);
            }

            if (tokenData.username && !localStorage.getItem("username")) {
                localStorage.setItem("username", tokenData.username);
                localStorage.setItem("userUsername", tokenData.username);
            }

            if (tokenData.rol && !localStorage.getItem("rol")) {
                localStorage.setItem("rol", tokenData.rol);
            }

            await fetchReportes();
            await fetchWeather();
        };

        inicializarDashboard();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [navigate]);

    useEffect(() => {
        if (!busqueda || busqueda.trim().length < 3) {
            setResultadosBusqueda([]);
            return;
        }

        const delayDebounceFn = setTimeout(async () => {
            try {
                const response = await axios.get(
                    `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(busqueda)}&addressdetails=1&limit=5&countrycodes=cl`
                );

                const resultadosChile = response.data || [];

                setResultadosBusqueda(resultadosChile);

                if (resultadosChile.length === 0) {
                    setErrorModal("No se encontraron ubicaciones dentro de Chile.");
                }
            }
            catch (error) {
                console.error("Error buscando dirección en tiempo real:", error);
            }
        }, 500);

        return () => clearTimeout(delayDebounceFn);
    }, [busqueda]);



    const abrirModalReporte = () => {
        resetFormularioReporte();
        setMostrarModal(true);
    };

    const cerrarModalReporte = () => {
        setMostrarModal(false);
        resetFormularioReporte();
    };

    const seleccionarUbicacion = (lat, lon, nombreLugar) => {
        setNuevoReporte({
            ...nuevoReporte,
            latitud: parseFloat(lat),
            longitud: parseFloat(lon)
        });

        setBusqueda(nombreLugar);
        setResultadosBusqueda([]);
        setErrorModal("");
    };

    const handleEnviarReporte = async (e) => {
        e.preventDefault();

        const usuarioIdActual = getUsuarioIdSesion();

        if (!usuarioIdActual) {
            setErrorModal("No se encontró el ID del usuario en la sesión. Vuelve a iniciar sesión.");
            return;
        }

        if (!nuevoReporte.descripcion.trim()) {
            setErrorModal("Debes ingresar una descripción de la emergencia.");
            return;
        }

        if (!busqueda.trim()) {
            setErrorModal("Debes buscar y seleccionar una ubicación antes de enviar el reporte.");
            return;
        }

        if (nuevoReporte.latitud === null || nuevoReporte.longitud === null) {
            setErrorModal("Selecciona una ubicación válida desde la lista de resultados.");
            return;
        }

        try {
            const payload = {
                ...nuevoReporte,
                descripcion: nuevoReporte.descripcion.trim(),
                urlMedia: "",
                usuarioId: Number(usuarioIdActual)
            };

            await api.post("/api/reportes", payload);

            setMostrarModal(false);
            resetFormularioReporte();

            await fetchReportes();

            setTimeout(() => {
                Swal.fire({
                    icon: 'success',
                    title: '¡Reporte Enviado!',
                    text: 'La alerta de emergencia ha sido georreferenciada correctamente.',
                    confirmButtonColor: '#FF7043'
                });
            }, 150);
        }
        catch (error) {
            console.error("Error al enviar reporte:", error.response || error);

            if (error.response?.status === 400 && error.response?.data) {
                const mensajes = Object.values(error.response.data).join('\n');
                setErrorModal(mensajes);
            }
            else if (
                error.response?.status === 401 ||
                error.response?.status === 403
            ) {
                setErrorModal("Permiso denegado. Vuelve a iniciar sesión para continuar.");
            }
            else {
                setErrorModal("No se logró establecer conexión con el servidor de GeoFire.");
            }
        }
    };

    const reportesActivos = reportes.filter(rep => rep.estado !== "RESUELTO");

    const reportesFiltrados = reportesActivos.filter(rep => {
        const pasaEstado =
            filtroEstado === "TODOS" ||
            rep.estado === filtroEstado;

        const pasaPrioridad =
            filtroPrioridad === "TODOS" ||
            rep.prioridad === filtroPrioridad;

        return pasaEstado && pasaPrioridad;
    });

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

    return (
        <div className="dashboard-container">
            <Navbar active="dashboard" showDashboard={false} />
            <main className="dash-content">
                <div className="dashboard-header">
                    <div>
                        <h1>Panel de Control GeoFire</h1>
                        <p>Monitoreo en tiempo real de incendios y emergencias.</p>
                    </div>

                    <button
                        className="btn-nuevo-reporte"
                        onClick={abrirModalReporte}
                        type="button"
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

                            <div className="weather-item weather-hide-mobile">
                                <div className="weather-main">
                                    <span className="weather-icon">💨</span>
                                    <span>
                                        {Math.round(weather.wind_speed_10m)} km/h
                                    </span>
                                </div>
                                <small>VELOCIDAD VIENTO</small>
                            </div>

                            <div className="weather-item weather-hide-mobile">
                                <div className="weather-main">
                                    <span className="weather-icon">💧</span>
                                    <span>{weather.relative_humidity_2m}%</span>
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
                                    <span>UV {weather.uv_index}</span>
                                </div>
                                <small>RADIACIÓN UV</small>
                            </div>

                            <div className="weather-item weather-hide-mobile">
                                <div className="weather-main">
                                    <span className="weather-icon">🌧️</span>
                                    <span>{weather.precipitation} mm</span>
                                </div>
                                <small>PRECIPITACIÓN</small>
                            </div>

                            <div
                                className={`weather-item weather-risk risk-${calcularRiesgo().toLowerCase()}`}
                            >
                                <div className="weather-main">
                                    <span className="weather-icon">🔥</span>
                                    <span>Riesgo {calcularRiesgo()}</span>
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
                        <div className="map-badge">📍 Mapa de Incidentes</div>

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
                                <p>{reportesEstadisticas.filter(r => r.estado !== 'RESUELTO').length}</p>
                            </div>
                        </div>

                        <div className="stat-card">
                            <span className="stat-icon">✅</span>
                            <div className="stat-info">
                                <h3>Incidentes Resueltos</h3>
                                <p>{reportesEstadisticas.filter(r => r.estado === 'RESUELTO').length}</p>
                            </div>
                        </div>

                        <div className="stat-card">
                            <span className="stat-icon">👥</span>
                            <div className="stat-info">
                                <h3>Total Reportes</h3>
                                <p>{reportesEstadisticas.length}</p>
                            </div>
                        </div>
                    </div>
                </section>

                <div className="table-card">
                    <div className="table-header">
                        <h2>Reportes Activos</h2>
                    </div>

                    {reportesFiltrados.length === 0 ? (
                        <p>No hay reportes activos que coincidan con los filtros.</p>
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
                                        {rep.fechaReporte
                                            ? new Date(rep.fechaReporte).toLocaleString('es-CL')
                                            : "Sin fecha"}
                                    </td>

                                    <td>
                                        {formatearCoordenadas(rep.latitud, rep.longitud)}
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
                                                {rep.prioridad || "MEDIA"}
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
                                                {rep.estado || "NUEVO"}
                                            </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </main>

            {mostrarModal && (
                <div className="modal-overlay">
                    <div className="modal-card">
                        <div className="modal-header">
                            <h2>Crear Alerta Geográfica</h2>

                            <button
                                className="close-btn"
                                onClick={cerrarModalReporte}
                                type="button"
                            >
                                &times;
                            </button>
                        </div>

                        <form className="modal-form" onSubmit={handleEnviarReporte}>
                            {errorModal && (
                                <div className="modal-inline-alert">
                                    ⚠️ {errorModal}
                                </div>
                            )}

                            <div className="input-group">
                                <label>Descripción de la emergencia</label>

                                <input
                                    type="text"
                                    className="input-field"
                                    placeholder="Ej: Incendio forestal cerca de la ruta 5"
                                    value={nuevoReporte.descripcion}
                                    onChange={(e) => {
                                        setNuevoReporte({
                                            ...nuevoReporte,
                                            descripcion: e.target.value
                                        });

                                        setErrorModal("");
                                    }}
                                />
                            </div>

                            <div className="input-group">
                                <label>Buscar Ubicación</label>

                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <input
                                        type="text"
                                        className="input-field"
                                        placeholder="Escribe para buscar una ubicación en Chile..."
                                        value={busqueda}
                                        onChange={(e) => {
                                            setBusqueda(e.target.value);
                                            setErrorModal("");
                                            setNuevoReporte({
                                                ...nuevoReporte,
                                                latitud: null,
                                                longitud: null
                                            });
                                        }}
                                        style={{ flex: 1 }}
                                    />
                                </div>

                                {resultadosBusqueda.length > 0 && (
                                    <ul
                                        style={{
                                            background: 'white',
                                            border: '1px solid #ccc',
                                            borderRadius: '8px',
                                            listStyle: 'none',
                                            padding: '0',
                                            marginTop: '5px',
                                            maxHeight: '150px',
                                            overflowY: 'auto'
                                        }}
                                    >
                                        {resultadosBusqueda.map((lugar, idx) => (
                                            <li
                                                key={lugar.place_id || idx}
                                                style={{
                                                    padding: '10px',
                                                    borderBottom: '1px solid #eee',
                                                    cursor: 'pointer',
                                                    fontSize: '0.9rem'
                                                }}
                                                onClick={() =>
                                                    seleccionarUbicacion(
                                                        lugar.lat,
                                                        lugar.lon,
                                                        lugar.display_name
                                                    )
                                                }
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
                                    onChange={(e) => {
                                        setNuevoReporte({
                                            ...nuevoReporte,
                                            prioridad: e.target.value
                                        });

                                        setErrorModal("");
                                    }}
                                >
                                    <option value="ALTA">Alta</option>
                                    <option value="MEDIA">Media</option>
                                    <option value="BAJA">Baja</option>
                                </select>
                            </div>

                            <div className="modal-actions">
                                <button
                                    type="button"
                                    className="btn-cancelar"
                                    onClick={cerrarModalReporte}
                                >
                                    Cancelar
                                </button>

                                <button
                                    type="submit"
                                    className="btn-guardar"
                                >
                                    Enviar Reporte
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}