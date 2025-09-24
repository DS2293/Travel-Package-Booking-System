import { apiRequest, DIRECT_SERVICE_URLS } from './apiClient';

class AssistanceService {
  constructor() {
    this.baseURL = DIRECT_SERVICE_URLS.ASSISTANCE_SERVICE;
  }

  async getAllAssistanceRequests() {
    return await apiRequest(this.baseURL);
  }

  async getAssistanceRequestById(requestId) {
    return await apiRequest(`${this.baseURL}/${requestId}`);
  }

  async getAssistanceRequestsByUserId(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}`);
  }

  async createAssistanceRequest(requestData) {
    return await apiRequest(this.baseURL, {
      method: 'POST',
      data: requestData
    });
  }

  async updateAssistanceRequest(requestId, requestData) {
    return await apiRequest(`${this.baseURL}/${requestId}`, {
      method: 'PUT',
      data: requestData
    });
  }

  async deleteAssistanceRequest(requestId) {
    return await apiRequest(`${this.baseURL}/${requestId}`, {
      method: 'DELETE'
    });
  }

  async updateAssistanceRequestStatus(requestId, status) {
    return await apiRequest(`${this.baseURL}/${requestId}/status`, {
      method: 'PUT',
      data: { status }
    });
  }

  async resolveAssistanceRequest(requestId, resolutionData) {
    return await apiRequest(`${this.baseURL}/${requestId}/resolve`, {
      method: 'PUT',
      data: resolutionData
    });
  }
  
  // Enhanced methods utilizing cross-service integration  
  async getAssistanceRequestsWithDetails() {
    return await apiRequest(`${this.baseURL}`, {
      params: { enhanced: true }
    });
  }

  async getAssistanceRequestsByUserIdWithDetails(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}`, {
      params: { enhanced: true }
    });
  }

  async getAssistanceRequestByIdWithDetails(requestId) {
    return await apiRequest(`${this.baseURL}/${requestId}`, {
      params: { enhanced: true }
    });
  }
}

export default new AssistanceService();