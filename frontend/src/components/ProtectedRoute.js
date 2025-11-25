import React from 'react';
import { Navigate } from 'react-router-dom';
import { isLoggedIn, getCurrentUser } from '../apiService';

function ProtectedRoute({ children, allowedRoles }) {
    if (!isLoggedIn()) {
        return <Navigate to="/login" replace />;
    }

    const user = getCurrentUser();

    if (!user || !allowedRoles.includes(user.role)) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default ProtectedRoute;