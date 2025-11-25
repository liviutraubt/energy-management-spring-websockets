import React, { useState, useEffect } from 'react';
import { createUser, updateUser } from '../apiService';

function UserForm({ onFormSubmit, selectedUser, clearSelection }) {

    const [formData, setFormData] = useState({
        username: '', password: '', role: 'USER',
        firstName: '', lastName: '', email: '',
        telephone: '', address: ''
    });

    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const isEditMode = selectedUser !== null;

    useEffect(() => {
        if (isEditMode) {
            setFormData({
                username: '',
                password: '',
                role: '',
                firstName: selectedUser.firstName,
                lastName: selectedUser.lastName,
                email: selectedUser.email,
                telephone: selectedUser.telephone,
                address: selectedUser.address
            });
            setError(null);
            setSuccess(null);
        } else {
            resetForm();
        }
    }, [selectedUser, isEditMode]);

    const resetForm = () => {
        setFormData({
            username: '', password: '', role: 'USER',
            firstName: '', lastName: '', email: '',
            telephone: '', address: ''
        });
        setError(null);
        setSuccess(null);
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        try {
            if (isEditMode) {
                await updateUser(selectedUser.id, formData);
                setSuccess('Utilizator actualizat cu succes!');
                onFormSubmit();
            } else {
                if (!formData.username || !formData.password || !formData.firstName || !formData.lastName) {
                    setError("Câmpurile Username, Password, Nume și Prenume sunt obligatorii.");
                    return;
                }
                await createUser(formData);
                setSuccess('Utilizator creat cu succes!');
                resetForm();
                onFormSubmit();
            }
        } catch (err) {
            setError(err.response?.data?.error || 'A apărut o eroare.');
        }
    };

    const formStyle = {
        display: 'flex',
        flexWrap: 'wrap',
        gap: '10px 15px',
        padding: '20px',
        border: '1px solid #ccc',
        borderRadius: '8px'
    };
    const inputGroupStyle = { display: 'flex', flexDirection: 'column', minWidth: '200px' };
    const labelStyle = { marginBottom: '5px', fontWeight: 'bold' };
    const inputStyle = { padding: '8px', border: '1px solid #ddd', borderRadius: '4px' };
    const buttonStyle = { padding: '10px 15px', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', alignSelf: 'flex-end' };

    return (
        <div>
            <h3>{isEditMode ? 'Actualizare Utilizator' : 'Creare Utilizator Nou'}</h3>
            <form onSubmit={handleSubmit} style={formStyle}>

                {!isEditMode && (
                    <>
                        <div style={inputGroupStyle}>
                            <label style={labelStyle} htmlFor="username">Username:</label>
                            <input style={inputStyle} type="text" id="username" name="username" value={formData.username} onChange={handleChange} required />
                        </div>
                        <div style={inputGroupStyle}>
                            <label style={labelStyle} htmlFor="password">Parolă:</label>
                            <input style={inputStyle} type="password" id="password" name="password" value={formData.password} onChange={handleChange} required />
                        </div>
                        <div style={inputGroupStyle}>
                            <label style={labelStyle} htmlFor="role">Rol:</label>
                            <select style={inputStyle} id="role" name="role" value={formData.role} onChange={handleChange}>
                                <option value="USER">USER (Client)</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                        </div>
                    </>
                )}

                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="firstName">Prenume:</label>
                    <input style={inputStyle} type="text" id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} required />
                </div>
                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="lastName">Nume:</label>
                    <input style={inputStyle} type="text" id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} required />
                </div>
                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="email">Email:</label>
                    <input style={inputStyle} type="email" id="email" name="email" value={formData.email} onChange={handleChange} />
                </div>
                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="telephone">Telefon:</label>
                    <input style={inputStyle} type="tel" id="telephone" name="telephone" value={formData.telephone} onChange={handleChange} />
                </div>
                <div style={inputGroupStyle}>
                    <label style={labelStyle} htmlFor="address">Adresă:</label>
                    <input style={inputStyle} type="text" id="address" name="address" value={formData.address} onChange={handleChange} />
                </div>

                <button type="submit" style={buttonStyle}>
                    {isEditMode ? 'Actualizare' : 'Creare Utilizator'}
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

export default UserForm;