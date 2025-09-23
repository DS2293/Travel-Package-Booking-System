import { apiRequest, DIRECT_SERVICE_URLS } from './apiClient';

class ReviewService {
  constructor() {
    this.baseURL = DIRECT_SERVICE_URLS.REVIEW_SERVICE;
  }

  async getAllReviews() {
    return await apiRequest(this.baseURL);
  }

  async getReviewById(reviewId) {
    return await apiRequest(`${this.baseURL}/${reviewId}`);
  }

  async getReviewsByUserId(userId) {
    return await apiRequest(`${this.baseURL}/user/${userId}`);
  }

  async getReviewsByPackageId(packageId) {
    return await apiRequest(`${this.baseURL}/package/${packageId}`);
  }

  async createReview(reviewData) {
    return await apiRequest(this.baseURL, {
      method: 'POST',
      data: reviewData
    });
  }

  async updateReview(reviewId, reviewData) {
    return await apiRequest(`${this.baseURL}/${reviewId}`, {
      method: 'PUT',
      data: reviewData
    });
  }

  async deleteReview(reviewId) {
    return await apiRequest(`${this.baseURL}/${reviewId}`, {
      method: 'DELETE'
    });
  }

  async addAgentReply(reviewId, replyData) {
    return await apiRequest(`${this.baseURL}/${reviewId}/reply`, {
      method: 'POST',
      data: replyData
    });
  }
}

export default new ReviewService(); 