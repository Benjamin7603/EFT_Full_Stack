import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../assets/logo.png';
import './Registro.css';

export default function Registro() {
    const navigate = useNavigate();
    const [alertMessage, setAlertMessage] = useState('');
    const [showAlert, setShowAlert] = useState(false);

    const [formData, setFormData] = useState({
        nombre: '',
        apellido: '',
        username: '',
        email: '',
        password: '',
        activo: true,
        rol: 'USER',
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const showError = (msg) => {
        setAlertMessage(msg);
        setShowAlert(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validaciones
        if (formData.nombre.length < 3) {
            showError('El nombre debe tener al menos 3 caracteres.');
            return;
        }
        if (!formData.email.includes('@')) {
            showError('El correo electrónico no es válido.');
            return;
        }
        if (formData.password.length < 6) {
            showError('La contraseña debe tener al menos 6 caracteres.');
            return;
        }

        try {
            // Llamada real al backend a través del Gateway
            await axios.post('http://localhost:8000/api/usuarios', formData);

            // Guardamos en localStorage para que el Dashboard lo lea
            localStorage.setItem('userNombre', formData.nombre);
            localStorage.setItem('userUsername', formData.username);

            alert('¡Cuenta creada con éxito! Redirigiendo al inicio de sesión...');
            navigate('/login');
        } catch (error) {
            console.error('Error al registrar:', error);
            const msg = error.response?.data?.mensaje
                || error.response?.data?.error
                || 'No se pudo conectar con el servidor. ¿Está corriendo el Gateway en el puerto 8000?';
            showError(msg);
        }
    };

    return (
        <div className="registro-container">
            <div className="registro-card">

                <img src={logoImg} alt="Logo GeoFire" className="registro-logo" />
                <h1 className="registro-title">Crear cuenta en GeoFire</h1>
                <p className="registro-subtitle">Únete a nuestra red de reportes geográficos</p>

                {/* Alerta de error */}
                {showAlert && (
                    <div className="registro-alert">
                        <span>⚠️ {alertMessage}</span>
                        <button className="alert-close" onClick={() => setShowAlert(false)}>✕</button>
                    </div>
                )}

                <form className="registro-form" onSubmit={handleSubmit}>

                    {/* Nombre y Apellido en una fila */}
                    <div className="form-row">
                        <div className="input-group">
                            <label htmlFor="nombre">Nombre</label>
                            <input
                                type="text"
                                id="nombre"
                                name="nombre"
                                className="input-field"
                                placeholder="Ej: Juan"
                                value={formData.nombre}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="input-group">
                            <label htmlFor="apellido">Apellido</label>
                            <input
                                type="text"
                                id="apellido"
                                name="apellido"
                                className="input-field"
                                placeholder="Ej: García"
                                value={formData.apellido}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="input-group">
                        <label htmlFor="username">Nombre de Usuario</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            className="input-field"
                            placeholder="Ej: juangarcia"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="email">Correo Electrónico</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            className="input-field"
                            placeholder="Ej: juan@gmail.com"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Contraseña</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            className="input-field"
                            placeholder="Mínimo 6 caracteres"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <button type="submit" className="registro-btn">
                        🚀 Registrarse
                    </button>

                </form>

                <div className="registro-footer">
                    ¿Ya tienes una cuenta?{' '}
                    <Link to="/login" className="registro-link">Inicia sesión aquí</Link>
                </div>

            </div>
        </div>
    );
}