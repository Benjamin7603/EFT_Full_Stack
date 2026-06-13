import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Registro from "./pages/Registro";
import Dashboard from "./pages/Dashboard";
import Perfil from "./pages/Perfil";
import Admin from "./pages/Admin";
import Notificaciones from "./pages/Notificaciones";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Landing />} />
                <Route path="/login" element={<Login />} />
                <Route path="/registro" element={<Registro />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/perfil" element={<Perfil />} />
                <Route path="/admin" element={<Admin />} />
                <Route path="/notificaciones" element={<Notificaciones />} />
            </Routes>
        </Router>
    );
}

export default App;