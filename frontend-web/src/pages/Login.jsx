import { useState } from 'react';
import { Button, TextField, Box, Typography, Container, Paper, Link } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import logoImg from '../assets/logo.png';

// Standard login styles
const styles = {
    textField: {
        '& .MuiOutlinedInput-root': {
            borderRadius: 2,
        },
    }
};

export default function Login() {
    const [email, setEmail] = useState('bgdiaz@gmail.com'); // Default from image_2.png flow
    const [password, setPassword] = useState('12345678'); // Default from image_4.png flow
    const navigate = useNavigate();

    const handleSubmit = (event) => {
        event.preventDefault();

        // SIMULACIÓN DE LOGIN REAL:
        // Capturamos el inicio del email como "username" simulado si no hay datos de registro
        let simulatedUsername = email.split('@')[0]; // De bgdiaz@gmail.com -> bgdiaz

        // Si pasaste por el registro, estos datos ya existen y no queremos pisarlos.
        // Solo guardamos si están vacíos.
        if (!localStorage.getItem('userNombre')) {
            localStorage.setItem('userNombre', simulatedUsername.charAt(0).toUpperCase() + simulatedUsername.slice(1));
            localStorage.setItem('userUsername', simulatedUsername);
        }

        // Redirigir al Dashboard
        alert(`Iniciando sesión simulada para ${localStorage.getItem('userNombre')}`);
        navigate('/dashboard');
    };

    return (
        <Box sx={{
            minHeight: '100vh', display: 'flex', alignItems: 'center',
            background: 'linear-gradient(135deg, #f5f7fa 0%, #e4efe9 100%)' // Standard smooth background from image_7.png context
        }}>
            <Container component="main" maxWidth="xs">
                <Paper
                    elevation={12}
                    sx={{
                        p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center',
                        borderRadius: 4, background: 'rgba(255, 255, 255, 0.95)', backdropFilter: 'blur(10px)',
                        transition: 'all 0.3sease-in-out',
                        '&:hover': { boxShadow: 24 }
                    }}
                >
                    <Box component="img" src={logoImg} alt="Logo de GeoFire" sx={{ width: 80, height: 'auto', mb: 2 }} />

                    <Typography component="h1" variant="h5" fontWeight="800" color="text.primary" gutterBottom>
                        Ingresar a GeoFire
                    </Typography>

                    <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
                        <TextField
                            margin="normal" required fullWidth id="email" label="Correo Electrónico" name="email"
                            autoComplete="email" autoFocus value={email} onChange={(e) => setEmail(e.target.value)}
                            sx={styles.textField}
                        />
                        <TextField
                            margin="normal" required fullWidth name="password" label="Contraseña" type="password"
                            id="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)}
                            sx={styles.textField}
                        />

                        <Button
                            type="submit" fullWidth variant="contained" size="large"
                            sx={{
                                mt: 3, mb: 2, borderRadius: 50, py: 1.5, fontSize: '1rem', fontWeight: 'bold',
                                // Secondary brand color warm gradient amigable
                                background: 'linear-gradient(45deg, #FF7043 30%, #FFAB40 90%)', // Match from previous style context
                                boxShadow: '0 3px 5px 2px rgba(255, 112, 67, .3)',
                                transition: 'all 0.3s',
                                '&:hover': {
                                    transform: 'translateY(-2px)',
                                    background: 'linear-gradient(45deg, #FFAB40 30%, #FF7043 90%)'
                                }
                            }}
                        >
                            INICIAR SESIÓN
                        </Button>

                        <Box sx={{ textAlign: 'center', mt: 2 }}>
                            <Typography variant="body2" color="text.secondary">
                                ¿No tienes una cuenta?{' '}
                                <Link component={RouterLink} to="/registro" variant="body2" sx={{ fontWeight: 'bold', color: '#FF7043', textDecoration: 'none', '&:hover': { textDecoration: 'underline'} }}>
                                    Regístrate aquí
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </Paper>
            </Container>
        </Box>
    );
}