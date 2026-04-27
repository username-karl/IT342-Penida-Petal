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

// ─── Auth API ───
export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    login: (data) => api.post('/auth/login', data),
    getMe: () => api.get('/user/me'),
};

// ─── Product API ───
export const productAPI = {
    // Get all products (optional mood filter)
    getAll: (mood) => api.get('/products', { params: mood ? { mood } : {} }),
    // Get a single product by ID
    getById: (id) => api.get(`/products/${id}`),
    // Get products owned by the current florist
    getMine: () => api.get('/products/mine'),
    // Create a new product (Florist only)
    create: (data) => api.post('/products', data),
    // Update an existing product (Florist only)
    update: (id, data) => api.put(`/products/${id}`, data),
    // Toggle stock status (Florist only)
    toggleStock: (id, inStock) => api.patch(`/products/${id}/stock`, { inStock }),
    // Delete a product (Florist only)
    delete: (id) => api.delete(`/products/${id}`),
};

// ─── Florist API ───
export const floristAPI = {
    // Get the current florist's profile
    getProfile: () => api.get('/florists/profile'),
    // Update the current florist's profile
    updateProfile: (data) => api.put('/florists/profile', data),
};

export default api;
