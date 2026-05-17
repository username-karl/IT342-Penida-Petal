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

export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    login: (data) => api.post('/auth/login', data),
    getMe: () => api.get('/user/me'),
};

export const productsAPI = {
    getProducts: (mood) => api.get('/products', { params: mood ? { mood } : {} }),
    getProduct: (id) => api.get(`/products/${id}`),
};

export const cartAPI = {
    getCart: () => api.get('/cart'),
    addItem: ({ productId, quantity }) => api.post('/cart/items', { productId, quantity }),
    updateItem: (id, quantity) => api.put(`/cart/items/${id}`, { quantity }),
    removeItem: (id) => api.delete(`/cart/items/${id}`),
};

export const ordersAPI = {
    createOrder: (data) => api.post('/orders', data),
};

export default api;
