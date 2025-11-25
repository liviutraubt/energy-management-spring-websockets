import React from 'react';
import UserManagement from './UserManagement';
import DeviceManagement from './DeviceManagement';
import OwnedDevices from '../components/OwnedDevices';

function AdminDashboard() {
    return (
        <div>
            <h1>Admin Dashboard</h1>
            <hr />
            <UserManagement />

            <DeviceManagement />

            <div style={{ marginTop: '50px', borderTop: '2px solid black' }}>
                <h3>Zona Personală (Dispozitive proprii)</h3>
                <OwnedDevices />
            </div>
        </div>
    );
}

export default AdminDashboard;