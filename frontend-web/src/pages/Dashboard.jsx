import { useState, useEffect } from 'react';
import { Box, Typography, Container, Grid, Paper, Button, AppBar, Toolbar, IconButton, Avatar, Card, Divider, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Grow } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import MapIcon from '@mui/icons-material/Map';
import AddAlertIcon from '@mui/icons-material/AddAlert';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import LogoutIcon from '@mui/icons-material/Logout';
import FireplaceIcon from '@mui/icons-material/Fireplace';
import LocalHospitalIcon from '@mui/icons-material/LocalHospital';
import SecurityIcon from '@mui/icons-material/Security';
import SettingsApplicationsIcon from '@mui/icons-material/SettingsApplications';
import logoImg from '../assets/logo.png';

const styles = {
    actionButton: {
        borderRadius: 50, py: 1.5, fontSize: '1rem', fontWeight: 'bold',
        transition: 'all 0.3s ease-in-out',
        '&:hover': { transform: 'translateY(-3px)' }
    },
    statCard: {
        p: 2, display: 'flex', alignItems: 'center', height: '100%', borderRadius: 3, elevation: 1,
        transition: 'all 0.2s ease-in-out',
        '&:hover': { boxShadow: 8, transform: 'scale(1.02)' }
    }
};

export default function Dashboard() {
    const navigate = useNavigate();
    const [userName, setUserName] = useState("Usuario");
    const [userInitial, setUserInitial] = useState("U");

    // AHORA USAMOS UN ESTADO VACÍO: Listo para recibir datos reales del Backend
    const [reportes, setReportes] = useState([]);

    useEffect(() => {
        const storedNombre = localStorage.getItem('userNombre');
        const storedUsername = localStorage.getItem('userUsername');

        if (storedUsername) {
            setUserName(storedUsername);
            setUserInitial(storedUsername.charAt(0).toUpperCase());
        } else if (storedNombre) {
            setUserName(storedNombre);
            setUserInitial(storedNombre.charAt(0).toUpperCase());
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('userNombre');
        localStorage.removeItem('userUsername');
        navigate('/'); // Al salir, volvemos a la Landing Page
    };

    return (
        <Box sx={{ flexGrow: 1, background: '#f0f2f5', minHeight: '100vh' }}>
            {/* BARRA SUPERIOR MÁS GRANDE (py: 2 añade altura extra) */}
            <AppBar position="static" sx={{ background: 'white', borderBottom: '1px solid #e0e0e0', boxShadow: 'none', py: 1 }}>
                <Toolbar>
                    <Box component="img" src={logoImg} alt="Logo" sx={{ width: 55, height: 'auto', mr: 2 }} />
                    <Typography variant="h5" component="div" sx={{ flexGrow: 1, color: '#FF7043', fontWeight: '900', letterSpacing: '1px' }}>
                        GeoFire
                    </Typography>
                    <Avatar sx={{ bgcolor: '#FF7043', mr: 2, width: 40, height: 40, fontSize: '1.2rem' }}>{userInitial}</Avatar>
                    <IconButton onClick={handleLogout} sx={{ color: '#FF7043', transition: 'all 0.3s', '&:hover': { transform: 'scale(1.1)', color: '#F4511E'} }} title="Cerrar Sesión">
                        <LogoutIcon fontSize="large" />
                    </IconButton>
                </Toolbar>
            </AppBar>

            <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
                <Grid container spacing={3}>

                    <Grow in={true} timeout={1000}>
                        <Grid item xs={12}>
                            <Paper sx={{ p: 4, display: 'flex', alignItems: 'center', borderRadius: 4, background: 'linear-gradient(135deg, #FF7043 30%, #FFAB40 90%)', color: 'white', boxShadow: 6, transition: 'all 0.3s', '&:hover': { boxShadow: 12 } }}>
                                <Avatar sx={{ width: 80, height: 80, mr: 4, bgcolor: 'rgba(255,255,255,0.2)', fontSize: '2.5rem', fontWeight: 'bold', border: '3px solid rgba(255,255,255,0.5)' }}>
                                    {userInitial}
                                </Avatar>
                                <Box>
                                    <Typography variant="h4" fontWeight="900" sx={{ letterSpacing: '-1px' }}>
                                        ¡Hola, {userName}!
                                    </Typography>
                                    <Typography variant="h6" sx={{ opacity: 0.9, mt: 0.5 }}>
                                        Bienvenido a tu panel GeoFire de gestión de alertas geográficas en tiempo real.
                                    </Typography>
                                </Box>
                            </Paper>
                        </Grid>
                    </Grow>

                    {/* Estadísticas */}
                    {[
                        { icon: <FireplaceIcon sx={{ color: '#FF7043' }} />, title: "Alertas Activas Hoy", value: "0" },
                        { icon: <AddAlertIcon sx={{ color: '#FF7043' }} />, title: "Tus Reportes", value: "0" },
                        { icon: <SecurityIcon sx={{ color: '#FF7043' }} />, title: "Personal Operativo", value: "0" },
                        { icon: <LocalHospitalIcon sx={{ color: '#FF7043' }} />, title: "Unidades en Alerta", value: "0" }
                    ].map((stat, index) => (
                        <Grow in={true} timeout={1200 + index * 100} key={index}>
                            <Grid item xs={12} sm={6} md={3}>
                                <Card sx={styles.statCard}>
                                    <Box sx={{ display: 'flex', flexDirection: 'column', width: '100%' }}>
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                            <Avatar sx={{ bgcolor: 'rgba(255, 112, 67, 0.15)', p: 1, mr: 2 }}>{stat.icon}</Avatar>
                                            <Typography variant="h6" fontWeight="bold" color="text.secondary">{stat.title}</Typography>
                                        </Box>
                                        <Typography variant="h3" fontWeight="900" color="text.primary" align="right">{stat.value}</Typography>
                                    </Box>
                                </Card>
                            </Grid>
                        </Grow>
                    ))}

                    {/* Acciones Rápidas */}
                    <Grow in={true} timeout={1600}>
                        <Grid item xs={12} md={4}>
                            <Paper sx={{ p: 4, display: 'flex', flexDirection: 'column', height: '100%', borderRadius: 4, elevation: 3, transition: 'all 0.3s', '&:hover': { boxShadow: 8 } }}>
                                <Typography variant="h6" fontWeight="bold" gutterBottom color="text.primary">
                                    Acciones Rápidas
                                </Typography>
                                <Button variant="contained" startIcon={<AddAlertIcon />} sx={{ mt: 2, ...styles.actionButton, background: '#FF7043', '&:hover': { background: '#F4511E', transform: 'translateY(-3px)' }}}>
                                    REPORTAR INCENDIO
                                </Button>
                                <Button variant="outlined" startIcon={<NotificationsActiveIcon />} sx={{ mt: 2, ...styles.actionButton, color: '#FF7043', borderColor: '#FF7043', '&:hover': { borderColor: '#F4511E', background: 'rgba(255, 112, 67, 0.05)', transform: 'translateY(-3px)' }}}>
                                    VER NOTIFICACIONES
                                </Button>
                                <Divider sx={{ my: 3 }} />
                                <Grid container spacing={1}>
                                    <Grid item xs={6}>
                                        <Button fullWidth size="small" variant="text" startIcon={<SettingsApplicationsIcon size="small" />} sx={{ color: 'text.secondary', textTransform: 'none' }}>Configuración</Button>
                                    </Grid>
                                    <Grid item xs={6}>
                                        <Button fullWidth size="small" variant="text" startIcon={<NotificationsActiveIcon size="small" />} sx={{ color: 'text.secondary', textTransform: 'none' }}>Ayuda</Button>
                                    </Grid>
                                </Grid>
                            </Paper>
                        </Grid>
                    </Grow>

                    {/* Mapa */}
                    <Grow in={true} timeout={1800}>
                        <Grid item xs={12} md={8}>
                            <Paper sx={{ p: 0, display: 'flex', flexDirection: 'column', height: '450px', borderRadius: 4, overflow: 'hidden', position: 'relative', boxShadow: 3, transition: 'all 0.3s', '&:hover': { boxShadow: 12, transform: 'scale(1.005)'} }}>
                                <Box sx={{ position: 'absolute', top: 16, left: 16, zIndex: 1, background: 'white', p: 1, borderRadius: 2, boxShadow: 1 }}>
                                    <Typography variant="subtitle2" fontWeight="bold" color="text.primary">
                                        <MapIcon sx={{ verticalAlign: 'middle', mr: 1, fontSize: 20, color: '#FF7043' }} />
                                        Mapa de Alertas Activas
                                    </Typography>
                                </Box>
                                <Box sx={{ flex: 1, background: 'url("https://www.transparenttextures.com/patterns/cubes.png") #e0e2e5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    <Typography variant="body1" color="text.secondary" sx={{ opacity: 0.8, fontSize: '1.2rem', fontStyle: 'italic'}}>
                                        [ El mapa interactivo de Google se cargará aquí ]
                                    </Typography>
                                </Box>
                            </Paper>
                        </Grid>
                    </Grow>

                    {/* Tabla de Reportes REAL (Esperando datos) */}
                    <Grow in={true} timeout={2000}>
                        <Grid item xs={12}>
                            <TableContainer component={Paper} sx={{ borderRadius: 4, boxShadow: 1, p: 3 }}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                                    <Typography variant="h6" fontWeight="bold" color="text.primary">
                                        Últimos Reportes Recibidos
                                    </Typography>
                                </Box>

                                {/* LÓGICA DE DATOS REALES */}
                                {reportes.length === 0 ? (
                                    <Box sx={{ textAlign: 'center', py: 5, background: '#f9f9f9', borderRadius: 2 }}>
                                        <Typography variant="h6" color="text.secondary">
                                            No hay reportes en la base de datos en este momento.
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                            Cuando crees un reporte en la aplicación, aparecerá mágicamente aquí.
                                        </Typography>
                                        <Button variant="outlined" color="primary" startIcon={<AddAlertIcon />}>
                                            Crear mi primer reporte
                                        </Button>
                                    </Box>
                                ) : (
                                    <Table size="small">
                                        <TableHead>
                                            <TableRow>
                                                <TableCell sx={{ fontWeight: 'bold' }}>ID Alerta</TableCell>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Fecha</TableCell>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Ubicación</TableCell>
                                                <TableCell sx={{ fontWeight: 'bold' }}>Nivel Riesgo</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {/* Aquí se mapeará reportes.map() cuando llegue del backend */}
                                        </TableBody>
                                    </Table>
                                )}
                            </TableContainer>
                        </Grid>
                    </Grow>

                </Grid>
            </Container>
        </Box>
    );
}