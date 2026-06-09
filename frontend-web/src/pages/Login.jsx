import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../assets/logo.png';
import Swal from 'sweetalert2';
import './Login.css';
import '../App.css';
export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const response = await axios.post('http://localhost:8000/api/auth/login', {
                username: username,
                password: password
            });

            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
                localStorage.setItem('userUsername', response.data.username || username);

                Swal.fire({
                    title: '¡Bienvenido a GeoFire!',
                    text: `Has ingresado correctamente como ${username}.`,
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
            const msj = error.response?.data?.error || "Credenciales incorrectas o error de servidor.";

            Swal.fire({
                title: 'Error de Autenticación',
                text: msj,
                icon: 'error',
                confirmButtonColor: '#E53E3E',
                background: 'rgba(255, 255, 255, 0.95)'
            });
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <img src={logoImg} alt="Logo" className="login-logo" />
                <h1 className="login-title">Ingresar a GeoFire</h1>
                <form className="login-form" onSubmit={handleSubmit}>
                    <div className="input-group">
                        <label>Nombre de Usuario (Username)</label>
                        <input
                            type="text" className="input-field" value={username}
                            onChange={(e) => setUsername(e.target.value)} required
                            placeholder="Ej: Benjamon"
                        />
                    </div>
                    <div className="input-group">
                        <label>Contraseña</label>
                        <input
                            type="password" className="input-field" value={password}
                            onChange={(e) => setPassword(e.target.value)} required
                        />
                    </div>
                    <button type="submit" className="login-btn">INICIAR SESIÓN</button>
                </form>
                <div className="login-footer">
                    ¿No tienes cuenta? <Link to="/registro">Regístrate aquí</Link>
                </div>
            </div>
        </div>
    );
}