import React from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { logout } from '../apiService';

function Layout() {
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div>
            <nav style={{ background: '#eee', padding: '10px', display: 'flex', justifyContent: 'space-between' }}>
                <h3>Energy Management</h3>
                <button onClick={handleLogout}>Logout</button>
            </nav>
            <main style={{ padding: '20px' }}>
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;