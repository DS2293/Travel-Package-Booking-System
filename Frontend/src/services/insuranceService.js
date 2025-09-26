import { apiRequest } from './apiClient';

class InsuranceService {
  constructor() {
    this.baseURL = '/api/insurance'; // API Gateway route
  }

  async getAllInsurance() {
    return await apiRequest(this.baseURL);
  }

  async getInsuranceById(insuranceId) {
    return await apiRequest(`${this.baseURL}/${insuranceId}`);
  }

  async getInsuranceByUserId(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}`);
  }

  async getInsuranceByBookingId(bookingId) {
    return await apiRequest(`${this.baseURL}/booking/${bookingId}`);
  }

  async createInsurance(insuranceData) {
    return await apiRequest(this.baseURL, {
      method: 'POST',
      data: insuranceData
    });
  }

  async updateInsurance(insuranceId, insuranceData) {
    return await apiRequest(`${this.baseURL}/${insuranceId}`, {
      method: 'PUT',
      data: insuranceData
    });
  }

  async cancelInsurance(insuranceId) {
    return await apiRequest(`${this.baseURL}/${insuranceId}/cancel`, {
      method: 'PUT'
    });
  }
  async getInsuranceTypes() {
    return await apiRequest(`${this.baseURL}/types`);
  }
  
  // Enhanced methods with cross-service data
  async getUserInsuranceWithDetails(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}/with-details`);
  }
  
  async getInsuranceWithDetails(insuranceId) {
    return await apiRequest(`${this.baseURL}/${insuranceId}/with-details`);
  }
  
  async getInsuranceQuotes(bookingId) {
    return await apiRequest(`${this.baseURL}/quotes/booking/${bookingId}`);
  }
  
  async renewInsurance(insuranceId) {
    return await apiRequest(`${this.baseURL}/${insuranceId}/renew`, {
      method: 'PUT'
    });
  }
}

export default new InsuranceService();