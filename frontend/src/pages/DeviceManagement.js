import React, { useState, useEffect, useCallback } from 'react';
import { getAllDevices, deleteDevice } from '../apiService';
import DeviceForm from '../components/DeviceForm';

function DeviceManagement() {
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // --- STARE NOUĂ ---
    const [selectedDevice, setSelectedDevice] = useState(null);

    const fetchDevices = useCallback(async () => {
        setLoading(true);
        try {
            const deviceData = await getAllDevices();
            setDevices(deviceData);
            setError(null);
        } catch (err) {
            setError('Nu am putut prelua lista de device-uri.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDevices();
    }, [fetchDevices]);

    const clearSelection = () => {
        setSelectedDevice(null);
    };

    const handleFormSubmit = () => {
        clearSelection();
        fetchDevices();
    };

    const handleDelete = async (deviceId) => {
        if (window.confirm(`Sunteți sigur că doriți să ștergeți device-ul cu ID ${deviceId}?`)) {
            try {
                await deleteDevice(deviceId);
                alert('Device șters cu succes!');
                if (selectedDevice && selectedDevice.id === deviceId) {
                    clearSelection();
                }
                fetchDevices();
            } catch (err) {
                setError('A apărut o eroare la ștergerea device-ului.');
            }
        }
    };

    const selectedRowStyle = { backgroundColor: '#e0e0e0', cursor: 'pointer' };
    const normalRowStyle = { cursor: 'pointer' };

    if (loading && devices.length === 0) return <h1>Se încarcă device-urile...</h1>;
    if (error && devices.length === 0) return <h1 style={{ color: 'red' }}>{error}</h1>;

    return (
        <div style={{ marginTop: '40px' }}>
            <h2>Management Device-uri</h2>

            <DeviceForm
                onFormSubmit={handleFormSubmit}
                selectedDevice={selectedDevice}
                clearSelection={clearSelection}
            />

            <hr style={{ margin: '20px 0' }} />

            <h2>Listă Device-uri</h2>
            {loading && <p>Se reîncarcă lista...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}

            <table border="1" style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr>
                    <th>Device ID</th>
                    <th>Tip Device</th>
                    <th>Consum (kW)</th>
                    <th>Activ</th>
                    <th>User ID Alocat</th>
                    <th>Acțiuni</th>
                </tr>
                </thead>
                <tbody>
                {devices.map(device => (
                    <tr
                        key={device.id}
                        onClick={() => setSelectedDevice(device)}
                        style={selectedDevice && selectedDevice.id === device.id ? selectedRowStyle : normalRowStyle}
                    >
                        <td>{device.id}</td>
                        <td>{device.device_type}</td>
                        <td>{device.consumption}</td>
                        <td>{device.active ? 'Da' : 'Nu'}</td>
                        <td>{device.user ? device.user.id : 'N/A'}</td>
                        <td style={{ textAlign: 'center' }}>
                            <button
                                style={{padding: '5px 10px', background: 'red', color: 'white', border: 'none', cursor: 'pointer', borderRadius: '4px'}}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    // 2. Apelăm noul handler
                                    handleDelete(device.id);
                                }}
                            >
                                Șterge
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default DeviceManagement;