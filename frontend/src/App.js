import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import AdminDashboard from './pages/AdminDashboard';
import ClientDashboard from './pages/ClientDashboard';

function App() {
    return (
        <BrowserRouter>
            <Routes>

                <Route element={<Layout />}>
                    <Route
                        path="/admin"
                        element={
                            <ProtectedRoute allowedRoles={['ADMIN']}>
                                <AdminDashboard />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/client"
                        element={
                            <ProtectedRoute allowedRoles={['USER']}>
                                <ClientDashboard />
                            </ProtectedRoute>
                        }
                    />
                </Route>

                <Route path="/login" element={<Login />} />
                <Route path="/" element={<Login />} />

                <Route path="*" element={<Navigate to="/" replace />} />

            </Routes>
        </BrowserRouter>
    );
}

export default App;