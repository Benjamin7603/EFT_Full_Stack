// eslint-disable-next-line no-unused-vars
import React from 'react';
// src/components/ReporteModal.jsx
import { useEffect, useState } from "react";
import axios from "axios";
import Swal from "sweetalert2";
import api from "../api/api";
import "./ReporteModal.css";

export default function ReporteModal({ onClose, onReporteCreado, getUsuarioIdSesion }) {
    const obtenerUsuarioId = () => {
        if (typeof getUsuarioIdSesion === "function") {
            return getUsuarioIdSesion();
        }

        try {
            const token = localStorage.getItem("token");

            if (!token) {
                return (
                    localStorage.getItem("usuarioId") ||
                    localStorage.getItem("userId") ||
                    null
                );
            }

            const payload = token.split(".")[1];
            const decoded = JSON.parse(atob(payload));

            return (
                localStorage.getItem("usuarioId") ||
                localStorage.getItem("userId") ||
                decoded.usuarioId ||
                null
            );
        } catch {
            return (
                localStorage.getItem("usuarioId") ||
                localStorage.getItem("userId") ||
                null
            );
        }
    };

    const obtenerRolSesion = () => {
        let rol = localStorage.getItem("rol");

        if (!rol) {
            try {
                const token = localStorage.getItem("token");

                if (token) {
                    const payload = token.split(".")[1];
                    const decoded = JSON.parse(atob(payload));
                    rol = decoded.rol;
                }
            } catch {
                rol = null;
            }
        }

        return rol ? rol.trim().toUpperCase() : "USER";
    };

    const esUsuarioOperativo = () => {
        return ["ADMIN", "BOMBERO", "BRIGADISTA", "FUNCIONARIO"].includes(obtenerRolSesion());
    };

    const obtenerTipoUsuario = () => {
        const rolNormalizado = obtenerRolSesion();

        if (rolNormalizado === "USER") {
            return "CIUDADANO";
        }

        return rolNormalizado;
    };

    const [nuevoReporte, setNuevoReporte] = useState({
        descripcion: "",
        latitud: null,
        longitud: null,
        prioridad: esUsuarioOperativo() ? "MEDIA" : "BAJA",
        tipoUsuario: obtenerTipoUsuario(),
        usuarioId: obtenerUsuarioId() ? Number(obtenerUsuarioId()) : null
    });

    const [busqueda, setBusqueda] = useState("");
    const [resultadosBusqueda, setResultadosBusqueda] = useState([]);
    const [errorModal, setErrorModal] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [buscandoUbicacion, setBuscandoUbicacion] = useState(false);
    const [ubicacionSeleccionada, setUbicacionSeleccionada] = useState(false);

    useEffect(() => {
        if (ubicacionSeleccionada) {
            return;
        }

        const textoBusqueda = busqueda.trim();

        if (textoBusqueda.length < 3) {
            setResultadosBusqueda([]);
            return;
        }

        const delayDebounceFn = setTimeout(async () => {
            try {
                setBuscandoUbicacion(true);
                setErrorModal("");

                const response = await axios.get("https://photon.komoot.io/api/", {
                    params: {
                        q: textoBusqueda,
                        limit: 8
                    }
                });

                const features = response.data?.features || [];

                const resultados = features
                    .map((feature) => {
                        const propiedades = feature.properties || {};
                        const coordenadas = feature.geometry?.coordinates || [];

                        const lon = coordenadas[0];
                        const lat = coordenadas[1];

                        const partesNombre = [
                            propiedades.name,
                            propiedades.street,
                            propiedades.city,
                            propiedades.county,
                            propiedades.state,
                            propiedades.country
                        ].filter(Boolean);

                        return {
                            id: `${lat}-${lon}-${propiedades.osm_id || Math.random()}`,
                            lat,
                            lon,
                            display_name: partesNombre.join(", "),
                            country: propiedades.country,
                            countrycode: propiedades.countrycode
                        };
                    })
                    .filter((lugar) => {
                        const esChile =
                            lugar.countrycode?.toLowerCase() === "cl" ||
                            lugar.country?.toLowerCase() === "chile";

                        return (
                            esChile &&
                            lugar.lat !== undefined &&
                            lugar.lon !== undefined &&
                            lugar.display_name
                        );
                    })
                    .slice(0, 5);

                setResultadosBusqueda(resultados);

                if (resultados.length === 0) {
                    setErrorModal("No se encontraron ubicaciones dentro de Chile.");
                }
            }
            catch (error) {
                console.error("Error buscando dirección en tiempo real:", error);
                setResultadosBusqueda([]);
                setErrorModal("No se pudo buscar la ubicación. Intenta nuevamente.");
            }
            finally {
                setBuscandoUbicacion(false);
            }
        }, 800);

        return () => clearTimeout(delayDebounceFn);
    }, [busqueda, ubicacionSeleccionada]);

    const seleccionarUbicacion = (lat, lon, nombreLugar) => {
        setUbicacionSeleccionada(true);

        setNuevoReporte((prev) => ({
            ...prev,
            latitud: parseFloat(lat),
            longitud: parseFloat(lon)
        }));

        setBusqueda(nombreLugar);
        setResultadosBusqueda([]);
        setErrorModal("");
    };

    const handleEnviarReporte = async (e) => {
        e.preventDefault();

        const usuarioIdActual = obtenerUsuarioId();

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
            setEnviando(true);

            const payload = {
                ...nuevoReporte,
                descripcion: nuevoReporte.descripcion.trim(),
                tipoUsuario: obtenerTipoUsuario(),
                prioridad: esUsuarioOperativo() ? nuevoReporte.prioridad : "BAJA",
                urlMedia: "",
                usuarioId: Number(usuarioIdActual)
            };

            await api.post("/api/reportes", payload);

            if (onReporteCreado) {
                await onReporteCreado();
            }

            onClose();

            setTimeout(() => {
                Swal.fire({
                    icon: "success",
                    title: "¡Reporte Enviado!",
                    text: "La alerta de emergencia ha sido georreferenciada correctamente.",
                    confirmButtonColor: "#FF7043"
                });
            }, 150);
        }
        catch (error) {
            console.error("Error al enviar reporte:", error.response || error);

            if (error.response?.status === 400 && error.response?.data) {
                const mensajes = Object.values(error.response.data).join("\n");
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
        finally {
            setEnviando(false);
        }
    };

    const cerrarModal = () => {
        if (!enviando) {
            onClose();
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-card">
                <div className="modal-header">
                    <h2>Crear Alerta Geográfica</h2>

                    <button
                        className="close-btn"
                        onClick={cerrarModal}
                        type="button"
                        disabled={enviando}
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
                            disabled={enviando}
                            onChange={(e) => {
                                setNuevoReporte((prev) => ({
                                    ...prev,
                                    descripcion: e.target.value
                                }));

                                setErrorModal("");
                            }}
                        />
                    </div>

                    <div className="input-group">
                        <label>Buscar Ubicación</label>

                        <div style={{ display: "flex", gap: "10px" }}>
                            <input
                                type="text"
                                className="input-field"
                                placeholder="Escribe para buscar una ubicación en Chile..."
                                value={busqueda}
                                disabled={enviando}
                                onChange={(e) => {
                                    setUbicacionSeleccionada(false);
                                    setBusqueda(e.target.value);
                                    setErrorModal("");
                                    setNuevoReporte((prev) => ({
                                        ...prev,
                                        latitud: null,
                                        longitud: null
                                    }));
                                }}
                                style={{ flex: 1 }}
                            />
                        </div>

                        {buscandoUbicacion && (
                            <small className="reporte-search-loading">
                                Buscando ubicaciones...
                            </small>
                        )}

                        {resultadosBusqueda.length > 0 && (
                            <ul className="reporte-resultados-lista">
                                {resultadosBusqueda.map((lugar, idx) => (
                                    <li
                                        key={lugar.id || idx}
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

                    {esUsuarioOperativo() && (
                        <div className="input-group">
                            <label>Prioridad</label>

                            <select
                                className="input-field"
                                value={nuevoReporte.prioridad}
                                disabled={enviando}
                                onChange={(e) => {
                                    setNuevoReporte((prev) => ({
                                        ...prev,
                                        prioridad: e.target.value
                                    }));

                                    setErrorModal("");
                                }}
                            >
                                <option value="ALTA">Alta</option>
                                <option value="MEDIA">Media</option>
                                <option value="BAJA">Baja</option>
                            </select>
                        </div>
                    )}

                    <div className="modal-actions">
                        <button
                            type="button"
                            className="btn-cancelar"
                            onClick={cerrarModal}
                            disabled={enviando}
                        >
                            Cancelar
                        </button>

                        <button
                            type="submit"
                            className="btn-guardar"
                            disabled={enviando}
                        >
                            {enviando ? "Enviando..." : "Enviar Reporte"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}