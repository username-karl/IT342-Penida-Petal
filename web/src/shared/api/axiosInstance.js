import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Attach JWT token to every request if available
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('petal_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Handle 401 responses globally
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('petal_token');
            localStorage.removeItem('petal_user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;
