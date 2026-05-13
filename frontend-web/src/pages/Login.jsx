import { useState } from 'react';
import {
    Avatar,
    Button,
    TextField,
    Box,
    Typography,
    Container,
    Paper
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

export default function Login() {
    // Estados para guardar lo que el usuario escribe
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    // Función que se ejecuta al presionar "Ingresar"
    const handleSubmit = (event) => {
        event.preventDefault();
        // Por ahora solo imprimimos los datos en consola.
        // Más adelante aquí llamaremos a tu backend (Gateway/BFF) usando Axios.
        console.log("Intentando iniciar sesión con:", { email, password });
    };

    return (
        <Container component="main" maxWidth="xs">
            {/* Paper es la tarjeta blanca con sombra */}
            <Paper
                elevation={6}
                sx={{
                    mt: 12,
                    p: 4,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    borderRadius: 3 // Bordes redondeados modernos
                }}
            >
                {/* Ícono superior */}
                <Avatar sx={{ m: 1, bgcolor: 'secondary.main', width: 56, height: 56 }}>
                    <LockOutlinedIcon fontSize="large" />
                </Avatar>

                <Typography component="h1" variant="h5" fontWeight="bold" gutterBottom>
                    Iniciar Sesión
                </Typography>

                {/* Formulario */}
                <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Correo Electrónico"
                        name="email"
                        autoComplete="email"
                        autoFocus
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Contraseña"
                        type="password"
                        id="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        size="large"
                        sx={{ mt: 4, mb: 2, borderRadius: 2, py: 1.5, fontSize: '1.1rem' }}
                    >
                        Ingresar
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
}