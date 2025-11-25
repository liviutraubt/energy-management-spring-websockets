import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
const apiClient = axios.create({
    baseURL: 'http://localhost:81/api',
});

const login = async (username, password) => {
    try {
        const response = await apiClient.post('/auth/login', {
            username,
            password,
        });

        if (response.data.token) {
            const { token } = response.data;

            localStorage.setItem('token', token);

            const decodedUser = jwtDecode(token);

            localStorage.setItem('user', JSON.stringify({
                id: decodedUser.id,
                username: decodedUser.username,
                role: decodedUser.role
            }));

            setupAxiosInterceptor(token);

            return decodedUser;
        }
    } catch (error) {
        console.error("Eroare la login:", error);
        throw error;
    }
};

const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    delete apiClient.defaults.headers.common['app-auth'];
};

const setupAxiosInterceptor = (token) => {
    apiClient.defaults.headers.common['app-auth'] = token;
};

const token = localStorage.getItem('token');
if (token) {
    setupAxiosInterceptor(token);
}

const getCurrentUser = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        return JSON.parse(userStr);
    }
    return null;
};

const isLoggedIn = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        return false;
    }
    try {
        const decoded = jwtDecode(token);
        if (Date.now() >= decoded.exp * 1000) {
            logout();
            return false;
        }
        return true;
    } catch (err) {
        return false;
    }
};

const getDevicesForUser = async (userId) => {
    try {
        const response = await apiClient.get(`/device/${userId}`);
        return response.data;
    } catch (error) {
        console.error(`Eroare la preluarea device-urilor pentru user ${userId}:`, error);
        throw error;
    }
};

const getAllUsers = async () => {
    try {
        const response = await apiClient.get('/user');
        return response.data;
    } catch (error) {
        console.error(`Eroare la preluarea utilizatorilor:`, error);
        throw error;
    }
};

const createUser = async (userData) => {

    try {
        const response = await apiClient.post('/auth/register-admin', {
            username: userData.username,
            password: userData.password,
            role: userData.role,
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            telephone: userData.telephone,
            address: userData.address
        });

        return { success: true, user: { ...userData} };

    } catch (error) {
        console.error("Eroare la crearea utilizatorului în 3 pași:", error);

        throw error;
    }
};

const updateUser = async (userId, userData) => {
    try {
        const response = await apiClient.put(`/user/${userId}`, {
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            telephone: userData.telephone,
            address: userData.address
        });
        return response.data;
    } catch (error) {
        console.error(`Eroare la actualizarea utilizatorului ${userId}:`, error);
        throw error;
    }
};

const deleteUser = async (userId) => {
    try {
        await apiClient.delete(`/auth/${userId}`);

        return { success: true, deletedId: userId };

    } catch (error) {
        console.error(`Eroare la ștergerea utilizatorului ${userId}:`, error);
        throw error;
    }
};

const getAllDevices = async () => {
    try {
        const response = await apiClient.get('/device');
        return response.data;
    } catch (error) {
        console.error('Eroare la preluarea device-urilor:', error);
        throw error;
    }
};

const createDevice = async (deviceData) => {
    try {
        const response = await apiClient.post('/device', {
            device_type: deviceData.device_type,
            consumption: deviceData.consumption,
            active: deviceData.active,
            user: { id: deviceData.userId }
        });
        return response.data;
    } catch (error) {
        console.error('Eroare la crearea device-ului:', error);
        throw error;
    }
};

const updateDevice = async (deviceId, deviceData) => {
    try {
        const response = await apiClient.put(`/device/${deviceId}`, {
            device_type: deviceData.device_type,
            consumption: deviceData.consumption,
            active: deviceData.active,
            user: { id: deviceData.userId }
        });
        return response.data;
    } catch (error) {
        console.error(`Eroare la actualizarea device-ului ${deviceId}:`, error);
        throw error;
    }
};

const deleteDevice = async (deviceId) => {
    try {
        await apiClient.delete(`/device/${deviceId}`);
        return { success: true, deletedId: deviceId };
    } catch (error) {
        console.error(`Eroare la ștergerea device-ului ${deviceId}:`, error);
        throw error;
    }
};

const getMonitoringData = async (deviceId, dateString) => {
    try {
        const response = await apiClient.get('/monitoring', {
            params: {
                deviceId: deviceId,
                date: dateString
            }
        });
        return response.data;
    } catch (error) {
        console.error(`Eroare la preluarea datelor de monitorizare pentru device ${deviceId}:`, error);
        throw error;
    }
};

const register = async (userData) => {
    try {
        const response = await apiClient.post('/auth/register', {
            username: userData.username,
            password: userData.password,
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            telephone: userData.telephone,
            address: userData.address
        });
        return response.data;
    } catch (error) {
        console.error("Eroare la înregistrare:", error);
        throw error;
    }
};

export {
    apiClient,
    login,
    logout,
    getCurrentUser,
    isLoggedIn,
    getDevicesForUser,
    getAllUsers,
    createUser,
    updateUser,
    deleteUser,
    getAllDevices,
    createDevice,
    updateDevice,
    deleteDevice,
    getMonitoringData,
    register,
};