import Navbar from "../components/Navbar.jsx";
import TablaGestionReportes from "../components/TablaGestionReportes.jsx";
import "./Admin.css";
import "../App.css";

export default function Reportes() {
    return (
        <div className="admin-container">
            <Navbar active="reportes" />

            <main className="admin-content">
                <section className="admin-header-card">
                    <div>
                        <span className="admin-kicker">Operación</span>
                        <h1>Gestión de reportes GeoFire</h1>
                        <p>
                            Revisa los reportes registrados, ajusta su prioridad y actualiza
                            el estado operativo de cada emergencia.
                        </p>
                    </div>
                </section>

                <TablaGestionReportes
                    titulo="Gestión operativa de reportes"
                    kicker="Reportes"
                    limite={null}
                    mostrarBotonActualizar={true}
                />
            </main>
        </div>
    );
}