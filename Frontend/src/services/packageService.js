import { apiRequest } from './apiClient';

class PackageService {
  constructor() {
    this.baseURL = '/api/packages'; // API Gateway route
  }

  async getAllPackages() {
    return await apiRequest(this.baseURL);
  }

  async getPackageById(packageId) {
    return await apiRequest(`${this.baseURL}/${packageId}`);
  }

  async getPackagesByAgentId(agentId) {
    return await apiRequest(`${this.baseURL}/agent/${agentId}`);
  }

  async createPackage(packageData) {
    return await apiRequest(this.baseURL, {
      method: 'POST',
      data: packageData
    });
  }

  async updatePackage(packageId, packageData) {
    return await apiRequest(`${this.baseURL}/${packageId}`, {
      method: 'PUT',
      data: packageData
    });
  }

  async deletePackage(packageId) {
    return await apiRequest(`${this.baseURL}/${packageId}`, {
      method: 'DELETE'
    });
  }
  async searchPackages(searchParams) {
    return await apiRequest(`${this.baseURL}/search`, {
      params: searchParams
    });
  }
  
  // Enhanced methods with cross-service data
  async getPackageWithDetails(packageId) {
    return await apiRequest(`${this.baseURL}/${packageId}/with-details`);
  }
  
  async getAgentPackagesWithStats(agentId) {
    return await apiRequest(`${this.baseURL}/agent/${agentId}/with-stats`);
  }
}

export default new PackageService();