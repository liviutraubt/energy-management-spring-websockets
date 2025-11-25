import React, { useState } from 'react';
import { login, register } from '../apiService'; // Import register
import { useNavigate } from 'react-router-dom';

function Login() {
    const [isLoginMode, setIsLoginMode] = useState(true);

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');


    const [registerData, setRegisterData] = useState({
        username: '',
        password: '',
        firstName: '',
        lastName: '',
        email: '',
        telephone: '',
        address: ''
    });

    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);
    const navigate = useNavigate();

    const handleRegisterChange = (e) => {
        const { name, value } = e.target;
        setRegisterData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccessMsg(null);

        if (isLoginMode) {

            try {
                const userData = await login(username, password);
                console.log('Login reușit:', userData);

                if (userData.role === 'ADMIN') {
                    navigate('/admin');
                } else if (userData.role === 'USER') {
                    navigate('/client');
                } else {
                    navigate('/');
                }
            } catch (err) {
                setError('Nume de utilizator sau parolă incorectă.');
                console.error(err);
            }
        } else {
            try {
                await register(registerData);
                setSuccessMsg('Cont creat cu succes! Te rugăm să te autentifici.');
                setIsLoginMode(true);
                setUsername(registerData.username);
                setPassword('');
            } catch (err) {
                const msg = err.response?.data?.error || 'Eroare la înregistrare.';
                setError(msg);
            }
        }
    };

    const toggleMode = () => {
        setIsLoginMode(!isLoginMode);
        setError(null);
        setSuccessMsg(null);
    };

    return (
        <div style={{ maxWidth: '400px', margin: 'auto', padding: '20px' }}>
            <h2>{isLoginMode ? 'Login' : 'Înregistrare'}</h2>

            {successMsg && <p style={{ color: 'green' }}>{successMsg}</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}

            <form onSubmit={handleSubmit}>
                {isLoginMode ? (
                    <>
                        <div>
                            <label>Username:</label>
                            <br/>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                style={{ width: '100%', marginBottom: '10px' }}
                            />
                        </div>
                        <div>
                            <label>Password:</label>
                            <br/>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                style={{ width: '100%', marginBottom: '10px' }}
                            />
                        </div>
                    </>
                ) : (
                    <>
                        <div>
                            <label>Username*:</label>
                            <input
                                type="text"
                                name="username"
                                value={registerData.username}
                                onChange={handleRegisterChange}
                                required
                                style={{ width: '100%', marginBottom: '5px' }}
                            />
                        </div>
                        <div>
                            <label>Password*:</label>
                            <input
                                type="password"
                                name="password"
                                value={registerData.password}
                                onChange={handleRegisterChange}
                                required
                                style={{ width: '100%', marginBottom: '5px' }}
                            />
                        </div>
                        <div>
                            <label>Nume:</label>
                            <input
                                type="text"
                                name="firstName"
                                value={registerData.firstName}
                                onChange={handleRegisterChange}
                                style={{ width: '100%', marginBottom: '5px' }}
                            />
                        </div>
                        <div>
                            <label>Prenume:</label>
                            <input
                                type="text"
                                name="lastName"
                                value={registerData.lastName}
                                onChange={handleRegisterChange}
                                style={{ width: '100%', marginBottom: '5px' }}
                            />
                        </div>
                        <div>
                            <label>Email:</label>
                            <input
                                type="email"
                                name="email"
                                value={registerData.email}
                                onChange={handleRegisterChange}
                                style={{ width: '100%', marginBottom: '5px' }}
                            />
                        </div>
                        <div>
                            <label>Telefon:</label>
                            <input
                                type="text"
                                name="telephone"
                                value={registerData.telephone}
                                onChange={handleRegisterChange}
                                style={{ width: '100%', marginBottom: '5px' }}
                            />
                        </div>
                        <div>
                            <label>Adresă:</label>
                            <input
                                type="text"
                                name="address"
                                value={registerData.address}
                                onChange={handleRegisterChange}
                                style={{ width: '100%', marginBottom: '10px' }}
                            />
                        </div>
                    </>
                )}

                <button type="submit" style={{ width: '100%', padding: '10px', marginTop: '10px' }}>
                    {isLoginMode ? 'Login' : 'Înregistrează-te'}
                </button>
            </form>

            <div style={{ marginTop: '15px', textAlign: 'center' }}>
                <button
                    onClick={toggleMode}
                    style={{ background: 'none', border: 'none', color: 'blue', textDecoration: 'underline', cursor: 'pointer' }}
                >
                    {isLoginMode ? "Nu ai cont? Înregistrează-te aici." : "Ai deja cont? Loghează-te."}
                </button>
            </div>
        </div>
    );
}

export default Login;