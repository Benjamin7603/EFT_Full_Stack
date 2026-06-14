/** @vitest-environment jsdom */
// eslint-disable-next-line no-unused-vars
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import Login from './Login';
import Swal from 'sweetalert2';

const mockAxiosInstance = {
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} }))
};

vi.mock('axios', () => {
    return {
        default: {
            create: vi.fn(() => mockAxiosInstance),
            get: vi.fn(() => Promise.resolve({ data: {} })),
            post: vi.fn(() => Promise.resolve({ data: {} })),
            put: vi.fn(() => Promise.resolve({ data: {} })),
            delete: vi.fn(() => Promise.resolve({ data: {} }))
        }
    };
});

vi.mock('sweetalert2', () => {
    return {
        default: {
            fire: vi.fn(() => Promise.resolve({ isConfirmed: true }))
        }
    };
});

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return { ...actual, useNavigate: () => mockNavigate };
});

describe('Pruebas Unitarias - Componente Login', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        console.error = vi.fn(); // Ocultamos errores intencionales en consola
    });

    it('1. Debe renderizar los inputs y el botón correctamente', () => {
        render(<BrowserRouter><Login /></BrowserRouter>);
        expect(screen.getByPlaceholderText('Ej: Benjamon')).toBeDefined();
        expect(screen.getByPlaceholderText('Ingresa tu contraseña')).toBeDefined();
        expect(screen.getByText('INICIAR SESIÓN')).toBeDefined();
    });

    it('2. Debe iniciar sesión exitosamente, guardar Token y redirigir', async () => {
        const axiosMock = await import('axios');
        axiosMock.default.post.mockImplementationOnce(() => Promise.resolve({
            data: { token: 'fake-jwt-token', username: 'benja', rol: 'ADMIN' }
        }));

        render(<BrowserRouter><Login /></BrowserRouter>);
        fireEvent.change(screen.getByPlaceholderText('Ej: Benjamon'), { target: { value: 'benja' } });
        fireEvent.change(screen.getByPlaceholderText('Ingresa tu contraseña'), { target: { value: '123456' } });
        fireEvent.click(screen.getByText('INICIAR SESIÓN'));

        await waitFor(() => {
            expect(localStorage.getItem('token')).toBe('fake-jwt-token');
            expect(localStorage.getItem('rol')).toBe('ADMIN');
            // ¡Aquí estaba el error! Se ajustó para coincidir con tu código real
            expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
        });
    });

    it('3. Debe mostrar alerta SweetAlert si las credenciales son incorrectas', async () => {
        const axiosMock = await import('axios');
        axiosMock.default.post.mockImplementationOnce(() => Promise.reject(new Error('Credenciales inválidas')));

        render(<BrowserRouter><Login /></BrowserRouter>);
        fireEvent.change(screen.getByPlaceholderText('Ej: Benjamon'), { target: { value: 'falso' } });
        fireEvent.change(screen.getByPlaceholderText('Ingresa tu contraseña'), { target: { value: 'falso' } });
        fireEvent.click(screen.getByText('INICIAR SESIÓN'));

        await waitFor(() => {
            expect(Swal.fire).toHaveBeenCalled();
            expect(localStorage.getItem('token')).toBeNull();
        });
    });
});