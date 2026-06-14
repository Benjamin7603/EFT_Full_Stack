// eslint-disable-next-line no-unused-vars
import React from 'react';
import { useNavigate } from "react-router-dom";
import {
    ClipboardList,
    LayoutDashboard,
    LogOut,
    ShieldCheck,
    UserRound,
} from "lucide-react";

import logoImg from "../assets/logo.png";
import NotificationBell from "./NotificationBell";
import "./Navbar.css";

export default function Navbar({
                                   active = "",
                                   showAdmin = true,
                                   showDashboard = true,
                                   showProfile = true,
                                   showReportes = true,
                               }) {
    const navigate = useNavigate();

    const getTokenData = () => {
        try {
            const token = localStorage.getItem("token");

            if (!token) {
                return {};
            }

            const payload = token.split(".")[1];
            const decoded = JSON.parse(atob(payload));

            return {
                usuarioId: decoded.usuarioId,
                username: decoded.sub,
                rol: decoded.rol,
            };
        } catch {
            return {};
        }
    };

    const tokenData = getTokenData();

    const nombreSesion =
        localStorage.getItem("nombre") ||
        localStorage.getItem("userNombre") ||
        localStorage.getItem("username") ||
        localStorage.getItem("userUsername") ||
        tokenData.username ||
        "Usuario";

    const rolSesion = (
        localStorage.getItem("rol") ||
        tokenData.rol ||
        "USER"
    ).toUpperCase();

    const rolesOperativos = ["ADMIN", "BOMBERO", "BRIGADISTA", "FUNCIONARIO"];
    const rolesConNotificaciones = ["ADMIN", "BOMBERO", "BRIGADISTA", "FUNCIONARIO"];

    const mostrarReportes = rolesOperativos.includes(rolSesion);
    const mostrarNotificaciones = rolesConNotificaciones.includes(rolSesion);

    const inicial = nombreSesion.trim().charAt(0).toUpperCase() || "U";

    const cerrarSesion = () => {
        localStorage.clear();
        navigate("/login");
    };

    return (
        <nav className="dash-navbar">
            <div className="nav-brand" onClick={() => navigate("/dashboard")}>
                <img src={logoImg} alt="GeoFire" />
                <h2>GeoFire</h2>
            </div>

            <div className="user-profile">
                <div className="avatar">{inicial}</div>
                <span>Hola, {nombreSesion}</span>

                {mostrarNotificaciones && (
                    <NotificationBell destinatario={rolSesion} />
                )}

                {showAdmin && rolSesion === "ADMIN" && active !== "admin" && (
                    <button
                        className="btn-profile-icon"
                        onClick={() => navigate("/admin")}
                        title="Panel administrador"
                        type="button"
                    >
                        <ShieldCheck size={20} strokeWidth={2.5} />
                    </button>
                )}

                {showReportes && mostrarReportes && active !== "reportes" && (
                    <button
                        className="btn-profile-icon"
                        onClick={() => navigate("/reportes")}
                        title="Gestión de reportes"
                        type="button"
                    >
                        <ClipboardList size={20} strokeWidth={2.5} />
                    </button>
                )}

                {showDashboard && active !== "dashboard" && (
                    <button
                        className="btn-profile-icon"
                        onClick={() => navigate("/dashboard")}
                        title="Dashboard"
                        type="button"
                    >
                        <LayoutDashboard size={20} strokeWidth={2.5} />
                    </button>
                )}

                {showProfile && active !== "perfil" && (
                    <button
                        className="btn-profile-icon"
                        onClick={() => navigate("/perfil")}
                        title="Mi perfil"
                        type="button"
                    >
                        <UserRound size={20} strokeWidth={2.5} />
                    </button>
                )}

                <button
                    onClick={cerrarSesion}
                    className="btn-logout"
                    type="button"
                    title="Cerrar sesión"
                >
                    <LogOut size={20} />
                </button>
            </div>
        </nav>
    );
}