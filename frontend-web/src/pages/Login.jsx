// eslint-disable-next-line no-unused-vars
import React from 'react';
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useEffect } from "react";
import api from '../api/api';
import logoImg from '../assets/logo.png';
import Swal from 'sweetalert2';
import './Login.css';
import '../App.css';

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (token) {
            navigate("/dashboard", { replace: true });
        }
    }, [navigate]);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setLoading(true);

        try {
            const response = await api.post('/api/auth/login', {
                username: username,
                password: password
            });

            const data = response.data;

            if (data.token) {
                localStorage.setItem("token", data.token);
                localStorage.setItem("username", data.username || username);
                localStorage.setItem("userUsername", data.username || username);
                localStorage.setItem("rol", data.rol || "USER");
                localStorage.setItem("ultimoAcceso", new Date().toISOString());

                if (data.usuarioId !== undefined && data.usuarioId !== null) {
                    localStorage.setItem('usuarioId', data.usuarioId);
                }

                if (data.nombre) {
                    localStorage.setItem('nombre', data.nombre);
                } else {
                    localStorage.setItem('nombre', data.username || username);
                }

                Swal.fire({
                    title: '¡Bienvenido a GeoFire!',
                    text: `Has ingresado correctamente como ${data.nombre || data.username || username}.`,
                    icon: 'success',
                    confirmButtonColor: '#FF7043',
                    background: 'rgba(255, 255, 255, 0.95)',
                    backdrop: `rgba(0,0,0,0.4) blur(4px)`
                }).then(() => {
                    navigate('/dashboard');
                });
            }
        } catch (error) {
            console.error("Error en login:", error);

            const msj =
                error.response?.data?.error ||
                error.response?.data?.mensaje ||
                "Credenciales incorrectas o error de servidor.";

            Swal.fire({
                title: 'Error de Autenticación',
                text: msj,
                icon: 'error',
                confirmButtonColor: '#E53E3E',
                background: 'rgba(255, 255, 255, 0.95)'
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <img src={logoImg} alt="Logo" className="login-logo" />

                <h1 className="login-title">Ingresar a GeoFire</h1>

                <form className="login-form" onSubmit={handleSubmit}>
                    <div className="input-group">
                        <label>Nombre de usuario</label>
                        <input
                            type="text"
                            className="input-field"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            placeholder="Ej: Benjamon"
                            autoComplete="username"
                        />
                    </div>

                    <div className="input-group">
                        <label>Contraseña</label>
                        <input
                            type="password"
                            className="input-field"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            placeholder="Ingresa tu contraseña"
                            autoComplete="current-password"
                        />
                    </div>

                    <button type="submit" className="login-btn" disabled={loading}>
                        {loading ? 'INGRESANDO...' : 'INICIAR SESIÓN'}
                    </button>
                </form>

                <div className="login-footer">
                    ¿No tienes cuenta? <Link to="/registro" className="login-link">Regístrate aquí</Link>
                </div>
            </div>
        </div>
    );
}
