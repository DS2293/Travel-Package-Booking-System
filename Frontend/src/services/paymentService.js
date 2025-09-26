import { apiRequest } from './apiClient';

class PaymentService {
  constructor() {
    this.baseURL = '/api/payments'; // API Gateway route
  }

  async getAllPayments() {
    return await apiRequest(this.baseURL);
  }

  async getPaymentById(paymentId) {
    return await apiRequest(`${this.baseURL}/${paymentId}`);
  }

  async getPaymentsByUserId(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}`);
  }

  async getPaymentByBookingId(bookingId) {
    return await apiRequest(`${this.baseURL}/booking/${bookingId}`);
  }

  async createPayment(paymentData) {
    return await apiRequest(this.baseURL, {
      method: 'POST',
      data: paymentData
    });
  }

  async updatePayment(paymentId, paymentData) {
    return await apiRequest(`${this.baseURL}/${paymentId}`, {
      method: 'PUT',
      data: paymentData
    });
  }

  async processPayment(paymentData) {
    return await apiRequest(`${this.baseURL}/process`, {
      method: 'POST',
      data: paymentData
    });
  }
  async refundPayment(paymentId) {
    return await apiRequest(`${this.baseURL}/${paymentId}/refund`, {
      method: 'PUT'
    });
  }
  
  // Enhanced methods with cross-service data
  async getUserPaymentsWithDetails(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}/with-details`);
  }
  
  async getPaymentWithDetails(paymentId) {
    return await apiRequest(`${this.baseURL}/${paymentId}/with-details`);
  }
}

export default new PaymentService();