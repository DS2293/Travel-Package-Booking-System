import { apiRequest } from './apiClient';

class UserService {
  // Authentication endpoints
  async login(credentials) {
    return await apiRequest('/api/auth/login', {
      method: 'POST',
      data: credentials
    });
  }

  async register(userData) {
    return await apiRequest('/api/auth/register', {
      method: 'POST',
      data: userData
    });
  }


  // User management endpoints (Admin only)
  async getAllUsers() {
    return await apiRequest('/api/users');
  }

  async getUserById(userId) {
    return await apiRequest(`/api/users/${userId}`);
  }

  async getUserByEmail(email) {
    return await apiRequest(`/api/users/email/${email}`);
  }

  async getUsersByRole(role) {
    return await apiRequest(`/api/users/role/${role}`);
  }

  async createUser(userData) {
    return await apiRequest('/api/users', {
      method: 'POST',
      data: userData
    });
  }
  async updateUser(userId, userData) {
    return await apiRequest(`/api/users/${userId}`, {
      method: 'PUT',
      data: userData
    });
  }
  // New method for users to update their own profiles
  async updateProfile(userData) {
    return await apiRequest('/api/users/profile', {
      method: 'PUT',
      data: userData
    });
  }

  // Get current user's profile data
  async getCurrentUser() {
    return await apiRequest('/api/users/profile', {
      method: 'GET'
    });
  }

  async deleteUser(userId) {
    return await apiRequest(`/api/users/${userId}`, {
      method: 'DELETE'
    });
  }

  // Admin approval endpoints
  async getPendingApprovals() {
    return await apiRequest('/api/users/pending-approvals');
  }

  async approveUser(userId) {
    return await apiRequest(`/api/users/${userId}/approve`, {
      method: 'PUT'
    });
  }

  async rejectUser(userId) {
    return await apiRequest(`/api/users/${userId}/reject`, {
      method: 'PUT'
    });
  }

  // Statistics endpoints
  async countUsersByRole(role) {
    return await apiRequest(`/api/users/count/${role}`);
  }

  async countPendingApprovals() {
    return await apiRequest('/api/users/count/pending');
  }

  // Debug endpoint
  async debugHeaders() {
    return await apiRequest('/api/users/debug/headers');
  }
}

export default new UserService(); 