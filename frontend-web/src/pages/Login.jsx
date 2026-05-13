import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './Login.css'; // <-- ¡Importamos nuestro nuevo CSS!

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (event) => {
        event.preventDefault();

        let simulatedUsername = email.split('@')[0];
        if (!localStorage.getItem('userNombre')) {
            localStorage.setItem('userNombre', simulatedUsername.charAt(0).toUpperCase() + simulatedUsername.slice(1));
            localStorage.setItem('userUsername', simulatedUsername);
        }

        navigate('/dashboard');
    };

    return (
        <div className="login-container">
            <div className="login-card">

                <img src={logoImg} alt="Logo GeoFire" className="login-logo" />
                <h1 className="login-title">Ingresar a GeoFire</h1>

                <form className="login-form" onSubmit={handleSubmit}>

                    <div className="input-group">
                        <label htmlFor="email">Correo Electrónico</label>
                        <input
                            type="email"
                            id="email"
                            className="input-field"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            autoFocus
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Contraseña</label>
                        <input
                            type="password"
                            id="password"
                            className="input-field"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="login-btn">
                        INICIAR SESIÓN
                    </button>

                </form>

                <div className="login-footer">
                    ¿No tienes una cuenta? <Link to="/registro" className="login-link">Regístrate aquí</Link>
                </div>

            </div>
        </div>
    );
}