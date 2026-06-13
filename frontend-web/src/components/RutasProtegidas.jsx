import { Navigate } from "react-router-dom";

export default function RutasProtegidas({ children, rolesPermitidos }) {
    const token = localStorage.getItem("token");
    const rol = localStorage.getItem("rol");
    const usuarioId = localStorage.getItem("usuarioId");

    const sesionValida = Boolean(token && rol && usuarioId);

    if (!sesionValida) {
        localStorage.clear();
        return <Navigate to="/login" replace />;
    }

    if (
        rolesPermitidos &&
        !rolesPermitidos.includes(rol.toUpperCase())
    ) {
        return <Navigate to="/dashboard" replace />;
    }

    return children;
}