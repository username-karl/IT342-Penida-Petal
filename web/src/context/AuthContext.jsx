import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = async () => {
        const token = localStorage.getItem('petal_token');
        if (!token) {
            setLoading(false);
            return;
        }

        try {
            const response = await authAPI.getMe();
            setUser(response.data.data);
        } catch (error) {
            localStorage.removeItem('petal_token');
            localStorage.removeItem('petal_user');
        } finally {
            setLoading(false);
        }
    };

    const login = async (email, password) => {
        const response = await authAPI.login({ email, password });
        const data = response.data.data;

        localStorage.setItem('petal_token', data.token);
        localStorage.setItem('petal_user', JSON.stringify(data));

        setUser({
            name: data.name,
            email: data.email,
            role: data.role,
        });

        return data;
    };

    const register = async (name, email, password, role) => {
        const response = await authAPI.register({ name, email, password, role });
        return response.data;
    };

    const logout = () => {
        localStorage.removeItem('petal_token');
        localStorage.removeItem('petal_user');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
