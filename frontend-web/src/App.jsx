import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';

function App() {
  return (
      <Router>
        <Routes>
          {/* Ruta para la pantalla de Login */}
          <Route path="/login" element={<Login />} />

          {/* Si el usuario entra a la raíz "/", lo redirigimos automáticamente al login */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
  );
}

export default App;