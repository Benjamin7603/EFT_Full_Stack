/** @vitest-environment jsdom */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import Dashboard from './Dashboard';
import axios from 'axios';

vi.mock('react-leaflet', () => ({
    MapContainer: ({ children }) => <div data-testid="map-container">{children}</div>,
    TileLayer: () => <div>TileLayer</div>,
    Marker: ({ children }) => <div>{children}</div>,
    Popup: () => <div>Popup</div>
}));

vi.mock('axios', () => ({
    default: {
        create: vi.fn(() => ({
            interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
            get: vi.fn(() => Promise.resolve({ data: {} })),
            post: vi.fn(() => Promise.resolve({ data: {} })),
            put: vi.fn(() => Promise.resolve({ data: {} })),
            delete: vi.fn(() => Promise.resolve({ data: {} }))
        })),
        get: vi.fn(() => Promise.resolve({ data: {} })),
        post: vi.fn(() => Promise.resolve({ data: {} }))
    }
}));

window.alert = vi.fn();
console.error = vi.fn();

describe('Pruebas Unitarias del Dashboard de GeoFire', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.setItem('userNombre', 'Juan Perez');

        // ¡LA SOLUCIÓN! Un token JWT simulado pero estructurado correctamente (Base64)
        const validBase64 = btoa(JSON.stringify({ usuarioId: 1, sub: 'juan', rol: 'ADMIN' }));
        localStorage.setItem('token', `header.${validBase64}.signature`);

        axios.get.mockImplementation((url) => {
            if (url.includes('open-meteo')) return Promise.resolve({ data: { current: { temperature_2m: 15.5, wind_speed_10m: 12.0 } } });
            if (url.includes('nominatim')) return Promise.resolve({ data: [{ display_name: 'Mall Paseo Costanera' }] });
            if (url.includes('/api/reportes')) return Promise.resolve({ data: [{ id: 1, descripcion: 'Incendio Test', latitud: -41.4, longitud: -72.9, estado: 'NUEVO', prioridad: 'ALTA' }] });
            return Promise.resolve({ data: {} });
        });
        axios.post.mockResolvedValue({ status: 201 });
    });

    afterEach(() => localStorage.clear());

    it('1. Debe renderizar el Navbar, el clima y el mapa', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        expect(screen.getByTestId('map-container')).toBeDefined();
        await waitFor(() => {
            expect(screen.getByText('15.5°C')).toBeDefined();
            expect(screen.getByText('12 km/h')).toBeDefined();
        });
    });

    it('2. Debe cargar y mostrar la tabla de reportes activos', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => {
            expect(screen.getByText('Incendio Test')).toBeDefined();
            expect(screen.getByText('ALTA')).toBeDefined();
            expect(screen.getByText('NUEVO')).toBeDefined();
        });
    });

    it('3. Debe abrir y cerrar el Modal de Nuevo Reporte', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());
        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));
        expect(screen.getByText('Registrar Emergencia')).toBeDefined();
        fireEvent.click(screen.getByText('Cancelar'));
        await waitFor(() => expect(screen.queryByText('Registrar Emergencia')).toBeNull());
    });

    it('4. Debe enviar un nuevo reporte y cerrar el modal', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());
        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));
        fireEvent.change(screen.getByPlaceholderText('Ej: Incendio forestal cerca de la ruta 5'), { target: { value: 'Fuego en bosque' } });
        fireEvent.change(screen.getByDisplayValue('-41.4693'), { target: { value: '-41.5' } });
        fireEvent.change(screen.getByDisplayValue('-72.9423'), { target: { value: '-73.0' } });
        fireEvent.click(screen.getByText('Enviar Reporte'));
        await waitFor(() => {
            expect(axios.post).toHaveBeenCalled();
            expect(screen.queryByText('Registrar Emergencia')).toBeNull();
        });
    });

    it('5. Debe cerrar sesión correctamente', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        const logoutBtn = await screen.findByText('Cerrar Sesión');
        fireEvent.click(logoutBtn);
        expect(localStorage.getItem('token')).toBeNull();
    });

    it('6. Debe buscar una dirección y sugerir resultados', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());
        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));
        fireEvent.change(screen.getByPlaceholderText('Buscar dirección o lugar...'), { target: { value: 'Paseo Costanera' } });
        fireEvent.click(screen.getByText('Buscar'));
        await waitFor(() => expect(screen.getByText('Mall Paseo Costanera')).toBeDefined());
        fireEvent.click(screen.getByText('Mall Paseo Costanera'));
        expect(screen.getByPlaceholderText('Buscar dirección o lugar...').value).toBe('Mall Paseo Costanera');
    });
});