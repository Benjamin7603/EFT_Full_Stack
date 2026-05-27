/** @vitest-environment jsdom */
// eslint-disable-next-line no-unused-vars
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import Dashboard from './Dashboard';
import axios from 'axios';

// MOCK DE DEPENDENCIAS EXTERNAS
vi.mock('react-leaflet', () => {
    return {
        MapContainer: ({ children }) => <div data-testid="map-container">{children}</div>,
        TileLayer: () => <div>TileLayer</div>,
        Marker: ({ children }) => <div>{children}</div>,
        Popup: () => <div>Popup</div>
    };
});

vi.mock('axios');
window.alert = vi.fn();
console.error = vi.fn(); // Ocultamos los errores en consola durante el test

describe('Pruebas Unitarias del Dashboard de GeoFire', () => {

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.setItem('userNombre', 'Juan Perez');
        localStorage.setItem('token', 'fake-jwt-token');

        // Configuramos Axios para simular RESPUESTAS CON DATOS
        axios.get.mockImplementation((url) => {
            if (url.includes('open-meteo')) {
                return Promise.resolve({
                    data: { current: { temperature_2m: 35, wind_speed_10m: 30, relative_humidity_2m: 20, uv_index: 9, precipitation: 0, weather_code: 1 } }
                });
            }
            if (url.includes('nominatim')) {
                return Promise.resolve({
                    data: [{ place_id: 1, lat: "-41.4", lon: "-72.9", display_name: "Mall Paseo Costanera" }]
                });
            }
            // Retornamos 2 reportes para probar la tabla y los filtros
            return Promise.resolve({
                data: [
                    { id: 1, descripcion: 'Fuego en el bosque', latitud: -41.4, longitud: -72.9, prioridad: 'ALTA', estado: 'NUEVO', fechaReporte: '2023-10-10T10:00:00' },
                    { id: 2, descripcion: 'Olor a humo', latitud: -41.5, longitud: -72.8, prioridad: 'BAJA', estado: 'RESUELTO', fechaReporte: '2023-10-11T10:00:00' }
                ]
            });
        });

        axios.post.mockResolvedValue({ data: { success: true } });
    });

    afterEach(() => {
        localStorage.clear();
    });

    it('1. Debe renderizar el panel principal y la tabla con datos', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => {
            expect(screen.getByText('Fuego en el bosque')).toBeDefined();
            expect(screen.getByText('Olor a humo')).toBeDefined();
        });
    });

    it('2. Debe abrir el modal al hacer clic en "NUEVO REPORTE"', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());

        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));
        expect(screen.getByText('Crear Alerta Geográfica')).toBeDefined();
    });

    it('3. Debe enviar un nuevo reporte exitosamente', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());

        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));
        fireEvent.change(screen.getByPlaceholderText('Ej: Incendio forestal cerca de la ruta 5'), { target: { value: 'Test' } });
        fireEvent.click(screen.getByText('Enviar Reporte'));

        await waitFor(() => expect(axios.post).toHaveBeenCalled());
    });

    it('4. Debe cerrar sesión y limpiar el localStorage', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());

        fireEvent.click(document.querySelector('.btn-logout'));
        expect(localStorage.getItem('token')).toBeNull();
    });

    it('5. Debe filtrar la tabla de reportes por Estado y Prioridad', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('Fuego en el bosque')).toBeDefined());

        const selectEstado = document.querySelectorAll('.filter-select')[0];
        fireEvent.change(selectEstado, { target: { value: 'NUEVO' } });

        // Verificamos que se filtró correctamente
        expect(screen.getByText('Fuego en el bosque')).toBeDefined();
        expect(screen.queryByText('Olor a humo')).toBeNull();
    });

    it('6. Debe buscar una dirección en el mapa y seleccionarla', async () => {
        render(<BrowserRouter><Dashboard /></BrowserRouter>);
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());

        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));

        // Escribimos en el buscador
        const inputBuscador = screen.getByPlaceholderText('Ej: Mall Paseo Costanera, Puerto Montt');
        fireEvent.change(inputBuscador, { target: { value: 'Mall' } });
        fireEvent.click(screen.getByText('🔍 Buscar'));

        // Esperamos que aparezca el resultado y le damos clic
        await waitFor(() => expect(screen.getByText('Mall Paseo Costanera')).toBeDefined());
        fireEvent.click(screen.getByText('Mall Paseo Costanera'));

        // El input debe haberse rellenado con el nombre
        expect(inputBuscador.value).toBe('Mall Paseo Costanera');
    });

    it('7. Debe manejar errores del servidor al cargar y enviar (Cobertura Catch)', async () => {
        // Simulamos que el servidor está caído y da error 400
        axios.get.mockRejectedValue(new Error('Network Error'));
        axios.post.mockRejectedValue({ response: { status: 400, data: { msg: 'Faltan datos' } } });

        render(<BrowserRouter><Dashboard /></BrowserRouter>);

        // Esperamos a que el botón exista y abrimos el modal
        await waitFor(() => expect(screen.getByText('+ NUEVO REPORTE')).toBeDefined());
        fireEvent.click(screen.getByText('+ NUEVO REPORTE'));

        // RELLENAMOS EL INPUT (¡Vital para pasar la validación 'required' del formulario!)
        const inputDesc = screen.getByPlaceholderText('Ej: Incendio forestal cerca de la ruta 5');
        fireEvent.change(inputDesc, { target: { value: 'Falla intencional' } });

        // Ahora sí, enviamos el reporte
        fireEvent.click(screen.getByText('Enviar Reporte'));

        // Verificamos que la alerta de error rojo se disparó
        await waitFor(() => {
            expect(console.error).toHaveBeenCalled();
            expect(window.alert).toHaveBeenCalledWith(expect.stringContaining('Error en el formulario'));
        });
    });
});