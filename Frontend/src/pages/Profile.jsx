import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { userService } from '../services';
import { toast } from 'react-hot-toast';
import LoadingSpinner from '../components/LoadingSpinner';
import '../styles/Profile.css';
import { Link } from 'react-router-dom';

const Profile = () => {
  const { currentUser, updateUser, refreshUserData } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [userBookings, setUserBookings] = useState([]);
  const [userPackages, setUserPackages] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [allPackages, setAllPackages] = useState([]);
  const [allBookings, setAllBookings] = useState([]);  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
    
  // Helper functions to get user data with fallbacks
  const getUserName = () => currentUser?.name || currentUser?.Name || 'Unknown User';
  const getUserEmail = () => currentUser?.email || currentUser?.Email || '';
  const getUserContactNumber = () => currentUser?.contactNumber || currentUser?.ContactNumber || '';
  const getUserRole = () => currentUser?.role || currentUser?.Role || 'customer';
  const getUserApproval = () => currentUser?.approval || currentUser?.Approval || 'approved';

  const [formData, setFormData] = useState({
    name: getUserName(),
    email: getUserEmail(),
    contactNumber: getUserContactNumber(),
    password: '',
    confirmPassword: ''
  });

  // Debug log to see the actual user data structure
  useEffect(() => {
    if (currentUser) {
      console.log('👤 Profile - Current user data:', currentUser);
    }
  }, [currentUser]);

  useEffect(() => {
    if (currentUser) {
      setFormData({
        name: getUserName(),
        email: getUserEmail(),
        contactNumber: getUserContactNumber(),
        password: '',
        confirmPassword: ''
      });
      
      // Load user-specific data based on role
      loadUserData();
    }  }, [currentUser]); // eslint-disable-line react-hooks/exhaustive-deps

  // Function to refresh user data and update form
  const handleRefreshUserData = async () => {
    if (!currentUser) return;
    
    setIsRefreshing(true);
    try {
      // Use AuthContext's refresh function
      const result = await refreshUserData();
      if (result.success && result.data) {
        // Update form data with fresh values from database
        setFormData({
          name: result.data.name || getUserName(),
          email: result.data.email || getUserEmail(),
          contactNumber: result.data.contactNumber || getUserContactNumber(),
          password: '',
          confirmPassword: ''
        });
        
        toast.success('Profile data refreshed!');
      } else {
        toast.error(result.message || 'Failed to refresh profile data');
      }
    } catch (error) {
      console.error('Error refreshing user data:', error);
      toast.error('Failed to refresh profile data');
    } finally {
      setIsRefreshing(false);
    }
  };

  const loadUserData = async () => {
    if (!currentUser) return;
    
    setIsLoading(true);
    try {
      const userRole = getUserRole();

      // Load data based on user role
      if (userRole === 'customer') {
        // Load customer's bookings
        // const bookings = await bookingService.getBookingsByUserId(userId);
        // setUserBookings(bookings.data || []);
        setUserBookings([]); // Placeholder until booking service is implemented
      } else if (userRole === 'agent') {
        // Load agent's packages
        // const packages = await packageService.getPackagesByAgentId(userId);
        // setUserPackages(packages.data || []);
        setUserPackages([]); // Placeholder until package service is implemented
      } else if (userRole === 'admin') {
        // Load admin dashboard data
        try {
          const usersResponse = await userService.getAllUsers();
          // TODO: Add package and booking services when available
          // const packagesResponse = await packageService.getAllPackages();
          // const bookingsResponse = await bookingService.getAllBookings();
          
          setAllUsers(usersResponse.data || []);
          setAllPackages([]); // Placeholder until package service is implemented
          setAllBookings([]); // Placeholder until booking service is implemented
        } catch (error) {
          console.error('Error loading admin data:', error);
          // Set empty arrays as fallback
          setAllUsers([]);
          setAllPackages([]);
          setAllBookings([]);
        }
      }
    } catch (error) {
      console.error('Error loading user data:', error);
      toast.error('Failed to load user data');
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }    try {
      const updatedUser = {
        name: formData.name,
        email: formData.email,
        contactNumber: formData.contactNumber,
        role: getUserRole(),
        approval: getUserApproval() // Preserve approval status
      };

      if (formData.password) {
        updatedUser.password = formData.password;
      }

      console.log('🔄 Updating user profile:', updatedUser);      // Update via API using the new profile endpoint
      const result = await userService.updateProfile(updatedUser);
        if (result.success) {
        // Update auth context with the new user data from backend response
        const updatedUserData = {
          ...currentUser,
          ...result.data  // Use the response from backend which includes all preserved fields
        };        await updateUser(updatedUserData);        toast.success('Profile updated successfully!');
        setIsEditing(false);
        
        // Auto-refresh user data from database after 1 second
        setTimeout(async () => {
          await handleRefreshUserData();
        }, 1000);
      } else {
        toast.error(result.message || 'Failed to update profile');
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      toast.error('Failed to update profile. Please try again.');
    }
  };

  const getPackageById = (packageId) => {
    return allPackages.find(pkg => pkg.packageId === packageId || pkg.PackageID === packageId);
  };

  const getBookingStatus = (status) => {
    const statusColors = {
      'confirmed': 'status-confirmed',
      'pending': 'status-pending',
      'completed': 'status-completed',
      'cancelled': 'status-cancelled'
    };
    return statusColors[status] || 'status-pending';
  };

  const renderCustomerProfile = () => (
    <div className="profile-section">
      <h3>My Bookings</h3>
      {isLoading ? (
        <LoadingSpinner />
      ) : userBookings.length === 0 ? (
        <div className="no-data">
          <p>You haven't made any bookings yet.</p>
          <Link to="/packages" className="btn btn-primary">Browse Packages</Link>
        </div>
      ) : (
        <div className="bookings-grid">
          {userBookings.map((booking) => {
            const packageInfo = getPackageById(booking.packageId || booking.PackageID);
            return (
              <div key={booking.bookingId || booking.BookingID} className="booking-card">
                <div className="booking-header">
                  <h4>{packageInfo?.title || packageInfo?.Title || 'Unknown Package'}</h4>
                  <span className={`status-badge ${getBookingStatus(booking.status || booking.Status)}`}>
                    {booking.status || booking.Status}
                  </span>
                </div>
                <div className="booking-details">
                  <p><strong>Start Date:</strong> {new Date(booking.startDate || booking.StartDate).toLocaleDateString()}</p>
                  <p><strong>End Date:</strong> {new Date(booking.endDate || booking.EndDate).toLocaleDateString()}</p>
                  <p><strong>Package:</strong> {packageInfo?.duration || packageInfo?.Duration || 'N/A'}</p>
                  <p><strong>Price:</strong> ${packageInfo?.price || packageInfo?.Price || 'N/A'}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  const renderAgentProfile = () => (
    <div className="profile-section">
      <h3>My Packages</h3>
      {isLoading ? (
        <LoadingSpinner />
      ) : userPackages.length === 0 ? (
        <div className="no-data">
          <p>You haven't created any packages yet.</p>
          <Link to="/agent-dashboard" className="btn btn-primary">Create Package</Link>
        </div>
      ) : (
        <div className="packages-grid">
          {userPackages.map((pkg) => (
            <div key={pkg.packageId || pkg.PackageID} className="package-card">
              <div className="package-image">
                <img 
                  src={pkg.image || pkg.Image} 
                  alt={pkg.title || pkg.Title}
                  onError={(e) => {
                    e.target.style.display = 'none';
                    e.target.parentElement.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
                  }}
                />
                <div className="package-price">${pkg.price || pkg.Price}</div>
              </div>
              <div className="package-content">
                <h4>{pkg.title || pkg.Title}</h4>
                <p>{pkg.description || pkg.Description}</p>
                <div className="package-details">
                  <span>⏱️ {pkg.duration || pkg.Duration}</span>
                  <span>📋 {(pkg.includedServices || pkg.IncludedServices || '').split(', ').slice(0, 2).join(', ')}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  const renderAdminProfile = () => (
    <div className="profile-section">
      <h3>System Overview</h3>
      <div className="admin-stats">
        <div className="stat-item">
          <span className="stat-number">{allUsers.filter(u => (u.role || u.Role) === 'customer').length}</span>
          <span className="stat-label">Total Customers</span>
        </div>
        <div className="stat-item">
          <span className="stat-number">{allUsers.filter(u => (u.role || u.Role) === 'agent').length}</span>
          <span className="stat-label">Total Agents</span>
        </div>
        <div className="stat-item">
          <span className="stat-number">{allPackages.length}</span>
          <span className="stat-label">Total Packages</span>
        </div>
        <div className="stat-item">
          <span className="stat-number">{allBookings.length}</span>
          <span className="stat-label">Total Bookings</span>
        </div>
      </div>
    </div>
  );

  if (!currentUser) {
    return (
      <div className="profile-page">
        <div className="container">
          <div className="not-authenticated">
            <h2>Please sign in to view your profile</h2>
            <Link to="/signin" className="btn btn-primary">Sign In</Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <div className="container">        <div className="profile-header">
          <div className="header-content">
            <div>
              <h1>Profile Management</h1>
              <p>Manage your account information and preferences</p>
            </div>            <button 
              onClick={handleRefreshUserData}
              className="btn btn-outline refresh-btn"
              disabled={isRefreshing}
            >
              {isRefreshing ? '🔄 Refreshing...' : '🔄 Refresh Data'}
            </button>
          </div>
        </div>

        {/* Profile Information */}
        <div className="profile-section">
          <div className="section-header">
            <h3>Personal Information</h3>
            <button 
              onClick={() => setIsEditing(!isEditing)}
              className="btn btn-secondary"
            >
              {isEditing ? 'Cancel' : 'Edit Profile'}
            </button>
          </div>

          {isEditing ? (
            <form onSubmit={handleSubmit} className="profile-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Full Name</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    className="form-control"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className="form-control"
                    required
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Contact Number</label>
                  <input
                    type="tel"
                    name="contactNumber"
                    value={formData.contactNumber}
                    onChange={handleInputChange}
                    className="form-control"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Role</label>
                  <input
                    type="text"
                    value={getUserRole()}
                    className="form-control"
                    disabled
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>New Password (optional)</label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    className="form-control"
                    placeholder="Leave blank to keep current password"
                  />
                </div>
                <div className="form-group">
                  <label>Confirm New Password</label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                    className="form-control"
                    placeholder="Confirm new password"
                  />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  Save Changes
                </button>
                <button 
                  type="button" 
                  onClick={() => setIsEditing(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : (
            <div className="profile-info">
              <div className="info-row">
                <div className="info-item">
                  <label>Full Name:</label>
                  <span>{getUserName()}</span>
                </div>
                <div className="info-item">
                  <label>Email:</label>
                  <span>{getUserEmail()}</span>
                </div>
              </div>
              <div className="info-row">
                <div className="info-item">
                  <label>Contact Number:</label>
                  <span>{getUserContactNumber()}</span>
                </div>
                <div className="info-item">
                  <label>Role:</label>
                  <span className={`role-badge role-${getUserRole()}`}>
                    {getUserRole()}
                  </span>
                </div>
              </div>
              <div className="info-row">
                <div className="info-item">
                  <label>Account Status:</label>
                  <span className={`status-badge status-${getUserApproval()}`}>
                    {getUserApproval()}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Role-specific sections */}
        {getUserRole() === 'customer' && renderCustomerProfile()}
        {getUserRole() === 'agent' && renderAgentProfile()}
        {getUserRole() === 'admin' && renderAdminProfile()}
      </div>
    </div>
  );
};

export default Profile;
