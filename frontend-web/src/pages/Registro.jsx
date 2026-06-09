import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../assets/logo.png';
import Swal from 'sweetalert2';
import './Registro.css';
import '../App.css';

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
            await axios.post('http://localhost:8000/api/usuarios', formData);

            localStorage.setItem('userNombre', formData.nombre);
            localStorage.setItem('userUsername', formData.username);

            try {
                const loginResponse = await axios.post('http://localhost:8000/api/auth/login', {
                    username: formData.username,
                    password: formData.password
                });

                if (loginResponse.data.token) {
                    localStorage.setItem('token', loginResponse.data.token);

                    Swal.fire({
                        title: '¡Registro Exitoso!',
                        text: 'Tu cuenta ha sido creada e iniciamos sesión automáticamente por ti. ¡Bienvenido a GeoFire!',
                        icon: 'success',
                        confirmButtonColor: '#FF7043',
                        background: 'rgba(255, 255, 255, 0.95)',
                        backdrop: `rgba(255, 112, 67, 0.2) blur(5px)`
                    }).then(() => {
                        navigate('/dashboard');
                    });
                    return;
                }
            } catch (loginError) {
                console.error('Error en login automático post-registro:', loginError);

                Swal.fire({
                    title: 'Usuario Creado',
                    text: 'Tu cuenta fue registrada con éxito, pero necesitaremos que inicies sesión manualmente.',
                    icon: 'info',
                    confirmButtonColor: '#FFAB40'
                }).then(() => {
                    navigate('/login');
                });
                return;
            }

            Swal.fire({
                title: 'Cuenta Creada',
                text: 'Redirigiendo al inicio de sesión...',
                icon: 'success',
                confirmButtonColor: '#FF7043'
            }).then(() => {
                navigate('/login');
            });

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

                {showAlert && (
                    <div className="registro-alert">
                        <span>⚠️ {alertMessage}</span>
                        <button className="alert-close" onClick={() => setShowAlert(false)}>✕</button>
                    </div>
                )}

                <form className="registro-form" onSubmit={handleSubmit}>
                    <div className="form-row">
                        <div className="input-group">
                            <label htmlFor="nombre">Nombre</label>
                            <input
                                type="text" id="nombre" name="nombre" className="input-field"
                                placeholder="Ej: Juan" value={formData.nombre} onChange={handleChange} required
                            />
                        </div>
                        <div className="input-group">
                            <label htmlFor="apellido">Apellido</label>
                            <input
                                type="text" id="apellido" name="apellido" className="input-field"
                                placeholder="Ej: García" value={formData.apellido} onChange={handleChange} required
                            />
                        </div>
                    </div>

                    <div className="input-group">
                        <label htmlFor="username">Nombre de Usuario</label>
                        <input
                            type="text" id="username" name="username" className="input-field"
                            placeholder="Ej: juangarcia" value={formData.username} onChange={handleChange} required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="email">Correo Electrónico</label>
                        <input
                            type="email" id="email" name="email" className="input-field"
                            placeholder="Ej: juan@gmail.com" value={formData.email} onChange={handleChange} required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Contraseña</label>
                        <input
                            type="password" id="password" name="password" className="input-field"
                            placeholder="Mínimo 6 caracteres" value={formData.password} onChange={handleChange} required
                        />
                    </div>

                    <button type="submit" className="registro-btn">🚀 Registrarse</button>
                </form>

                <div className="registro-footer">
                    ¿Ya tienes una cuenta?{' '}
                    <Link to="/login" className="registro-link">Inicia sesión aquí</Link>
                </div>
            </div>
        </div>
    );
}