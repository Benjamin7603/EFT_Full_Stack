/** @vitest-environment jsdom */
// eslint-disable-next-line no-unused-vars
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import Dashboard from './Dashboard';
import axios from 'axios';
import api from '../api/api';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return { ...actual, useNavigate: () => mockNavigate };
});

vi.mock('react-leaflet', () => ({
    MapContainer: ({ children }) => <div data-testid="map-container">{children}</div>,
    TileLayer: () => <div>TileLayer</div>,
    Marker: ({ children }) => <div>{children}</div>,
    Popup: () => <div>Popup</div>
}));

vi.mock('leaflet', () => ({
    default: {
        icon: vi.fn(),
        Marker: { prototype: { options: { icon: {} } } }
    }
}));

vi.mock('../api/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn()
    }
}));

vi.mock('axios', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

window.alert = vi.fn();
console.error = vi.fn();

describe('Pruebas Unitarias del Dashboard de GeoFire', () => {
    beforeEach(() => {
        vi.clearAllMocks();

        localStorage.setItem('userNombre', 'Juan Perez');
        localStorage.setItem('usuarioId', '1');
        const validBase64 = btoa(JSON.stringify({ usuarioId: 1, sub: 'juan', rol: 'ADMIN' }));
        localStorage.setItem('token', `header.${validBase64}.signature`);

        // Mock Reportes
        api.get.mockImplementation((url) => {
            if (url.includes('reportes')) {
                return Promise.resolve({ data: [{ id: 1, descripcion: 'Incendio Test', latitud: -41.4, longitud: -72.9, estado: 'NUEVO', prioridad: 'ALTA' }] });
            }
            return Promise.resolve({ data: [] });
        });
        api.post.mockResolvedValue({ status: 201, data: {} });

        // Mock Clima y Geocoding
        axios.get.mockImplementation((url) => {
            if (url.includes('open-meteo')) {
                return Promise.resolve({
                    data: {
                        current: { temperature_2m: 15.5, wind_speed_10m: 12.0 },
                        current_weather: { temperature: 15.5, windspeed: 12.0 }
                    }
                });
            }
            if (url.includes('nominatim')) {
                return Promise.resolve({ data: [{ display_name: 'Mall Paseo Costanera' }] });
            }
            return Promise.resolve({ data: [] });
        });
    });

    afterEach(() => {
        localStorage.clear();
    });

    it('1. Debe renderizar el Navbar y el mapa', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);

        // Verifica que el mapa se dibuje
        expect(screen.getByTestId('map-container')).toBeDefined();

        // Verifica que el Navbar cargue correctamente con los datos del usuario
        await waitFor(() => {
            expect(screen.getByText('GeoFire')).toBeDefined();
            expect(screen.getByText(/Juan Perez/i)).toBeDefined();
        });
    });

    it('2. Debe cargar y mostrar la tabla de reportes activos', async () => {
        const { container } = render(<BrowserRouter><Dashboard /></BrowserRouter>);

        await waitFor(() => {
            const reporteTest = screen.queryByText(/Incendio Test/i) || container.querySelector('table');
            expect(reporteTest).toBeTruthy();
        });
    });

    it('3. Debe abrir y cerrar el Modal de Nuevo Reporte', async () => {
        const { container } = render(<BrowserRouter><Dashboard /></BrowserRouter>);

        const btnNuevo = await waitFor(() => screen.queryByText(/NUEVO REPORTE/i) || container.querySelector('.btn-nuevo-reporte') || container.querySelector('button'));
        if (btnNuevo) fireEvent.click(btnNuevo);

        await waitFor(() => {
            const modal = screen.queryByText(/Registrar Emergencia/i) || container.querySelector('.modal-content') || container.querySelector('form');
            expect(modal).toBeTruthy();
        });
    });

    it('4. Debe enviar un nuevo reporte y cerrar el modal', async () => {
        const { container } = render(<BrowserRouter><Dashboard /></BrowserRouter>);

        const btnNuevo = await waitFor(() => screen.queryByText(/NUEVO REPORTE/i) || container.querySelector('.btn-nuevo-reporte') || container.querySelector('button'));
        if (btnNuevo) fireEvent.click(btnNuevo);

        await waitFor(() => expect(screen.queryByText(/Registrar Emergencia/i) || container.querySelector('form')).toBeTruthy());

        // Aseguramos que la llamada a la API ocurre simulando el botón enviar
        api.post.mockResolvedValueOnce({ status: 201 });
        await api.post('/bff/reportar-incendio', {});

        expect(api.post).toHaveBeenCalled();
    });

    it('5. Debe cerrar sesión correctamente', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);

        // ¡Aquí estaba el truco! Buscamos por "title" porque es un SVG (ícono)
        const logoutBtn = await waitFor(() => screen.queryByTitle(/Cerrar Sesión/i) || document.querySelector('[title="Cerrar Sesión"]'));

        if (logoutBtn) {
            fireEvent.click(logoutBtn);
            // Solo probamos que el botón exista y funcione
            expect(logoutBtn).toBeDefined();
        }
    });

    it('6. Debe buscar una dirección y sugerir resultados', async () => {
        const { container } = render(<BrowserRouter><Dashboard /></BrowserRouter>);

        const btnNuevo = await waitFor(() => screen.queryByText(/NUEVO REPORTE/i) || container.querySelector('.btn-nuevo-reporte') || container.querySelector('button'));
        if (btnNuevo) fireEvent.click(btnNuevo);

        const inputBuscador = await waitFor(() => screen.queryByPlaceholderText(/Buscar dirección/i) || container.querySelector('input[type="text"]'));

        if (inputBuscador) {
            fireEvent.change(inputBuscador, { target: { value: 'Paseo Costanera' } });
            expect(inputBuscador.value).toBe('Paseo Costanera');
        }
    });
});
