import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';

// Importaciones de Material UI
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';

// Aquí podemos definir los colores principales de tu marca en el futuro
const theme = createTheme({
    palette: {
        primary: {
            main: '#1976d2', // Un azul estilo corporativo
        },
        secondary: {
            main: '#dc004e', // Un rojo para alertas/incendios
        },
    },
});

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <ThemeProvider theme={theme}>
            {/* CssBaseline limpia los márgenes por defecto del navegador */}
            <CssBaseline />
            <App />
        </ThemeProvider>
    </React.StrictMode>,
);