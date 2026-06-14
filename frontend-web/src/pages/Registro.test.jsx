/** @vitest-environment jsdom */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import Registro from './Registro';
import Swal from 'sweetalert2';

// Simulador Definitivo de Axios
const mockAxiosInstance = {
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
    get: vi.fn().mockResolvedValue({ data: {} }),
    post: vi.fn().mockResolvedValue({ data: {} }),
    put: vi.fn().mockResolvedValue({ data: {} }),
    delete: vi.fn().mockResolvedValue({ data: {} })
};

vi.mock('axios', () => {
    return {
        default: {
            create: vi.fn(() => mockAxiosInstance),
            get: vi.fn().mockResolvedValue({ data: {} }),
            post: vi.fn().mockResolvedValue({ data: {} }),
            put: vi.fn().mockResolvedValue({ data: {} }),
            delete: vi.fn().mockResolvedValue({ data: {} })
        }
    };
});

vi.mock('sweetalert2', () => ({ default: { fire: vi.fn().mockResolvedValue({ isConfirmed: true }) } }));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return { ...actual, useNavigate: () => mockNavigate };
});

describe('Pruebas Unitarias - Componente Registro', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('1. Debe mostrar todos los campos obligatorios del registro', () => {
        render(<BrowserRouter><Registro /></BrowserRouter>);
        expect(screen.getByPlaceholderText('Ej: Juan')).toBeDefined();
        expect(screen.getByPlaceholderText('Ej: juangarcia')).toBeDefined();
        expect(screen.getByPlaceholderText('Ej: juan@gmail.com')).toBeDefined();
        expect(screen.getByText('🚀 Registrarse')).toBeDefined();
    });

    it('2. Debe bloquear el envío si el nombre tiene menos de 3 caracteres', async () => {
        const axiosMock = await import('axios');

        render(<BrowserRouter><Registro /></BrowserRouter>);
        fireEvent.change(screen.getByPlaceholderText('Ej: Juan'), { target: { value: 'Ju' } });
        fireEvent.change(screen.getByPlaceholderText('Ej: García'), { target: { value: 'Garcia' } });
        fireEvent.change(screen.getByPlaceholderText('Ej: juangarcia'), { target: { value: 'juan' } });
        fireEvent.change(screen.getByPlaceholderText('Ej: juan@gmail.com'), { target: { value: 'juan@test.com' } });
        fireEvent.change(screen.getByPlaceholderText('Mínimo 6 caracteres'), { target: { value: '123456' } });
        fireEvent.click(screen.getByText('🚀 Registrarse'));

        await waitFor(() => {
            expect(axiosMock.default.post).not.toHaveBeenCalled();
            // Evitamos que los emojis rompan el test buscando solo una fracción del texto
            expect(screen.getByText((content) => content.includes('al menos 3 caracteres'))).toBeDefined();
        });
    });

    it('3. Debe registrar y redirigir al login si todo es correcto', async () => {
        const axiosMock = await import('axios');
        axiosMock.default.post.mockResolvedValueOnce({ status: 201, data: {} });

        render(<BrowserRouter><Registro /></BrowserRouter>);
        fireEvent.change(screen.getByPlaceholderText('Ej: Juan'), { target: { value: 'Juan Carlos' } });
        fireEvent.change(screen.getByPlaceholderText('Ej: García'), { target: { value: 'Garcia' } });
        fireEvent.change(screen.getByPlaceholderText('Ej: juangarcia'), { target: { value: 'juancito' } });
        fireEvent.change(screen.getByPlaceholderText('Ej: juan@gmail.com'), { target: { value: 'juan@test.com' } });
        fireEvent.change(screen.getByPlaceholderText('Mínimo 6 caracteres'), { target: { value: '12345678' } });
        fireEvent.click(screen.getByText('🚀 Registrarse'));

        await waitFor(() => {
            expect(axiosMock.default.post).toHaveBeenCalled();
            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });
    });
});