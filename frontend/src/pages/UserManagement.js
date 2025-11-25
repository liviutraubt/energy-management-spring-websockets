import React, { useState, useEffect, useCallback } from 'react';
import { getAllUsers, deleteUser } from '../apiService';
import UserForm from '../components/UserForm';

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [selectedUser, setSelectedUser] = useState(null);

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        try {
            const userData = await getAllUsers();
            setUsers(userData);
            setError(null);
        } catch (err) {
            setError('Nu am putut prelua lista de utilizatori.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleDelete = async (userId) => {
        if (window.confirm(`Sunteți sigur că doriți să ștergeți utilizatorul cu ID ${userId}?`)) {
            try {
                await deleteUser(userId);
                alert('Utilizator șters cu succes!');
                fetchUsers();
            } catch (err) {
                setError('A apărut o eroare la ștergerea utilizatorului.');
            }
        }
    };

    const clearSelection = () => {
        setSelectedUser(null);
    };

    const handleFormSubmit = () => {
        clearSelection();
        fetchUsers();
    };

    if (loading && users.length === 0) return <h1>Se încarcă utilizatorii...</h1>;
    if (error && users.length === 0) return <h1 style={{ color: 'red' }}>{error}</h1>;

    const selectedRowStyle = {
        backgroundColor: '#e0e0e0',
        cursor: 'pointer'
    };
    const normalRowStyle = {
        cursor: 'pointer'
    };

    return (
        <div>
            <UserForm
                onFormSubmit={handleFormSubmit}
                selectedUser={selectedUser}
                clearSelection={clearSelection}
            />

            <hr style={{ margin: '20px 0' }} />

            <h2>Listă Utilizatori</h2>

            <table border="1" style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Nume</th>
                    <th>Prenume</th>
                    <th>Email</th>
                    <th>Telefon</th>
                    <th>Adresă</th>
                    <th>Acțiuni</th>
                </tr>
                </thead>
                <tbody>
                {users.map(user => (
                    <tr
                        key={user.id}
                        onClick={() => setSelectedUser(user)}
                        style={selectedUser && selectedUser.id === user.id ? selectedRowStyle : normalRowStyle}
                    >
                        <td>{user.id}</td>
                        <td>{user.lastName}</td>
                        <td>{user.firstName}</td>
                        <td>{user.email}</td>
                        <td>{user.telephone}</td>
                        <td>{user.address}</td>
                        <td style={{textAlign: 'center'}}>
                            <button
                                style={{padding: '5px 10px', background: 'red', color: 'white', border: 'none', cursor: 'pointer', borderRadius: '4px'}}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleDelete(user.id);
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

export default UserManagement;