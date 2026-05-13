import { Link } from 'react-router-dom';
import logoImg from '../assets/logo.png';
import './Landing.css';

export default function Landing() {
    return (
        <div className="landing-container">
            <header className="landing-header">
                <div className="brand">
                    <img src={logoImg} alt="GeoFire Logo" />
                    <h1>GeoFire</h1>
                </div>
                <Link to="/login" className="btn-landing" style={{padding: '10px 25px', border: '2px solid #FF7043', color: '#FF7043', background: 'transparent'}}>
                    Iniciar Sesión
                </Link>
            </header>

            <section className="hero">
                <div className="hero-text">
                    <h2>Protege tu entorno en <span>Tiempo Real</span>.</h2>
                    <p>GeoFire es la red comunitaria y profesional líder para el reporte, monitoreo y gestión de emergencias geográficas e incendios.</p>
                    <div className="hero-btns">
                        <button className="btn-landing btn-android" onClick={() => alert("Descargando APK...")}>
                            🤖 Descargar Android
                        </button>
                        <Link to="/registro" className="btn-landing btn-web">
                            🌐 Usar Plataforma Web
                        </Link>
                    </div>
                </div>
                <div className="hero-image">
                    <img src={logoImg} alt="GeoFire App" />
                </div>
            </section>

            <section className="features">
                <h2 style={{fontSize: '2.5rem', fontWeight: 800}}>¿Por qué usar GeoFire?</h2>
                <div className="features-grid">
                    <div className="feature-card">
                        <span className="feature-icon">📍</span>
                        <h3>Mapeo Preciso</h3>
                        <p>Geolocalización exacta de reportes usando tecnología de vanguardia.</p>
                    </div>
                    <div className="feature-card">
                        <span className="feature-icon">⚡</span>
                        <h3>Alertas Inmediatas</h3>
                        <p>Notificaciones push al instante cuando ocurre una emergencia cerca de ti.</p>
                    </div>
                    <div className="feature-card">
                        <span className="feature-icon">🛡️</span>
                        <h3>Conexión Oficial</h3>
                        <p>Datos canalizados directamente con equipos de emergencia y bomberos.</p>
                    </div>
                </div>
            </section>
        </div>
    );
}