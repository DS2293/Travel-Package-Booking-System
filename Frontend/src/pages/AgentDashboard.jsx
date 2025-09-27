import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { bookingService, packageService } from '../services';
import { toast } from 'react-hot-toast';
import '../styles/Dashboard.css';

const AgentDashboard = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedPackage, setSelectedPackage] = useState(null);
  const [dashboardData, setDashboardData] = useState({
    packages: [],
    bookings: [],
    totalBookings: 0,
    confirmedBookings: 0,
    pendingBookings: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAgentDashboardData();
  }, []); // No dependency on currentUser.UserID since JWT handles authentication

  const loadAgentDashboardData = async () => {
    setLoading(true);
    try {
      // Use JWT-based package service to get agent's packages and stats
      const result = await packageService.getMyPackagesWithStats();
      
      if (result.success) {
        // The API response is wrapped, so we need to access result.data.data
        const actualData = result.data.data || result.data;
        setDashboardData(actualData);
      } else {
        toast.error('Failed to load dashboard data');
      }
    } catch (error) {
      console.error('Failed to load agent dashboard data:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  // Use total revenue from backend (calculated from completed payments)
  const totalRevenue = dashboardData.totalRevenue || 0;

  const confirmedBookings = dashboardData.totalConfirmedBookings || 0;
  const pendingBookings = dashboardData.pendingBookings || 0;
  const totalBookings = dashboardData.totalBookings || 0;

  const handleCreatePackage = async (packageData) => {
    // Validate required fields
    if (!packageData.title || packageData.title.trim() === '') {
      toast.error('Package title is required');
      return;
    }
    
    if (!packageData.duration || packageData.duration.trim() === '') {
      toast.error('Package duration is required');
      return;
    }
    
    if (!packageData.price || parseFloat(packageData.price) <= 0) {
      toast.error('Package price is required and must be greater than 0');
      return;
    }

    try {
      const newPackageData = {
        title: packageData.title,
        description: packageData.description,
        duration: packageData.duration,
        price: parseFloat(packageData.price), // Convert to number - backend will convert to BigDecimal
        includedServices: packageData.services,
        image: packageData.image || "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop&crop=center&auto=format"
        // agentId will be extracted from JWT token by backend
      };
      
      console.log('📤 Creating package with data:', newPackageData);

      const result = await packageService.createPackage(newPackageData);
      if (result.success) {
        toast.success('Travel package created successfully');
        
        // Reload dashboard data to reflect the new package
        await loadAgentDashboardData();
        
        // Close modal and reset form
        setShowCreateModal(false);
        document.getElementById('title').value = '';
        document.getElementById('description').value = '';
        document.getElementById('duration').value = '';
        document.getElementById('price').value = '';
        document.getElementById('services').value = '';
        document.getElementById('imageUrl').value = '';
      } else {
        toast.error('Failed to create package');
      }
    } catch (error) {
      console.error('Failed to create package:', error);
      toast.error('Failed to create package');
    }
  };

  const handleUpdatePackage = async (packageId, updates) => {
    // Validate required fields
    if (!updates.title || updates.title.trim() === '') {
      toast.error('Package title is required');
      return;
    }
    
    if (!updates.duration || updates.duration.trim() === '') {
      toast.error('Package duration is required');
      return;
    }
    
    if (!updates.price || parseFloat(updates.price) <= 0) {
      toast.error('Package price is required and must be greater than 0');
      return;
    }

    console.log('🔍 Update Package Data:', {
      packageId,
      updates
    });

    try {
      // Ensure proper data types
      const updateData = {
        title: updates.title.trim(),
        description: updates.description || '',
        duration: updates.duration.trim(),
        price: parseFloat(updates.price), // Convert to number, backend expects BigDecimal from number
        includedServices: updates.includedServices || '',
        image: updates.image || ''
      };

      console.log('📤 Sending update data:', updateData);

      const result = await packageService.updatePackage(packageId, updateData);
      if (result.success) {
        toast.success('Travel package updated successfully');
        // Reload dashboard data to reflect the changes
        await loadAgentDashboardData();
        setShowEditModal(false);
      } else {
        console.error('❌ Update failed:', result);
        toast.error('Failed to update package: ' + (result.error || 'Unknown error'));
      }
    } catch (error) {
      console.error('Failed to update package:', error);
      toast.error('Failed to update package');
    }
  };

  const handleDeletePackage = async (packageId) => {
    if (window.confirm('Are you sure you want to delete this package?')) {
      try {
        const result = await packageService.deletePackage(packageId);
        if (result.success) {
          toast.success('Travel package deleted successfully');
          // Reload dashboard data to reflect the changes
          await loadAgentDashboardData();
        } else {
          toast.error('Failed to delete package');
        }
      } catch (error) {
        console.error('Failed to delete package:', error);
        toast.error('Failed to delete package');
      }
    }
  };

  const getUserById = (userId) => {
    // Return placeholder for user name since we don't load all users
    return { UserID: userId, Name: `User ${userId}` };
  };

  const getPackageById = (packageId) => {
    return dashboardData.packages?.find(pkg => (pkg.packageId || pkg.PackageID) === packageId);
  };

  const getPaymentById = (paymentId) => {
    // Return placeholder for payment info
    return { PaymentID: paymentId, Status: 'Unknown', Amount: 0 };
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <div className="header-content">
            <div>
              <h1>Agent Dashboard</h1>
              <p>Welcome back, {currentUser.Name}! Manage your packages and view bookings</p>
            </div>
            <div className="header-actions">
              {/* Data refreshes automatically when component loads */}
            </div>
          </div>
        </div>

        {/* Statistics */}
        <section className="dashboard-section">
          <h2>Overview Statistics</h2>
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon">📦</div>
              <div className="stat-content">
                <h3>{dashboardData.packages ? dashboardData.packages.length : 0}</h3>
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
              <div className="stat-icon">✅</div>
              <div className="stat-content">
                <h3>{confirmedBookings}</h3>
                <p>Confirmed Bookings</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">💰</div>
              <div className="stat-content">
                <h3>${totalRevenue.toFixed(2)}</h3>
                <p>Total Revenue</p>
              </div>
            </div>
          </div>
        </section>

        {/* Package Management */}
        <section className="dashboard-section">
          <div className="section-header">
            <h2>Your Travel Packages</h2>
            <button 
              onClick={() => setShowCreateModal(true)}
              className="btn btn-primary"
            >
              Create New Package
            </button>
          </div>
          
          {!dashboardData.packages || dashboardData.packages.length === 0 ? (
            <div className="no-packages">
              <p>You haven't created any packages yet.</p>
            </div>
          ) : (
            <div className="packages-grid">
              {dashboardData.packages.map((pkg) => (
                <div key={pkg.packageId || pkg.PackageID} className="package-card">
                  <div className="package-image">
                    <img 
                      src={pkg.image || pkg.Image} 
                      alt={pkg.title || pkg.Title}
                      onError={(e) => {
                        e.target.style.display = 'none';
                        e.target.parentElement.style.background = 'linear-gradient(45deg, #667eea, #764ba2)';
                      }}
                    />
                    <div className="package-price">${pkg.price || pkg.Price}</div>
                  </div>
                  <div className="package-content">
                    <h3>{pkg.title || pkg.Title}</h3>
                    <p>{pkg.description || pkg.Description}</p>
                    <div className="package-details">
                      <span>⏱️ {pkg.duration || pkg.Duration}</span>
                      <span>📋 {(pkg.includedServices || pkg.IncludedServices || '').split(', ').slice(0, 2).join(', ')}</span>
                    </div>
                    <div className="package-actions">
                      <button 
                        onClick={() => {
                          setSelectedPackage(pkg);
                          setShowEditModal(true);
                        }}
                        className="btn btn-secondary btn-sm"
                      >
                        Edit
                      </button>
                      <button 
                        onClick={() => handleDeletePackage(pkg.packageId || pkg.PackageID)}
                        className="btn btn-danger btn-sm"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Bookings Table */}
        <section className="dashboard-section">
          <h2>Booking Overview</h2>
          {!dashboardData.bookings || dashboardData.bookings.length === 0 ? (
            <div className="no-bookings">
              <p>No bookings for your packages yet.</p>
            </div>
          ) : (
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
                  </tr>
                </thead>
                <tbody>
                  {dashboardData.bookings.map((booking) => {
                    // ✅ Use enriched data from backend first, then fallback to helper functions
                    const customer = booking.userInfo || getUserById(booking.userId || booking.UserID);
                    const packageInfo = booking.packageInfo || getPackageById(booking.packageId || booking.PackageID);
                    const paymentInfo = booking.paymentInfo || getPaymentById(booking.paymentId || booking.PaymentID);
                    
                    return (
                      <tr key={booking.bookingId || booking.BookingID}>
                        <td>{customer?.name || customer?.Name || `User ${booking.userId || booking.UserID}`}</td>
                        <td>{packageInfo?.title || packageInfo?.Title || 'Package Unavailable'}</td>
                        <td>{new Date(booking.startDate || booking.StartDate).toLocaleDateString()}</td>
                        <td>{new Date(booking.endDate || booking.EndDate).toLocaleDateString()}</td>
                        <td>
                          <span className={`status status-${booking.status || booking.Status}`}>
                            {(booking.status || booking.Status)?.toUpperCase()}
                          </span>
                        </td>
                        <td>
                          {paymentInfo ? (
                            <span className={`payment-status payment-${(paymentInfo.status || paymentInfo.Status)?.toLowerCase()}`}>
                              {(paymentInfo.status || paymentInfo.Status)?.toUpperCase()} - ${paymentInfo.amount || paymentInfo.Amount}
                            </span>
                          ) : (
                            <span className="payment-pending">PAYMENT PENDING</span>
                          )}
                        </td>
                        <td>
                          <span className="insurance-status">N/A</span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* Create Package Modal */}
        {showCreateModal && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h3>Create New Travel Package</h3>
                <button 
                  onClick={() => setShowCreateModal(false)}
                  className="modal-close"
                >
                  ×
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Package Title</label>
                  <input type="text" id="title" className="form-control" placeholder="Enter package title" />
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <textarea id="description" className="form-control" placeholder="Enter package description" rows="3"></textarea>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Duration</label>
                    <input type="text" id="duration" className="form-control" placeholder="e.g., 5 days" />
                  </div>
                  <div className="form-group">
                    <label>Price ($)</label>
                    <input type="number" id="price" className="form-control" placeholder="Enter price" step="0.01" />
                  </div>
                </div>
                <div className="form-group">
                  <label>Included Services</label>
                  <input type="text" id="services" className="form-control" placeholder="e.g., Hotel, Flights, Tours" />
                </div>
                <div className="form-group">
                  <label>Image URL (optional)</label>
                  <input type="url" id="imageUrl" className="form-control" placeholder="Enter image URL" />
                </div>
              </div>
              <div className="modal-footer">
                <button 
                  onClick={() => setShowCreateModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button 
                  onClick={() => handleCreatePackage({
                    title: document.getElementById('title').value,
                    description: document.getElementById('description').value,
                    duration: document.getElementById('duration').value,
                    price: document.getElementById('price').value,
                    services: document.getElementById('services').value,
                    image: document.getElementById('imageUrl').value
                  })}
                  className="btn btn-primary"
                >
                  Create Package
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Edit Package Modal */}
        {showEditModal && selectedPackage && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h3>Edit Travel Package</h3>
                <button 
                  onClick={() => setShowEditModal(false)}
                  className="modal-close"
                >
                  ×
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Package Title</label>
                  <input type="text" id="editTitle" className="form-control" defaultValue={selectedPackage?.title || selectedPackage?.Title} />
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <textarea id="editDescription" className="form-control" defaultValue={selectedPackage?.description || selectedPackage?.Description} rows="3"></textarea>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Duration</label>
                    <input type="text" id="editDuration" className="form-control" defaultValue={selectedPackage?.duration || selectedPackage?.Duration} />
                  </div>
                  <div className="form-group">
                    <label>Price ($)</label>
                    <input type="number" id="editPrice" className="form-control" defaultValue={selectedPackage?.price || selectedPackage?.Price} step="0.01" />
                  </div>
                </div>
                <div className="form-group">
                  <label>Included Services</label>
                  <input type="text" id="editServices" className="form-control" defaultValue={selectedPackage?.includedServices || selectedPackage?.IncludedServices} />
                </div>
                <div className="form-group">
                  <label>Image URL (optional)</label>
                  <input type="url" id="editImageUrl" className="form-control" defaultValue={selectedPackage?.image || selectedPackage?.Image} />
                </div>
              </div>
              <div className="modal-footer">
                <button 
                  onClick={() => setShowEditModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button 
                  onClick={() => handleUpdatePackage(selectedPackage?.packageId || selectedPackage?.PackageID, {
                    title: document.getElementById('editTitle').value,
                    description: document.getElementById('editDescription').value,
                    duration: document.getElementById('editDuration').value,
                    price: document.getElementById('editPrice').value,
                    includedServices: document.getElementById('editServices').value,
                    image: document.getElementById('editImageUrl').value
                  })}
                  className="btn btn-primary"
                >
                  Update Package
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AgentDashboard; 