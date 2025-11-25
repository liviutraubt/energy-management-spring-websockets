import React, { useState, useEffect } from 'react';
import { createDevice, updateDevice } from '../apiService';

function DeviceForm({ onFormSubmit, selectedDevice, clearSelection }) {

    const [formData, setFormData] = useState({
        device_type: '',
        consumption: 0.0,
        active: false,
        userId: ''
    });

    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const isEditMode = selectedDevice !== null;

    useEffect(() => {
        if (isEditMode) {
            setFormData({
                device_type: selectedDevice.device_type,
                consumption: selectedDevice.consumption,
                active: selectedDevice.active,
                userId: selectedDevice.user ? selectedDevice.user.id : ''
            });
            setError(null);
            setSuccess(null);
        } else {
            resetForm();
        }
    }, [selectedDevice, isEditMode]);

    const resetForm = () => {
        setFormData({
            device_type: '',
            consumption: 0.0,
            active: false,
            userId: ''
        });
        setError(null);
        setSuccess(null);
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === 'checkbox' ? checked : value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        if (!formData.device_type || !formData.userId) {
            setError("Tipul device-ului și ID-ul utilizatorului sunt obligatorii.");
            return;
        }

        const payload = {
            ...formData,
            consumption: parseFloat(formData.consumption),
            userId: parseInt(formData.userId, 10)
        };

        if (isNaN(payload.consumption) || isNaN(payload.userId)) {
            setError("Consumul și User ID trebuie să fie numere valide.");
            return;
        }

        try {
            if (isEditMode) {
                await updateDevice(selectedDevice.id, payload);
                setSuccess('Device actualizat cu succes!');
            } else {
                await createDevice(payload);
                setSuccess('Device creat cu succes!');
                resetForm();
            }
            onFormSubmit();

        } catch (err) {
            setError(err.response?.data?.error || 'A apărut o eroare.');
        }
    };

    const formStyle = { display: 'flex', flexWrap: 'wrap', gap: '10px 15px', padding: '20px', border: '1px solid #ccc', borderRadius: '8px' };
    const inputGroupStyle = { display: 'flex', flexDirection: 'column', minWidth: '200px' };
    const labelStyle = { marginBottom: '5px', fontWeight: 'bold' };
    const inputStyle = { padding: '8px', border: '1px solid #ddd', borderRadius: '4px' };
    const buttonStyle = { padding: '10px 15px', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', alignSelf: 'flex-end' };
    const checkboxGroupStyle = { display: 'flex', alignItems: 'center', minWidth: '200px', alignSelf: 'center' };

    return (
        <div>
            <h3>{isEditMode ? 'Actualizare Device' : 'Creare Device Nou'}</h3>
            <form onSubmit={handleSubmit} style={formStyle}>

                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="device_type">Tip Device:</label>
                    <input style={inputStyle} type="text" id="device_type" name="device_type" value={formData.device_type} onChange={handleChange} required />
                </div>
                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="consumption">Consum (kW):</label>
                    <input style={inputStyle} type="number" step="0.01" id="consumption" name="consumption" value={formData.consumption} onChange={handleChange} required />
                </div>
                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="userId">User ID Alocat:</label>
                    <input style={inputStyle} type="number" id="userId" name="userId" value={formData.userId} onChange={handleChange} required />
                </div>
                <div style={checkboxGroupStyle}>
                    <input style={{marginRight: '10px'}} type="checkbox" id="active" name="active" checked={formData.active} onChange={handleChange} />
                    <label style={{...labelStyle, marginBottom: 0}} htmlFor="active">Activ</label>
                </div>

                <button type="submit" style={buttonStyle}>
                    {isEditMode ? 'Actualizare' : 'Creare Device'}
                </button>

                {isEditMode && (
                    <button type="button" onClick={clearSelection} style={{...buttonStyle, background: '#6c757d'}}>
                        Anulare
                    </button>
                )}
            </form>
            {success && <p style={{ color: 'green', marginTop: '10px' }}>{success}</p>}
            {error && <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>}
        </div>
    );
}

export default DeviceForm;