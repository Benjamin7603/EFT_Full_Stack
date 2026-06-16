
import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

const api = axios.create({
    baseURL,
});

// ── Interceptor de REQUEST ─────────────────────────────────────────────────
// Antes de cada llamada, lee el token del localStorage y lo agrega al header.
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// ── Interceptor de RESPONSE ────────────────────────────────────────────────
// Si el backend devuelve 401 (token expirado o inválido), limpia la sesión
// y redirige al login automáticamente.
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.clear();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;
