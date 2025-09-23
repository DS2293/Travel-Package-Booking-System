import { apiRequest, DIRECT_SERVICE_URLS } from './apiClient';

class BookingService {
  constructor() {
    this.baseURL = DIRECT_SERVICE_URLS.BOOKING_SERVICE;
  }

  async getAllBookings() {
    return await apiRequest(this.baseURL);
  }

  async getBookingById(bookingId) {
    return await apiRequest(`${this.baseURL}/${bookingId}`);
  }

  async getBookingsByUserId(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}`);
  }

  async getBookingsByPackageId(packageId) {
    return await apiRequest(`${this.baseURL}/package/${packageId}`);
  }

  async createBooking(bookingData) {
    return await apiRequest(this.baseURL, {
      method: 'POST',
      data: bookingData
    });
  }

  async updateBooking(bookingId, bookingData) {
    return await apiRequest(`${this.baseURL}/${bookingId}`, {
      method: 'PUT',
      data: bookingData
    });
  }

  async deleteBooking(bookingId) {
    return await apiRequest(`${this.baseURL}/${bookingId}`, {
      method: 'DELETE'
    });
  }

  async cancelBooking(bookingId) {
    return await apiRequest(`${this.baseURL}/${bookingId}/cancel`, {
      method: 'PUT'
    });
  }
  async confirmBooking(bookingId) {
    return await apiRequest(`${this.baseURL}/${bookingId}/confirm`, {
      method: 'PUT'
    });
  }
  
  // Enhanced methods with cross-service data
  async getUserBookingsWithDetails(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}/with-details`);
  }
  
  async getAgentDashboardData(agentId) {
    return await apiRequest(`${this.baseURL}/agent/${agentId}/dashboard`);
  }
}

export default new BookingService();