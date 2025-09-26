import axios from 'axios';

// Base URL - All requests go through API Gateway for centralized routing, authentication, and load balancing
const API_BASE_URL = 'http://localhost:8080'; // API Gateway

// Create axios instance with default configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000, // Increased timeout for better reliability
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add auth token if available
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle auth errors and provide consistent error format
apiClient.interceptors.response.use(
  (response) => {
    console.log(`✅ API Success Response for ${response.config.url}:`, response.data);
    return {
      success: true,
      data: response.data
    };
  },
  (error) => {
    console.log(`❌ API Error Response for ${error.config?.url}:`, {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });
    
    // Handle auth errors - but avoid infinite redirect for login endpoint
    if (error.response?.status === 401 && !error.config.url?.includes('/auth/login')) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('currentUser');
      // Only redirect if not already on login page
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/signin')) {
        window.location.href = '/signin';
      }
    }

    let errorMessage = 'Something went wrong';
    let errorDetails = null;
    
    if (error.response) {
      // Server responded with error status
      errorMessage = error.response.data?.message || 
                    error.response.data?.error || 
                    `Server Error ${error.response.status}`;
      errorDetails = error.response.data;
    } else if (error.request) {
      // Request was made but no response received
      errorMessage = 'Cannot connect to server. Please check your connection and try again.';
    } else if (error.code === 'ECONNABORTED') {
      // Request timeout
      errorMessage = 'Request timed out. Please try again.';
    } else {
      // Something else happened
      errorMessage = error.message || 'Request failed';
    }

    return Promise.resolve({
      success: false,
      error: errorMessage,
      details: errorDetails,
      status: error.response?.status
    });
  }
);

// Generic API request helper
export const apiRequest = async (endpoint, options = {}) => {
  try {
    console.log(`🌐 API Request: ${options.method || 'GET'} ${API_BASE_URL}${endpoint}`);
    const config = {
      url: endpoint,
      method: options.method || 'GET',
      data: options.data,
      params: options.params,
      ...options
    };

    const response = await apiClient(config);
    console.log(`📡 API Response for ${endpoint}:`, response);
    return response;
  } catch (error) {
    console.error(`💥 API request failed for ${endpoint}:`, error);
    return {
      success: false,
      error: error.error || error.message || 'Request failed',
      details: error.details || null,
      status: error.status || null
    };
  }
};

// Export base URL for API Gateway access
export { API_BASE_URL };
export default apiClient; 