import React, { useState, useEffect } from 'react';
import { getDevicesForUser, getMonitoringData, getCurrentUser } from '../apiService';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const OwnedDevices = () => {
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [selectedDeviceId, setSelectedDeviceId] = useState(null);
    const [monitoringData, setMonitoringData] = useState([]);
    const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]); // Default azi: YYYY-MM-DD

    useEffect(() => {
        const user = getCurrentUser();
        if (user && user.id) {
            fetchDevices(user.id);
        } else {
            setError('Nu am putut identifica utilizatorul curent.');
            setLoading(false);
        }
    }, []);

    const fetchDevices = async (userId) => {
        try {
            const userDevices = await getDevicesForUser(userId);
            setDevices(userDevices);
        } catch (err) {
            setError('Nu am putut prelua lista de device-uri.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleViewHistory = async (deviceId) => {
        setSelectedDeviceId(deviceId);
        try {
            const data = await getMonitoringData(deviceId, selectedDate);

            const processedData = data.map(item => ({
                hour: new Date(item.timestamp).getHours() + ':00',
                consumption: item.consumption,
                fullTime: item.timestamp
            }));

            setMonitoringData(processedData);
        } catch (err) {
            console.error("Eroare la fetch monitoring:", err);
            alert("Nu s-au putut prelua datele de consum.");
        }
    };

    if (loading) return <p>Se încarcă dispozitivele...</p>;
    if (error) return <p style={{ color: 'red' }}>{error}</p>;

    return (
        <div style={{ padding: '20px', border: '1px solid #ccc', borderRadius: '8px', marginTop: '20px' }}>
            <h2>Dispozitivele Mele</h2>

            <div style={{ marginBottom: '20px' }}>
                <label style={{ marginRight: '10px' }}>Alege data pentru istoric:</label>
                <input
                    type="date"
                    value={selectedDate}
                    onChange={(e) => setSelectedDate(e.target.value)}
                />
            </div>

            {devices.length === 0 ? (
                <p>Nu aveți niciun device alocat.</p>
            ) : (
                <table border="1" style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '20px' }}>
                    <thead>
                    <tr style={{ backgroundColor: '#f2f2f2' }}>
                        <th>ID</th>
                        <th>Tip Device</th>
                        <th>Consum Max (kW)</th>
                        <th>Acțiuni</th>
                    </tr>
                    </thead>
                    <tbody>
                    {devices.map(device => (
                        <tr key={device.id}>
                            <td>{device.id}</td>
                            <td>{device.device_type}</td>
                            <td>{device.consumption}</td>
                            <td style={{ textAlign: 'center' }}>
                                <button
                                    onClick={() => handleViewHistory(device.id)}
                                    style={{ cursor: 'pointer', padding: '5px 10px' }}
                                >
                                    Vezi Istoric
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {selectedDeviceId && (
                <div style={{ height: '400px', marginTop: '30px' }}>
                    <h3>Istoric Consum pentru Device ID: {selectedDeviceId} (Data: {selectedDate})</h3>
                    {monitoringData.length > 0 ? (
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart
                                data={monitoringData}
                                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                            >
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="hour" label={{ value: 'Ora', position: 'insideBottomRight', offset: -5 }} />
                                <YAxis label={{ value: 'kWh', angle: -90, position: 'insideLeft' }} />
                                <Tooltip />
                                <Legend />
                                <Bar dataKey="consumption" fill="#8884d8" name="Consum (kWh)" />
                            </BarChart>
                        </ResponsiveContainer>
                    ) : (
                        <p>Nu există date de înregistrate pentru această dată.</p>
                    )}
                </div>
            )}
        </div>
    );
};

export default OwnedDevices;