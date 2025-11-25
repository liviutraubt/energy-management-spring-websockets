import React from 'react';
import OwnedDevices from '../components/OwnedDevices';

function ClientDashboard() {
    return (
        <div className="dashboard-container" style={{ padding: '20px' }}>
            <h1>Client Dashboard</h1>
            <p>Bine ați venit! Mai jos puteți gestiona dispozitivele dumneavoastră.</p>

            {/* Inserăm componenta nouă */}
            <OwnedDevices />
        </div>
    );
}

export default ClientDashboard;