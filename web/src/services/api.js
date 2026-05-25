import axios from 'axios';

const defaultApiBaseUrl = `${window.location.protocol}//${window.location.hostname}:8080/api`;
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || defaultApiBaseUrl).replace(/\/+$/, '');
const API_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, '');

export function mediaUrl(value) {
    const url = String(value || '').trim();
    if (!url) return '';
    if (/^(https?:)?\/\//i.test(url) || /^(data|blob):/i.test(url)) return url;
    if (url.startsWith('/uploads/')) return `${API_ORIGIN}${url}`;
    if (url.startsWith('uploads/')) return `${API_ORIGIN}/${url}`;
    return url;
}

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
    getSellerProducts: () => api.get('/seller/products'),
    createProduct: (data) => api.post('/seller/products', data),
    updateProduct: (id, data) => api.put(`/seller/products/${id}`, data),
    deleteProduct: (id) => api.delete(`/seller/products/${id}`),
};

export const floristAPI = {
    getProfile: () => api.get('/seller/florist'),
    updateProfile: (data) => api.put('/seller/florist', data),
    uploadProfileImage: (file) => {
        const formData = new FormData();
        formData.append('file', file);
        return api.post('/florists/profile/image', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    },
};

export const cartAPI = {
    getCart: () => api.get('/cart'),
    addItem: ({ productId, quantity }) => api.post('/cart/items', { productId, quantity }),
    updateItem: (id, quantity) => api.put(`/cart/items/${id}`, { quantity }),
    removeItem: (id) => api.delete(`/cart/items/${id}`),
};

export const addressesAPI = {
    getAddresses: () => api.get('/addresses'),
    createAddress: (data) => api.post('/addresses', data),
    updateAddress: (id, data) => api.put(`/addresses/${id}`, data),
    deleteAddress: (id) => api.delete(`/addresses/${id}`),
};

export const savedDatesAPI = {
    getSavedDates: () => api.get('/users/dates'),
    createSavedDate: (data) => api.post('/users/dates', data),
};

export const slotsAPI = {
    getAvailability: ({ floristId, date }) => api.get('/slots/availability', {
        params: { florist_id: floristId, date },
    }),
};

export const ordersAPI = {
    createOrder: (data) => api.post('/orders', data),
    getBuyerOrders: () => api.get('/orders'),
    getBuyerOrder: (id) => api.get(`/orders/${id}`),
    getSellerOrders: () => api.get('/seller/orders'),
    getSellerOrder: (id) => api.get(`/seller/orders/${id}`),
    updateSellerOrderStatus: (id, status) => api.put(`/seller/orders/${id}/status`, { status }),
    updateSellerShipping: (id, data) => api.put(`/seller/orders/${id}/shipping`, data),
    uploadFulfillmentPhoto: (id, file) => {
        const formData = new FormData();
        formData.append('file', file);
        return api.post(`/orders/${id}/fulfillment-photo`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    },
    uploadProofPhoto: (id, file) => {
        const formData = new FormData();
        formData.append('file', file);
        return api.post(`/orders/${id}/proof`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    },
};

export default api;
