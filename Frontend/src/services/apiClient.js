import axios from 'axios';

// Base URLs - Now using API Gateway for user service
const API_BASE_URL = 'http://localhost:8080'; // API Gateway
const DIRECT_SERVICE_URLS = {
  PACKAGE_SERVICE: 'http://localhost:8082/api/packages',
  BOOKING_SERVICE: 'http://localhost:8083/api/bookings', 
  PAYMENT_SERVICE: 'http://localhost:8084/api/payments',
  REVIEW_SERVICE: 'http://localhost:8085/api/reviews',
  INSURANCE_SERVICE: 'http://localhost:8086/api/insurance',
  ASSISTANCE_SERVICE: 'http://localhost:8087/api/assistance'
};

// Create axios instance with default configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
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
    
    if (error.response) {
      errorMessage = error.response.data?.message || 
                    error.response.data?.error || 
                    `Error ${error.response.status}`;
    } else if (error.request) {
      errorMessage = 'Cannot connect to server';
    }

    return Promise.resolve({
      success: false,
      error: errorMessage,
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
      error: error.message || 'Request failed'
    };
  }
};

// Export base URLs for direct service access
export { API_BASE_URL, DIRECT_SERVICE_URLS };
export default apiClient; 