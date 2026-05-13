import { useState } from 'react';
import { Button, TextField, Box, Typography, Container, Paper, Grid, Link, Collapse, Alert, IconButton } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import CloseIcon from '@mui/icons-material/Close';
// axios import removed as we are mocking. Add back when backend is ready.
import logoImg from '../assets/logo.png';

// Pre-define required style as shown in user image
const styles = {
    textField: {
        '& .MuiOutlinedInput-root': {
            borderRadius: 2,
        },
    }
};

export default function Registro() {
    const navigate = useNavigate();
    const [openAlert, setOpenAlert] = useState(false);
    const [alertMessage, setAlertMessage] = useState('');

    const [formData, setFormData] = useState({
        nombre: 'Benja', // Default value from user image
        apellido: 'Garcia', // Default value from user image
        username: 'Benjamon', // Default value from user image
        email: 'bgdiaz@gmail.com', // Default value from user image
        password: '',
        activo: true,
        rol: 'USER'
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        console.log("Enviando datos al backend:", formData);

        // Validaciones básicas requeridas por el backend (vía Hibernate Validator)
        if (formData.nombre.length < 3) {
            setAlertMessage('El nombre debe tener al menos 3 caracteres.');
            setOpenAlert(true);
            return;
        }
        // Simple email validation matching the user image
        if (!formData.email.includes('@')) {
            setAlertMessage('El correo electrónico no es válido.');
            setOpenAlert(true);
            return;
        }

        try {
            // FOR MOCKING CONNECTION to backend gateway
            // Since connection is refused in image_2.png, we simulate a successful local save for UI flow
            // Await sleep(1000) for realism if desired

            // We simulate a successful save to the local storage so dashboard can read it.
            // TRUCO DE PROGRAMADOR: Guardamos los datos localmente para simular una sesión
            localStorage.setItem('userNombre', formData.nombre);
            localStorage.setItem('userUsername', formData.username);

            alert("¡Usuario registrado con éxito en GeoFire (Simulado)! Redirigiendo al Login.");
            setOpenAlert(false);
            navigate('/login');

            // Original axios code for when MS-USUARIOS is running
            // const response = await axios.post('http://localhost:8000/api/usuarios', formData);
            // console.log("Respuesta del servidor:", response.data);
        } catch (error) {
            console.error("Error al registrar:", error);
            // Original error handling for connection issues
            // setAlertMessage('Hubo un error al registrar el usuario. Revisa la consola o asegúrate de que el backend (puerto 8000) esté corriendo.');
            // openAlert(true);
        }
    };

    return (
        <Box sx={{
            minHeight: '100vh', display: 'flex', alignItems: 'center', py: 4,
            background: 'linear-gradient(135deg, #fecfef 0%, #ff9a9e 99%, #fecfef 100%)', // matching image_2.png background color better
        }}>
            <Container component="main" maxWidth="sm">
                <Paper
                    elevation={12}
                    sx={{
                        p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center',
                        borderRadius: 4, background: 'rgba(255, 255, 255, 0.95)', backdropFilter: 'blur(10px)',
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': { boxShadow: 24 } // Subtle animation on hover
                    }}
                >
                    <Box component="img" src={logoImg} alt="Logo" sx={{ width: 70, height: 'auto', mb: 2 }} />

                    <Typography component="h1" variant="h5" fontWeight="800" color="text.primary" gutterBottom>
                        Crear cuenta en GeoFire
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                        Únete a nuestra red de reportes geográficos
                    </Typography>

                    {/* Validation Alert matching typical MUI usage shown in image */}
                    <Collapse in={openAlert}>
                        <Alert
                            severity="error"
                            action={
                                <IconButton aria-label="close" color="inherit" size="small" onClick={() => setOpenAlert(false)}>
                                    <CloseIcon fontSize="inherit" />
                                </IconButton>
                            }
                            sx={{ mb: 2, width: '100%', borderRadius: 2 }}
                        >
                            {alertMessage}
                        </Alert>
                    </Collapse>

                    <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
                        <Grid container spacing={2}>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    required fullWidth name="nombre" label="Nombre"
                                    value={formData.nombre} onChange={handleChange}
                                    sx={styles.textField}
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    required fullWidth name="apellido" label="Apellido"
                                    value={formData.apellido} onChange={handleChange}
                                    sx={styles.textField}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    required fullWidth name="username" label="Nombre de Usuario (Username)"
                                    value={formData.username} onChange={handleChange}
                                    sx={styles.textField}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    required fullWidth type="email" name="email" label="Correo Electrónico"
                                    value={formData.email} onChange={handleChange}
                                    sx={styles.textField}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    required fullWidth name="password" label="Contraseña" type="password"
                                    value={formData.password} onChange={handleChange}
                                    sx={styles.textField}
                                />
                            </Grid>
                        </Grid>

                        <Button
                            type="submit" fullWidth variant="contained" size="large"
                            sx={{
                                mt: 4, mb: 2, borderRadius: 50, py: 1.5, fontSize: '1rem', fontWeight: 'bold',
                                // Primary brand color gradient warm but intense warm color
                                background: 'linear-gradient(45deg, #FE6B8B 30%, #FF8E53 90%)', // Matching register button warm gradient in image_2.png
                                boxShadow: '0 3px 5px 2px rgba(255, 105, 135, .3)',
                                transition: 'all 0.3s',
                                '&:hover': {
                                    transform: 'translateY(-2px)',
                                    background: 'linear-gradient(45deg, #FF8E53 30%, #FE6B8B 90%)'
                                }
                            }}
                        >
                            REGISTRARSE
                        </Button>

                        <Box sx={{ textAlign: 'center', mt: 1 }}>
                            <Typography variant="body2" color="text.secondary">
                                ¿Ya tienes una cuenta?{' '}
                                <Link component={RouterLink} to="/login" variant="body2" sx={{ fontWeight: 'bold', color: '#FE6B8B', textDecoration: 'none', '&:hover': { textDecoration: 'underline'} }}>
                                    Inicia sesión aquí
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
}