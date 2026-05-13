import { Box, Typography, Container, Grid, Button, Paper } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import AndroidIcon from '@mui/icons-material/Android';
import WebIcon from '@mui/icons-material/Web';
import SecurityIcon from '@mui/icons-material/Security';
import SpeedIcon from '@mui/icons-material/Speed';
import MapIcon from '@mui/icons-material/Map';
import logoImg from '../assets/logo.png';

export default function Landing() {
    return (
        <Box sx={{ minHeight: '100vh', background: '#fff' }}>
            {/* Navbar Simple */}
            <Box sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255,255,255,0.9)', borderBottom: '1px solid #eee' }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <img src={logoImg} alt="GeoFire Logo" style={{ height: '40px', marginRight: '10px' }} />
                    <Typography variant="h5" fontWeight="900" color="#FF7043">GeoFire</Typography>
                </Box>
                <Button component={RouterLink} to="/login" variant="outlined" sx={{ color: '#FF7043', borderColor: '#FF7043', borderRadius: 20 }}>
                    Iniciar Sesión
                </Button>
            </Box>

            {/* Hero Section (El gancho) */}
            <Box sx={{ background: 'linear-gradient(135deg, #FFF3E0 0%, #FFCCBC 100%)', py: { xs: 8, md: 12 } }}>
                <Container maxWidth="lg">
                    <Grid container spacing={6} alignItems="center">
                        <Grid item xs={12} md={6}>
                            <Typography variant="h2" fontWeight="900" color="text.primary" gutterBottom sx={{ lineHeight: 1.1 }}>
                                Protege tu entorno en <span style={{ color: '#FF7043' }}>Tiempo Real</span>.
                            </Typography>
                            <Typography variant="h6" color="text.secondary" paragraph sx={{ mb: 4 }}>
                                GeoFire es la red comunitaria y profesional para el reporte, monitoreo y gestión de emergencias geográficas e incendios.
                                Súmate desde la web o lleva la seguridad en tu bolsillo.
                            </Typography>
                            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                                <Button
                                    variant="contained" size="large" startIcon={<AndroidIcon />}
                                    sx={{ background: '#3DDC84', color: 'white', borderRadius: 30, py: 1.5, px: 3, fontWeight: 'bold', '&:hover': { background: '#32B86E' } }}
                                    onClick={() => alert("¡Próximamente! Aquí descargaremos el archivo .APK de Android Studio.")}
                                >
                                    Descargar para Android
                                </Button>
                                <Button
                                    component={RouterLink} to="/registro" variant="contained" size="large" startIcon={<WebIcon />}
                                    sx={{ background: '#FF7043', borderRadius: 30, py: 1.5, px: 3, fontWeight: 'bold', '&:hover': { background: '#F4511E' } }}
                                >
                                    Usar Plataforma Web
                                </Button>
                            </Box>
                        </Grid>

                        <Grid item xs={12} md={6} sx={{ textAlign: 'center' }}>
                            {/* Aquí luego puedes poner un mockup de celular real */}
                            <Box component="img" src={logoImg} sx={{ width: '60%', maxWidth: 400, filter: 'drop-shadow(0px 20px 30px rgba(255, 112, 67, 0.3))', transform: 'rotate(-5deg)', transition: 'transform 0.3s', '&:hover': { transform: 'scale(1.05) rotate(0deg)' } }} />
                        </Grid>
                    </Grid>
                </Container>
            </Box>

            {/* Características (Endulzando los ojos) */}
            <Container maxWidth="lg" sx={{ py: 10 }}>
                <Typography variant="h3" fontWeight="bold" textAlign="center" gutterBottom mb={6}>
                    ¿Por qué usar GeoFire?
                </Typography>
                <Grid container spacing={4}>
                    {[
                        { icon: <MapIcon sx={{ fontSize: 50, color: '#FF7043' }}/>, title: "Mapeo Preciso", desc: "Geolocalización exacta de reportes usando la API de Google Maps en tu dispositivo." },
                        { icon: <SpeedIcon sx={{ fontSize: 50, color: '#FF7043' }}/>, title: "Alertas Inmediatas", desc: "Notificaciones push al instante cuando ocurre una emergencia cerca de ti." },
                        { icon: <SecurityIcon sx={{ fontSize: 50, color: '#FF7043' }}/>, title: "Conexión Oficial", desc: "Datos canalizados directamente con equipos de emergencia, bomberos y CONAF." }
                    ].map((feature, index) => (
                        <Grid item xs={12} md={4} key={index}>
                            <Paper sx={{ p: 4, textAlign: 'center', borderRadius: 4, height: '100%', transition: 'all 0.3s', '&:hover': { transform: 'translateY(-10px)', boxShadow: 6 } }}>
                                <Box sx={{ mb: 2 }}>{feature.icon}</Box>
                                <Typography variant="h5" fontWeight="bold" gutterBottom>{feature.title}</Typography>
                                <Typography color="text.secondary">{feature.desc}</Typography>
                            </Paper>
                        </Grid>
                    ))}
                </Grid>
            </Container>
        </Box>
    );
}