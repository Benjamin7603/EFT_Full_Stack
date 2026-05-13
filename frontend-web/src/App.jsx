import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Landing from './pages/Landing'; // Importamos la nueva página
import Login from './pages/Login';
import Registro from './pages/Registro';
import Dashboard from './pages/Dashboard';

function App() {
    return (
        <Router>
            <Routes>
                {/* La ruta principal ahora es nuestra página de venta/descarga */}
                <Route path="/" element={<Landing />} />

                {/* Nuestras pantallas de la app web */}
                <Route path="/login" element={<Login />} />
                <Route path="/registro" element={<Registro />} />
                <Route path="/dashboard" element={<Dashboard />} />
            </Routes>
        </Router>
    );
}

export default App;