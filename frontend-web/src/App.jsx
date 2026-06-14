import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Registro from "./pages/Registro";
import Dashboard from "./pages/Dashboard";
import Perfil from "./pages/Perfil";
import Admin from "./pages/Admin";
import Notificaciones from "./pages/Notificaciones";
import RutasProtegidas from "./components/RutasProtegidas";
import Reportes from "./pages/Reportes.jsx";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Landing />} />
                <Route path="/login" element={<Login />} />
                <Route path="/registro" element={<Registro />} />
                <Route path="/dashboard" element={<RutasProtegidas><Dashboard /></RutasProtegidas>}/>
                <Route path="/perfil" element={<RutasProtegidas><Perfil /></RutasProtegidas>}/>
                <Route path="/notificaciones" element={<RutasProtegidas><Notificaciones /></RutasProtegidas>}/>
                <Route path="/admin" element={<RutasProtegidas rolesPermitidos={["ADMIN"]}><Admin /></RutasProtegidas>}/>
                <Route path="/reportes" element={<RutasProtegidas rolesPermitidos={["ADMIN", "BOMBERO", "BRIGADISTA", "FUNCIONARIO"]}><Reportes /></RutasProtegidas>}/>
            </Routes>
        </Router>
    );
}

export default App;