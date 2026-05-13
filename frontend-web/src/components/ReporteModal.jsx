// src/components/ReporteModal.jsx
import { useState, useRef } from 'react';
import axios from 'axios';
import './ReporteModal.css';

export default function ReporteModal({ onClose, onReporteCreado }) {
    const [enviando, setEnviando] = useState(false);
    const [exito, setExito] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');
    const [showError, setShowError] = useState(false);

    // Estado del archivo adjunto
    const [archivo, setArchivo] = useState(null);       // File object
    const [preview, setPreview] = useState(null);       // URL local para mostrar imagen
    const [esVideo, setEsVideo] = useState(false);
    const fileInputRef = useRef(null);

    const [formData, setFormData] = useState({
        latitud: '',
        longitud: '',
        descripcion: '',
        tipoUsuario: 'CIUDADANO',
        usuarioId: 1, // TODO: reemplazar con el ID real del usuario autenticado (viene del JWT)
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleArchivoChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        // Validar tamaño máximo 20MB
        if (file.size > 20 * 1024 * 1024) {
            mostrarError('El archivo no puede superar los 20MB.');
            return;
        }

        setArchivo(file);
        setEsVideo(file.type.startsWith('video/'));
        setPreview(URL.createObjectURL(file));
    };

    const eliminarArchivo = () => {
        setArchivo(null);
        setPreview(null);
        setEsVideo(false);
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const mostrarError = (msg) => {
        setErrorMsg(msg);
        setShowError(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validaciones frontend
        const lat = parseFloat(formData.latitud);
        const lng = parseFloat(formData.longitud);

        if (isNaN(lat) || lat < -90 || lat > 90) {
            mostrarError('La latitud debe ser un número entre -90 y 90.');
            return;
        }
        if (isNaN(lng) || lng < -180 || lng > 180) {
            mostrarError('La longitud debe ser un número entre -180 y 180.');
            return;
        }
        if (formData.descripcion.trim().length < 5) {
            mostrarError('La descripción debe tener al menos 5 caracteres.');
            return;
        }

        setEnviando(true);
        setShowError(false);

        try {
            // Por ahora enviamos JSON con urlMedia null (o el nombre del archivo como referencia).
            // Cuando el backend tenga un endpoint de subida de archivos (S3, Cloudinary, etc.),
            // aquí se subirá primero el archivo y se obtendrá la URL real.
            const payload = {
                latitud: lat,
                longitud: lng,
                descripcion: formData.descripcion.trim(),
                tipoUsuario: formData.tipoUsuario,
                urlMedia: archivo ? archivo.name : null, // TODO: reemplazar por URL real tras subida
                usuarioId: formData.usuarioId,
            };

            await axios.post('http://localhost:8000/api/reportes', payload);

            setExito(true);

            // Avisamos al Dashboard para que refresque la tabla automáticamente
            setTimeout(() => {
                onReporteCreado();
                onClose();
            }, 2000);

        } catch (error) {
            console.error('Error al enviar reporte:', error);

            // Manejo de errores del GlobalExceptionHandler de Spring
            const data = error.response?.data;
            if (data && typeof data === 'object') {
                const mensajes = Object.values(data).join(' | ');
                mostrarError(mensajes);
            } else {
                mostrarError('Error al conectar con el servidor. ¿Está corriendo el Gateway?');
            }
        } finally {
            setEnviando(false);
        }
    };

    // Cerrar al hacer click fuera del modal
    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) onClose();
    };

    return (
        <div className="modal-overlay" onClick={handleOverlayClick}>
            <div className="modal-card">

                {/* Header */}
                <div className="modal-header">
                    <div className="modal-header-text">
                        <h2>🚨 Nuevo Reporte</h2>
                        <p>Ingresa los datos del incendio detectado</p>
                    </div>
                    <button className="modal-close" onClick={onClose} aria-label="Cerrar">✕</button>
                </div>

                {/* Estado de éxito */}
                {exito ? (
                    <div className="modal-success">
                        <span className="success-icon">✅</span>
                        <h3>¡Reporte enviado!</h3>
                        <p>El equipo de emergencias ha sido notificado. Actualizando dashboard...</p>
                    </div>
                ) : (
                    <form className="modal-form" onSubmit={handleSubmit}>

                        {/* Error */}
                        {showError && (
                            <div className="modal-alert">
                                <span>⚠️ {errorMsg}</span>
                                <button type="button" className="alert-close" onClick={() => setShowError(false)}>✕</button>
                            </div>
                        )}

                        {/* Coordenadas */}
                        <div className="modal-row">
                            <div className="input-group">
                                <label htmlFor="latitud">Latitud</label>
                                <input
                                    type="number"
                                    id="latitud"
                                    name="latitud"
                                    className="input-field"
                                    placeholder="-33.4569"
                                    step="any"
                                    value={formData.latitud}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="input-group">
                                <label htmlFor="longitud">Longitud</label>
                                <input
                                    type="number"
                                    id="longitud"
                                    name="longitud"
                                    className="input-field"
                                    placeholder="-70.6483"
                                    step="any"
                                    value={formData.longitud}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        {/* Descripción */}
                        <div className="input-group">
                            <label htmlFor="descripcion">Descripción del Incidente</label>
                            <textarea
                                id="descripcion"
                                name="descripcion"
                                className="input-field"
                                placeholder="Ej: Incendio forestal en sector norponiente, llamas visibles a 500m..."
                                value={formData.descripcion}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        {/* Tipo de usuario */}
                        <div className="input-group">
                            <label htmlFor="tipoUsuario">Tipo de Reporte</label>
                            <select
                                id="tipoUsuario"
                                name="tipoUsuario"
                                className="input-field"
                                value={formData.tipoUsuario}
                                onChange={handleChange}
                            >
                                <option value="CIUDADANO">👤 Ciudadano</option>
                                <option value="OFICIAL">🛡️ Oficial (Prioridad ALTA)</option>
                            </select>
                        </div>

                        {/* Adjuntar foto o video */}
                        <div className="input-group">
                            <label>Foto / Video del Incidente (opcional)</label>

                            {/* Zona de drop / click */}
                            {!archivo ? (
                                <div
                                    className="file-dropzone"
                                    onClick={() => fileInputRef.current?.click()}
                                    onDragOver={(e) => e.preventDefault()}
                                    onDrop={(e) => {
                                        e.preventDefault();
                                        const file = e.dataTransfer.files[0];
                                        if (file) handleArchivoChange({ target: { files: [file] } });
                                    }}
                                >
                                    <span className="file-dropzone-icon">📎</span>
                                    <span className="file-dropzone-text">
                                        Arrastra un archivo aquí o <strong>haz click para seleccionar</strong>
                                    </span>
                                    <span className="file-dropzone-hint">Imágenes o videos · Máx. 20MB</span>
                                </div>
                            ) : (
                                <div className="file-preview">
                                    {esVideo ? (
                                        <video src={preview} className="file-preview-media" controls />
                                    ) : (
                                        <img src={preview} alt="Vista previa" className="file-preview-media" />
                                    )}
                                    <div className="file-preview-info">
                                        <span className="file-preview-name">📄 {archivo.name}</span>
                                        <span className="file-preview-size">
                                            {(archivo.size / 1024 / 1024).toFixed(2)} MB
                                        </span>
                                        <button
                                            type="button"
                                            className="file-preview-remove"
                                            onClick={eliminarArchivo}
                                        >
                                            ✕ Quitar
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* Input oculto */}
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/*,video/*"
                                capture="environment"
                                style={{ display: 'none' }}
                                onChange={handleArchivoChange}
                            />
                        </div>

                        {/* Botones */}
                        <div className="modal-footer">
                            <button type="button" className="btn-cancel" onClick={onClose}>
                                Cancelar
                            </button>
                            <button type="submit" className="btn-submit" disabled={enviando}>
                                {enviando ? '⏳ Enviando...' : '🔥 Enviar Reporte'}
                            </button>
                        </div>

                    </form>
                )}
            </div>
        </div>
    );
}