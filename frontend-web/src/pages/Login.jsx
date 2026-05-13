import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../assets/logo.png';
import './Login.css';

export default function Login() {
    const [username, setUsername] = useState(''); // <-- Ahora pedimos Username
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            // Enviamos "username" tal como lo espera tu backend en Java
            const response = await axios.post('http://localhost:8000/api/auth/login', {
                username: username,
                password: password
            });

            if (response.data.token) {
                // ¡Éxito! Guardamos el Token en el navegador
                localStorage.setItem('token', response.data.token);
                localStorage.setItem('userUsername', response.data.username || username);

                alert("¡Bienvenido a GeoFire!");
                navigate('/dashboard');
            }
        } catch (error) {
            console.error("Error en login:", error);
            // Mostrará el error real (ej: "Contraseña incorrecta")
            const msj = error.response?.data?.error || "Credenciales incorrectas o error de servidor.";
            alert("Error: " + msj);
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