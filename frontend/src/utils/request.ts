import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

const request = axios.create({
    baseURL: '/api',
    timeout: 5000,
});

// Add token to request headers if it exists
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle responses and errors
request.interceptors.response.use(
    response => {
        const res = response.data;
        // If the response code is not 200, show an error message
        if (res.code !== 200) {
            ElMessage.error(res.message || 'Request failed');
            return Promise.reject(new Error(res.message));
        }
        return res;
    },
    error => {
        // If the error response code is 401, redirect to login page
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            router.push('/login');
            ElMessage.error('Session expired, please log in again');
        } else {
            ElMessage.error(error.message || 'Network error');
        }
        return Promise.reject(error);
    }
);

export default request;