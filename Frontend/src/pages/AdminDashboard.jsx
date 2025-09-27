import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { userService, packageService, bookingService, paymentService, assistanceService } from '../services';
import { toast } from 'react-hot-toast';
import LoadingSpinner from '../components/LoadingSpinner';
import '../styles/Dashboard.css';

const AdminDashboard = () => {
  const { currentUser } = useAuth();
  const [allUsers, setAllUsers] = useState([]);
  const [allPackages, setAllPackages] = useState([]);
  const [allBookings, setAllBookings] = useState([]);
  const [allPayments, setAllPayments] = useState([]);
  const [allAssistanceRequests, setAllAssistanceRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Helper functions to get user data with fallbacks for both field name formats
  const getUserName = (user) => user?.name || user?.Name || 'Unknown User';
  const getUserEmail = (user) => user?.email || user?.Email || '';
  const getUserContact = (user) => user?.contactNumber || user?.ContactNumber || '';
  const getUserRole = (user) => user?.role || user?.Role || 'customer';
  const getUserApproval = (user) => user?.approval || user?.Approval || 'approved';
  const getUserId = (user) => user?.userId || user?.UserID;

  useEffect(() => {
    // Load all admin dashboard data on component mount
    loadAdminData();
  }, []);

  // Load all admin dashboard data
  const loadAdminData = async () => {
    setIsLoading(true);
    try {
      // Load all data from backend services
      const [usersResponse, packagesResponse, bookingsResponse, paymentsResponse, assistanceResponse] = await Promise.all([
        userService.getAllUsers(),
        packageService.getAllPackages(),
        bookingService.getAllBookings(),
        paymentService.getAllPayments(),
        assistanceService.getAllAssistanceRequests()
      ]);
      
      // Handle potential double-wrapping of API responses
      setAllUsers(usersResponse.success ? (Array.isArray(usersResponse.data?.data) ? usersResponse.data.data : (Array.isArray(usersResponse.data) ? usersResponse.data : [])) : []);
      setAllPackages(packagesResponse.success ? (Array.isArray(packagesResponse.data?.data) ? packagesResponse.data.data : (Array.isArray(packagesResponse.data) ? packagesResponse.data : [])) : []);
      setAllBookings(bookingsResponse.success ? (Array.isArray(bookingsResponse.data?.data) ? bookingsResponse.data.data : (Array.isArray(bookingsResponse.data) ? bookingsResponse.data : [])) : []);
      setAllPayments(paymentsResponse.success ? (Array.isArray(paymentsResponse.data?.data) ? paymentsResponse.data.data : (Array.isArray(paymentsResponse.data) ? paymentsResponse.data : [])) : []);
      setAllAssistanceRequests(assistanceResponse.success ? (Array.isArray(assistanceResponse.data?.data) ? assistanceResponse.data.data : (Array.isArray(assistanceResponse.data) ? assistanceResponse.data : [])) : []);
      
    } catch (error) {
      console.error('Error loading admin data:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      setIsLoading(false);
    }
  };

  // Refresh all data
  const refreshData = async () => {
    setIsRefreshing(true);
    try {
      await loadAdminData();
      toast.success('Dashboard data refreshed!');
    } catch (error) {
      toast.error('Failed to refresh data');
    } finally {
      setIsRefreshing(false);
    }
  };

  // Calculate statistics using helper functions with array safety checks
  const totalAgents = Array.isArray(allUsers) ? allUsers.filter(user => getUserRole(user) === 'agent').length : 0;
  const totalCustomers = Array.isArray(allUsers) ? allUsers.filter(user => getUserRole(user) === 'customer').length : 0;
  const pendingAgents = Array.isArray(allUsers) ? allUsers.filter(user => getUserRole(user) === 'agent' && getUserApproval(user) === 'pending').length : 0;
  const totalPackages = Array.isArray(allPackages) ? allPackages.length : 0;
  const totalBookings = Array.isArray(allBookings) ? allBookings.length : 0;
  const totalRevenue = Array.isArray(allPayments) ? allPayments.reduce((total, payment) => total + (payment.Amount || payment.amount || 0), 0) : 0;
  const pendingBookings = Array.isArray(allBookings) ? allBookings.filter(booking => (booking.Status || booking.status) === 'pending').length : 0;
  const completedBookings = Array.isArray(allBookings) ? allBookings.filter(booking => (booking.Status || booking.status) === 'confirmed').length : 0;
  const pendingAssistance = Array.isArray(allAssistanceRequests) ? allAssistanceRequests.filter(req => (req.Status || req.status) === 'pending').length : 0;

  const handleRemoveUser = async (userId) => {
    if (window.confirm('Are you sure you want to remove this user?')) {
      try {
        const result = await userService.deleteUser(userId);
        if (result.success) {
          // Update local state
          setAllUsers(Array.isArray(allUsers) ? allUsers.filter(user => getUserId(user) !== userId) : []);
          toast.success('User removed successfully');
        } else {
          toast.error(result.message || 'Failed to remove user');
        }
      } catch (error) {
        console.error('Error removing user:', error);
        toast.error('Failed to remove user');
      }
    }
  };

  const handleUserApproval = async (userId, approval) => {
    try {
      let result;
      if (approval === 'approved') {
        result = await userService.approveUser(userId);
      } else {
        result = await userService.rejectUser(userId);
      }
      
      if (result.success) {
        // Update local state
        setAllUsers(Array.isArray(allUsers) ? allUsers.map(user =>
          getUserId(user) === userId ? { ...user, approval: approval, Approval: approval } : user
        ) : []);
        
        const approvalText = approval === 'approved' ? 'approved' : 'rejected';
        toast.success(`User ${approvalText} successfully`);
      } else {
        toast.error(result.message || `Failed to ${approval} user`);
      }
    } catch (error) {
      console.error('Error updating user approval:', error);
      toast.error('Failed to update user approval');
    }
  };

  const handleAgentApproval = async (userId, approval) => {
    await handleUserApproval(userId, approval);
  };

  // Placeholder functions for future service integrations
  const handleBookingStatusChange = () => {
    // TODO: Implement when booking service is available
    // await bookingService.updateBookingStatus(bookingId, newStatus);
    toast.info('Booking management will be available when booking service is integrated');
  };

  const handleAssistanceStatusChange = () => {
    // TODO: Implement when assistance service is available
    // await assistanceService.updateRequestStatus(requestId, newStatus);
    toast.info('Assistance management will be available when assistance service is integrated');
  };

  const getPackageById = (packageId) => {
    return Array.isArray(allPackages) ? allPackages.find(pkg => pkg.PackageID === packageId) : null;
  };

  const getPaymentById = (paymentId) => {
    return Array.isArray(allPayments) ? allPayments.find(payment => payment.PaymentID === paymentId) : null;
  };

  const getInsuranceByBookingId = () => {
    // For now, return null as insurance data is not fully implemented
    return null;
  };

  return (
    <div className="dashboard">
      {isLoading ? (
        <LoadingSpinner message="Loading admin dashboard..." />
      ) : (
      <div className="container">
        <div className="dashboard-header">
          <div className="header-content">
            <div>
              <h1>Admin Dashboard</h1>
              <p>Welcome back, {getUserName(currentUser)}! Manage the system and oversee operations</p>
            </div>
            <div className="admin-header-actions">
              <button 
                onClick={refreshData}
                className="btn btn-outline refresh-btn"
                disabled={isRefreshing}
              >
                {isRefreshing ? '🔄 Refreshing...' : '🔄 Refresh Data'}
              </button>
              <Link to="/profile" className="btn btn-primary">
                👤 Profile
              </Link>
            </div>
          </div>
        </div>

        {/* Statistics */}
        <section className="dashboard-section">
          <h2>System Overview</h2>
          <div className="admin-stats-grid">
            <div className="stat-card">
              <div className="stat-icon">👥</div>
              <div className="stat-content">
                <h3>{totalCustomers}</h3>
                <p>Total Customers</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">👨‍💼</div>
              <div className="stat-content">
                <h3>{totalAgents}</h3>
                <p>Total Agents</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">⏳</div>
              <div className="stat-content">
                <h3>{pendingAgents}</h3>
                <p>Pending Agents</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📦</div>
              <div className="stat-content">
                <h3>{totalPackages}</h3>
                <p>Total Packages</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📅</div>
              <div className="stat-content">
                <h3>{totalBookings}</h3>
                <p>Total Bookings</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">💰</div>
              <div className="stat-content">
                <h3>${totalRevenue.toFixed(2)}</h3>
                <p>Total Revenue</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">⏳</div>
              <div className="stat-content">
                <h3>{pendingBookings}</h3>
                <p>Pending Bookings</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">✅</div>
              <div className="stat-content">
                <h3>{completedBookings}</h3>
                <p>Completed Bookings</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">💬</div>
              <div className="stat-content">
                <h3>{pendingAssistance}</h3>
                <p>Pending Assistance Requests</p>
              </div>
            </div>
          </div>
        </section>

        {/* Agent Approval Management */}
        <section className="dashboard-section">
          <h2>Agent Approval Management</h2>
          <div className="agent-approval-table">
            {!Array.isArray(allUsers) || allUsers.filter(user => getUserRole(user) === 'agent' && getUserApproval(user) === 'pending').length === 0 ? (
              <div className="no-pending-agents">
                <p>No pending agent approvals</p>
              </div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Contact</th>
                    <th>Registration Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {allUsers
                    .filter(user => getUserRole(user) === 'agent' && getUserApproval(user) === 'pending')
                    .map((agent) => (
                      <tr key={getUserId(agent)}>
                        <td>{getUserName(agent)}</td>
                        <td>{getUserEmail(agent)}</td>
                        <td>{getUserContact(agent)}</td>
                        <td>{new Date().toLocaleDateString()}</td>
                        <td className="action-buttons">
                          <button
                            onClick={() => handleAgentApproval(getUserId(agent), 'approved')}
                            className="btn btn-success btn-sm"
                            title="Approve Agent"
                          >
                            ✅ Approve
                          </button>
                          <button
                            onClick={() => handleAgentApproval(getUserId(agent), 'rejected')}
                            className="btn btn-danger btn-sm"
                            title="Reject Agent"
                          >
                            ❌ Reject
                          </button>
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            )}
          </div>
        </section>

        {/* User Management */}
        <section className="dashboard-section">
          <h2>User Management</h2>
          <div className="users-table">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Contact</th>
                  <th>Approval Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {Array.isArray(allUsers) ? allUsers.map((user) => (
                  <tr key={getUserId(user)}>
                    <td>{getUserName(user)}</td>
                    <td>{getUserEmail(user)}</td>
                    <td>
                      <span className={`role-badge role-${getUserRole(user)}`}>
                        {getUserRole(user)}
                      </span>
                    </td>
                    <td>{getUserContact(user)}</td>
                    <td>
                      <span className={`status-badge status-${getUserApproval(user)}`}>
                        {getUserApproval(user)}
                      </span>
                    </td>
                    <td className="action-buttons">
                      {getUserApproval(user) === 'pending' && (
                        <>
                          <button
                            onClick={() => handleUserApproval(getUserId(user), 'approved')}
                            className="btn btn-success btn-sm"
                            title="Approve User"
                          >
                            ✅
                          </button>
                          <button
                            onClick={() => handleUserApproval(getUserId(user), 'rejected')}
                            className="btn btn-danger btn-sm"
                            title="Reject User"
                          >
                            ❌
                          </button>
                        </>
                      )}
                      {getUserRole(user) !== 'admin' && (
                        <button
                          onClick={() => handleRemoveUser(getUserId(user))}
                          className="btn btn-danger btn-sm"
                          title="Remove User"
                        >
                          🗑️
                        </button>
                      )}
                      {getUserRole(user) === 'admin' && (
                        <span className="admin-protected" title="Admin users cannot be deleted">
                          🔒 Protected
                        </span>
                      )}
                    </td>
                  </tr>
                )) : (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '20px' }}>
                      No users found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* Booking Management */}
        <section className="dashboard-section">
          <h2>Booking Management</h2>
          <div className="bookings-table">
            <table>
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Package</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Status</th>
                  <th>Payment</th>
                  <th>Insurance</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {Array.isArray(allBookings) ? allBookings.map((booking) => {
                  const customer = Array.isArray(allUsers) ? allUsers.find(user => user.UserID === booking.UserID) : null;
                  const packageInfo = getPackageById(booking.PackageID);
                  const paymentInfo = getPaymentById(booking.PaymentID);
                  const insuranceInfo = getInsuranceByBookingId();
                  
                  return (
                    <tr key={booking.BookingID}>
                      <td>{customer?.Name}</td>
                      <td>{packageInfo?.Title}</td>
                      <td>{new Date(booking.StartDate).toLocaleDateString()}</td>
                      <td>{new Date(booking.EndDate).toLocaleDateString()}</td>
                      <td>
                        <span className={`status status-${booking.Status}`}>
                          {booking.Status}
                        </span>
                      </td>
                      <td>
                        {paymentInfo ? (
                          <span className={`payment-status payment-${paymentInfo.Status}`}>
                            {paymentInfo.Status} - ${paymentInfo.Amount}
                          </span>
                        ) : (
                          <span className="payment-pending">Payment Pending</span>
                        )}
                      </td>
                      <td>
                        {insuranceInfo ? (
                          <span className="insurance-status">Active</span>
                        ) : (
                          <span className="insurance-inactive">No Insurance</span>
                        )}
                      </td>
                      <td>
                        <select
                          value={booking.Status}
                          onChange={() => handleBookingStatusChange()}
                          className="status-select"
                        >
                          <option value="pending">Pending</option>
                          <option value="confirmed">Confirmed</option>
                          <option value="completed">Completed</option>
                          <option value="cancelled">Cancelled</option>
                        </select>
                      </td>
                    </tr>
                  );
                }) : (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '20px' }}>
                      No bookings found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* Assistance Requests */}
        <section className="dashboard-section">
          <h2>Customer Assistance Requests</h2>
          {allAssistanceRequests.length === 0 ? (
            <div className="no-requests">
              <p>No assistance requests at the moment.</p>
            </div>
          ) : (
            <div className="assistance-table">
              <table>
                <thead>
                  <tr>
                    <th>Customer</th>
                    <th>Issue Description</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Resolution Time</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {Array.isArray(allAssistanceRequests) ? allAssistanceRequests.map((request) => {
                    const customer = Array.isArray(allUsers) ? allUsers.find(user => user.UserID === request.UserID) : null;
                    
                    return (
                      <tr key={request.RequestID}>
                        <td>
                          <div className="customer-info">
                            <strong>{customer?.Name}</strong>
                            <span className="customer-email">{customer?.Email}</span>
                          </div>
                        </td>
                        <td>
                          <div className="issue-description">
                            {request.IssueDescription}
                          </div>
                        </td>
                        <td>
                          <span className={`priority-badge priority-${request.Priority}`}>
                            {request.Priority}
                          </span>
                        </td>
                        <td>
                          <span className={`status status-${request.Status}`}>
                            {request.Status.replace('_', ' ')}
                          </span>
                        </td>
                        <td>
                          {new Date(request.Timestamp).toLocaleDateString()}
                        </td>
                        <td>
                          {request.ResolutionTime ? (
                            <span className="resolution-time">{request.ResolutionTime}</span>
                          ) : (
                            <span className="no-resolution">Not resolved</span>
                          )}
                        </td>
                        <td>
                          <div className="assistance-actions">
                            <select
                              value={request.Status}
                              onChange={() => handleAssistanceStatusChange()}
                              className="status-select"
                            >
                              <option value="pending">Pending</option>
                              <option value="in_progress">In Progress</option>
                              <option value="completed">Completed</option>
                            </select>
                            {request.Status === 'completed' && !request.ResolutionTime && (
                              <input
                                type="text"
                                placeholder="Resolution time (e.g., 2 hours)"
                                className="resolution-input"
                                onBlur={(e) => {
                                  if (e.target.value.trim()) {
                                    // Update resolution time
                                    const updatedRequests = Array.isArray(allAssistanceRequests) ? allAssistanceRequests.map(req => 
                                      req.RequestID === request.RequestID 
                                        ? { ...req, ResolutionTime: e.target.value.trim() }
                                        : req
                                    ) : [];
                                    setAllAssistanceRequests(updatedRequests);
                                  }
                                }}
                              />
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  }) : (
                    <tr>
                      <td colSpan="7" style={{ textAlign: 'center', padding: '20px' }}>
                        No assistance requests found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
      )}
    </div>
  );
};

export default AdminDashboard; 