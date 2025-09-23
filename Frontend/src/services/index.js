// Main services export file
import userService from './userService';
import packageService from './packageService';
import bookingService from './bookingService';
import paymentService from './paymentService';
import reviewService from './reviewService';
import insuranceService from './insuranceService';
import assistanceService from './assistanceService';

// Export individual services
export {
  userService,
  packageService,
  bookingService,
  paymentService,
  reviewService,
  insuranceService,
  assistanceService
};

// Export combined services object for easy access
export const services = {
  user: userService,
  package: packageService,
  booking: bookingService,
  payment: paymentService,
  review: reviewService,
  insurance: insuranceService,
  assistance: assistanceService
};

// Dashboard helper functions
export const dashboardHelpers = {
  async getUserDashboardData(userId) {
    try {
      const [bookingsResult, paymentsResult, packagesResult] = await Promise.all([
        bookingService.getBookingsByUserId(userId),
        paymentService.getPaymentsByUserId(userId),
        packageService.getAllPackages()
      ]);
      
      return {
        success: true,
        data: {
          bookings: bookingsResult.success ? bookingsResult.data : [],
          payments: paymentsResult.success ? paymentsResult.data : [],
          packages: packagesResult.success ? packagesResult.data : []
        }
      };
    } catch (error) {
      return { success: false, error: error.message };
    }
  },
    async getAgentDashboardData(agentId) {
    try {
      // Use the new enhanced booking service endpoint
      const dashboardResult = await bookingService.getAgentDashboardData(agentId);
      
      if (dashboardResult.success) {
        return {
          success: true,
          data: dashboardResult.data
        };
      } else {
        // Fallback to old method if new endpoint fails
        const [packagesResult, bookingsResult, paymentsResult] = await Promise.all([
          packageService.getPackagesByAgentId(agentId),
          bookingService.getAllBookings(),
          paymentService.getAllPayments()
        ]);
        
        const agentPackages = packagesResult.success ? packagesResult.data : [];
        const packageIds = agentPackages.map(pkg => pkg.packageId);
        
        const allBookings = bookingsResult.success ? bookingsResult.data : [];
        const allPayments = paymentsResult.success ? paymentsResult.data : [];
        
        const agentBookings = allBookings.filter(booking => 
          packageIds.includes(booking.packageId)
        );
        const agentPayments = allPayments.filter(payment => 
          agentBookings.some(booking => booking.bookingId === payment.bookingId)
        );
        
        return {
          success: true,
          data: {
            packages: agentPackages,
            bookings: agentBookings,
            payments: agentPayments
          }
        };
      }
    } catch (error) {
      return { success: false, error: error.message };
    }
  },
  
  async getAdminDashboardData() {
    try {
      const [usersResult, packagesResult, bookingsResult, paymentsResult, assistanceResult] = await Promise.all([
        userService.getAllUsers(),
        packageService.getAllPackages(),
        bookingService.getAllBookings(),
        paymentService.getAllPayments(),
        assistanceService.getAllAssistanceRequests()
      ]);
      
      return {
        success: true,
        data: {
          users: usersResult.success ? usersResult.data : [],
          packages: packagesResult.success ? packagesResult.data : [],
          bookings: bookingsResult.success ? bookingsResult.data : [],
          payments: paymentsResult.success ? paymentsResult.data : [],
          assistanceRequests: assistanceResult.success ? assistanceResult.data : []
        }
      };
    } catch (error) {
      return { success: false, error: error.message };
    }
  }
};

export default services; 