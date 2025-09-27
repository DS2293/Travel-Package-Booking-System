import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { bookingService, packageService, paymentService } from '../services';
import { insuranceTypes } from '../constants/insuranceTypes';
import { toast } from 'react-hot-toast';
import '../styles/Dashboard.css';
import { Link } from 'react-router-dom';

const UserDashboard = () => {
  const { currentUser } = useAuth();
  const [selectedPackage, setSelectedPackage] = useState(null);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedInsurance, setSelectedInsurance] = useState(null);
  const [currentBooking, setCurrentBooking] = useState(null); // Store the booking being processed
  const [userBookings, setUserBookings] = useState([]);
  const [userPayments, setUserPayments] = useState([]);
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUserData();
  }, []); // No dependency on currentUser.UserID since JWT handles authentication

  const loadUserData = async () => {
    setLoading(true);
    try {
      // Use JWT-based booking service to get bookings with package details
      const bookingsResult = await bookingService.getMyBookingsWithDetails();
      if (bookingsResult.success) {
        // The API response is wrapped, so we need to access bookingsResult.data.data
        const actualBookingsData = bookingsResult.data.data || bookingsResult.data;
        setUserBookings(Array.isArray(actualBookingsData) ? actualBookingsData : []);
      }

      // Get user's payments (will need to update payment service for JWT)
      const paymentsResult = await paymentService.getMyPayments();
      if (paymentsResult.success) {
        // Handle potential double-wrapping for payments too
        const actualPaymentsData = paymentsResult.data.data || paymentsResult.data;
        setUserPayments(Array.isArray(actualPaymentsData) ? actualPaymentsData : []);
      }

      // Get all packages for booking
      const packagesResult = await packageService.getAllPackages();
      if (packagesResult.success) {
        // Handle potential double-wrapping for packages too
        const actualPackagesData = packagesResult.data.data || packagesResult.data;
        setPackages(Array.isArray(actualPackagesData) ? actualPackagesData : []);
      }
    } catch (error) {
      console.error('Failed to load user data:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const handlePackageSelect = (pkg) => {
    setSelectedPackage(pkg);
    setShowBookingModal(true);
  };

  const handleBookingSubmit = async (bookingData) => {
    try {
      // Validate dates
      if (!bookingData.startDate || !bookingData.endDate) {
        toast.error('Please select both start and end dates');
        return;
      }

      // Validate date format and values
      const startDate = new Date(bookingData.startDate);
      const endDate = new Date(bookingData.endDate);
      const today = new Date();

      if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
        toast.error('Invalid date format');
        return;
      }

      if (startDate <= today) {
        toast.error('Start date must be in the future');
        return;
      }

      if (endDate <= startDate) {
        toast.error('End date must be after start date');
        return;
      }

      // Create new booking using JWT-based API call
      const newBookingData = {
        // userId will be extracted from JWT token by backend
        packageId: selectedPackage.packageId || selectedPackage.PackageID,
        startDate: bookingData.startDate,
        endDate: bookingData.endDate,
        status: 'pending'
        // paymentId will be set automatically when payment is processed
      };

      console.log('📅 Booking data being sent:', newBookingData);

      const result = await bookingService.createBooking(newBookingData);
      if (result.success) {
        // ✅ NEW: Store the actual booking data (handle double-wrapped response)
        const actualBookingData = result.data.data || result.data;
        setCurrentBooking(actualBookingData);
        console.log('✅ Booking stored in state:', actualBookingData);
        
        // Update the bookings list for UI
        setUserBookings([...userBookings, { booking: actualBookingData, package: selectedPackage }]);
        
        // Proceed to payment
        setShowBookingModal(false);
        setShowPaymentModal(true);
        toast.success('Booking created successfully!');
      } else {
        console.error('❌ Booking creation failed:', result);
        toast.error('Failed to create booking: ' + (result.error || 'Unknown error'));
      }
    } catch (error) {
      console.error('Failed to create booking:', error);
      toast.error('Failed to create booking');
    }
  };

  const handlePaymentSubmit = async (e) => {
    e.preventDefault();
    
    const cardNumber = document.getElementById('cardNumber').value;
    const expiryDate = document.getElementById('expiryDate').value;
    const cvv = document.getElementById('cvv').value;
    const cardholderName = document.getElementById('cardholderName').value;

    if (!cardNumber || !expiryDate || !cvv || !cardholderName) {
      toast.error('Please fill in all payment fields');
      return;
    }

    // Validate card number format
    if (!/^\d{4}\s\d{4}\s\d{4}\s\d{4}$/.test(cardNumber)) {
      toast.error('Please enter a valid card number in format: XXXX XXXX XXXX XXXX');
      return;
    }

    // Validate expiry date format
    if (!/^\d{2}\/\d{2}$/.test(expiryDate)) {
      toast.error('Please enter expiry date in format: MM/YY');
      return;
    }

    // Validate CVV
    if (!/^\d{3,4}$/.test(cvv)) {
      toast.error('Please enter a valid CVV (3-4 digits)');
      return;
    }

    // Validate cardholder name
    if (!/^[a-zA-Z\s]{2,50}$/.test(cardholderName)) {
      toast.error('Please enter a valid cardholder name (2-50 characters, letters and spaces only)');
      return;
    }

    // Check if card is expired
    const [month, year] = expiryDate.split('/');
    const expiry = new Date(2000 + parseInt(year), parseInt(month) - 1);
    const now = new Date();
    
    if (expiry < now) {
      toast.error('Card has expired. Please use a valid card.');
      return;
    }

    try {
      // Create payment using JWT-based payment service
      const totalAmount = (selectedPackage.price || selectedPackage.Price) + (selectedInsurance ? selectedInsurance.price : 0);
      
      console.log('💰 Payment processing data:', {
        selectedPackage,
        selectedInsurance,
        totalAmount,
        currentBooking
      });
      
      // ✅ NEW: Use the stored booking instead of trying to find it in the array
      if (!currentBooking) {
        toast.error('No booking found. Please create a booking first.');
        return;
      }
      
      // Get booking ID from the stored booking
      const bookingId = currentBooking.bookingId || currentBooking.BookingID;
      
      if (!bookingId) {
        toast.error('Invalid booking data. Please try creating the booking again.');
        return;
      }
      
      const paymentData = {
        // userId will be extracted from JWT token by backend
        bookingId: bookingId,
        amount: totalAmount,
        paymentMethod: 'CREDIT_CARD',
        cardLastFour: cardNumber.slice(-4).replace(/\s/g, ''),
        description: `Payment for ${selectedPackage.title || selectedPackage.Title}`
      };

      console.log('💳 Payment data being sent:', paymentData);

      const paymentResult = await paymentService.processPayment(paymentData);
      
      if (paymentResult.success) {
        // Update booking status to confirmed
        await bookingService.confirmBooking(bookingId);
        
        toast.success('Payment processed successfully!');
        setShowPaymentModal(false);
        
        // ✅ NEW: Clear the current booking since it's completed
        setCurrentBooking(null);
        
        // Reload user data to get updated information
        await loadUserData();
      } else {
        toast.error('Payment processing failed. Please try again.');
      }
    } catch (error) {
      console.error('Payment processing error:', error);
      toast.error('Payment processing failed. Please try again.');
    }
  };

  // ✅ NEW: Cleanup functions for modal closing
  const handleCloseBookingModal = () => {
    setShowBookingModal(false);
    setCurrentBooking(null); // Clear stored booking if user cancels
  };

  const handleClosePaymentModal = () => {
    setShowPaymentModal(false);
    setCurrentBooking(null); // Clear stored booking if user cancels payment
  };

  const canCancelBooking = (booking) => {
    const startDate = new Date(booking.StartDate);
    const today = new Date();
    const daysDifference = Math.ceil((startDate - today) / (1000 * 60 * 60 * 24));
    return daysDifference > 7;
  };

  const handleCancelBooking = (bookingId) => {
    // In a real app, this would be sent to the backend
    if (Array.isArray(userBookings)) {
      setUserBookings(userBookings.map(booking => 
        booking.BookingID === bookingId 
          ? { ...booking, Status: 'cancelled' }
          : booking
      ));
    }
  };

  const getPaymentById = (paymentId) => {
    return userPayments.find(payment => payment.paymentId === paymentId);
  };

  const getPackageById = (packageId) => {
    return packages.find(pkg => pkg.packageId === packageId);
  };

  // Helper functions for payment input formatting
  const formatCardNumber = (input) => {
    let value = input.value.replace(/\s/g, '').replace(/\D/g, '');
    value = value.replace(/(\d{4})/g, '$1 ').trim();
    input.value = value;
  };

  const formatExpiryDate = (input) => {
    let value = input.value.replace(/\D/g, '');
    if (value.length >= 2) {
      value = value.substring(0, 2) + '/' + value.substring(2, 4);
    }
    input.value = value;
  };

  const formatCVV = (input) => {
    input.value = input.value.replace(/\D/g, '').substring(0, 4);
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <div className="header-content">
            <div>
              <h1>User Dashboard</h1>
              <p>Welcome back, {currentUser.Name}! Manage your bookings and preferences</p>
            </div>
            <div className="header-actions">
              <Link to="/profile" className="btn btn-secondary">
                👤 Profile
              </Link>
              <Link to="/packages" className="btn btn-primary">
                📦 Browse Packages
              </Link>
            </div>
          </div>
        </div>

        {/* Available Packages */}
        <section className="dashboard-section">
          <h2>Available Travel Packages</h2>
          <div className="packages-grid">
            {packages && packages.length > 0 ? packages.map((pkg) => (
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
                  <button 
                    onClick={() => handlePackageSelect(pkg)}
                    className="btn btn-primary w-100"
                  >
                    Book Package
                  </button>
                </div>
              </div>
            )) : (
              <p>No packages available at the moment.</p>
            )}
          </div>
        </section>

        {/* User Bookings */}
        <section className="dashboard-section">
          <div className="section-header">
            <h2>Your Bookings</h2>
            <div className="booking-stats">
              <span className="stat-item">
                <strong>{userBookings.length}</strong> Total Bookings
              </span>
            </div>
          </div>
          
          {userBookings.length === 0 ? (
            <div className="no-bookings">
              <div className="empty-state">
                <div className="empty-icon">🏖️</div>
                <h3>No bookings yet</h3>
                <p>Start planning your next adventure by booking a travel package!</p>
                <button className="btn btn-primary" onClick={() => document.querySelector('.packages-grid').scrollIntoView({ behavior: 'smooth' })}>
                  Browse Packages
                </button>
              </div>
            </div>
          ) : (
            <div className="bookings-container">
              <div className="bookings-grid">
                {userBookings.map((booking) => {
                  // Handle both nested booking structure and direct booking data
                  const bookingData = booking.booking || booking;
                  const packageData = booking.package || getPackageById(bookingData.packageId || bookingData.PackageID);
                  const paymentInfo = getPaymentById(bookingData.paymentId || bookingData.PaymentID);
                  
                  // Calculate trip duration
                  const startDate = new Date(bookingData.startDate || bookingData.StartDate);
                  const endDate = new Date(bookingData.endDate || bookingData.EndDate);
                  const duration = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
                  
                  // Get status color
                  const getStatusColor = (status) => {
                    switch(status?.toLowerCase()) {
                      case 'confirmed': return '#28a745';
                      case 'pending': return '#ffc107';
                      case 'cancelled': return '#dc3545';
                      case 'completed': return '#17a2b8';
                      default: return '#6c757d';
                    }
                  };
                  
                  return (
                    <div key={bookingData.bookingId || bookingData.BookingID || `booking-${Date.now()}-${Math.random()}`} className="booking-card">
                      <div className="booking-header">
                        <div className="booking-info">
                          <h4>{packageData?.title || packageData?.Title || 'Package Information Unavailable'}</h4>
                          <div className="booking-id">Booking ID: #{bookingData.bookingId || bookingData.BookingID}</div>
                        </div>
                        <div className="booking-status">
                          <span 
                            className="status-badge" 
                            style={{ backgroundColor: getStatusColor(bookingData.status || bookingData.Status) }}
                          >
                            {(bookingData.status || bookingData.Status)?.toUpperCase()}
                          </span>
                        </div>
                      </div>
                      
                      <div className="booking-details">
                        <div className="detail-row">
                          <div className="detail-item">
                            <span className="detail-label">📅 Check-in</span>
                            <span className="detail-value">{startDate.toLocaleDateString('en-US', { 
                              weekday: 'short', 
                              month: 'short', 
                              day: 'numeric' 
                            })}</span>
                          </div>
                          <div className="detail-item">
                            <span className="detail-label">📅 Check-out</span>
                            <span className="detail-value">{endDate.toLocaleDateString('en-US', { 
                              weekday: 'short', 
                              month: 'short', 
                              day: 'numeric' 
                            })}</span>
                          </div>
                        </div>
                        
                        <div className="detail-row">
                          <div className="detail-item">
                            <span className="detail-label">⏰ Duration</span>
                            <span className="detail-value">{duration} days</span>
                          </div>
                          <div className="detail-item">
                            <span className="detail-label">💰 Amount</span>
                            <span className="detail-value">
                              ${packageData?.price || packageData?.Price || 'N/A'}
                            </span>
                          </div>
                        </div>
                        
                        <div className="detail-row">
                          <div className="detail-item full-width">
                            <span className="detail-label">💳 Payment Status</span>
                            <span className="detail-value">
                              {paymentInfo ? (
                                <span className={`payment-badge payment-${(paymentInfo.status || paymentInfo.Status)?.toLowerCase()}`}>
                                  {(paymentInfo.status || paymentInfo.Status)?.toUpperCase()} - ${paymentInfo.amount || paymentInfo.Amount}
                                </span>
                              ) : (
                                <span className="payment-badge payment-pending">PAYMENT PENDING</span>
                              )}
                            </span>
                          </div>
                        </div>
                        
                        {packageData?.includedServices || packageData?.IncludedServices ? (
                          <div className="detail-row">
                            <div className="detail-item full-width">
                              <span className="detail-label">🎯 Services</span>
                              <span className="detail-value services-list">
                                {(packageData.includedServices || packageData.IncludedServices).split(', ').map((service, index) => (
                                  <span key={index} className="service-tag">{service.trim()}</span>
                                ))}
                              </span>
                            </div>
                          </div>
                        ) : null}
                      </div>
                      
                                             <div className="booking-actions">
                         {canCancelBooking(bookingData) && (bookingData.status || bookingData.Status)?.toLowerCase() !== 'cancelled' && (
                           <button
                             onClick={() => handleCancelBooking(bookingData.bookingId || bookingData.BookingID)}
                             className="btn btn-outline-danger btn-sm"
                           >
                             Cancel Booking
                           </button>
                         )}
                       </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </section>

        {/* Booking Modal */}
        {showBookingModal && selectedPackage && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h3>Book Package: {selectedPackage.title || selectedPackage.Title}</h3>
                <button 
                  onClick={handleCloseBookingModal}
                  className="modal-close"
                >
                  ×
                </button>
              </div>
              <div className="modal-body">
                <div className="package-summary">
                  <p><strong>Price:</strong> ${selectedPackage.price || selectedPackage.Price}</p>
                  <p><strong>Duration:</strong> {selectedPackage.duration || selectedPackage.Duration}</p>
                  <p><strong>Services:</strong> {selectedPackage.includedServices || selectedPackage.IncludedServices}</p>
                </div>
                
                <div className="insurance-selection">
                  <h4>Select Insurance (Optional)</h4>
                  {insuranceTypes.map((insurance) => (
                    <label key={insurance.id} className="insurance-option">
                      <input
                        type="radio"
                        name="insurance"
                        value={insurance.id}
                        onChange={() => setSelectedInsurance(insurance)}
                      />
                      <div className="insurance-details">
                        <strong>{insurance.name}</strong>
                        <p>{insurance.description}</p>
                        <span className="insurance-price">${insurance.price}</span>
                      </div>
                    </label>
                  ))}
                </div>

                <div className="date-selection">
                  <div className="form-group">
                    <label>Start Date</label>
                    <input type="date" id="startDate" className="form-control" />
                  </div>
                  <div className="form-group">
                    <label>End Date</label>
                    <input type="date" id="endDate" className="form-control" />
                  </div>
                </div>

                <div className="total-price">
                  <strong>Total Price: </strong>
                  ${(selectedPackage.price || selectedPackage.Price) + (selectedInsurance ? selectedInsurance.price : 0)}
                </div>
              </div>
              <div className="modal-footer">
                <button 
                  onClick={handleCloseBookingModal}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button 
                  onClick={() => handleBookingSubmit({
                    startDate: document.getElementById('startDate').value,
                    endDate: document.getElementById('endDate').value
                  })}
                  className="btn btn-primary"
                >
                  Confirm Booking
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Payment Modal */}
        {showPaymentModal && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h3>Complete Payment</h3>
                <button 
                  onClick={handleClosePaymentModal}
                  className="modal-close"
                >
                  ×
                </button>
              </div>
              <div className="modal-body">
                <div className="payment-summary">
                  <p><strong>Package:</strong> {selectedPackage?.title || selectedPackage?.Title}</p>
                  <p><strong>Package Price:</strong> ${selectedPackage?.price || selectedPackage?.Price}</p>
                  {selectedInsurance && (
                    <p><strong>Insurance:</strong> {selectedInsurance.name} - ${selectedInsurance.price}</p>
                  )}
                  <p><strong>Total Amount:</strong> ${(selectedPackage?.price || selectedPackage?.Price || 0) + (selectedInsurance ? selectedInsurance.price : 0)}</p>
                </div>

                <div className="payment-methods">
                  <h4>Select Payment Method</h4>
                  <label className="payment-option">
                    <input type="radio" name="paymentMethod" value="credit_card" defaultChecked />
                    Credit Card
                  </label>
                  <label className="payment-option">
                    <input type="radio" name="paymentMethod" value="paypal" />
                    PayPal
                  </label>
                </div>

                <div className="payment-form">
                  <div className="form-group">
                    <label>Cardholder Name</label>
                    <input 
                      type="text" 
                      id="cardholderName"
                      placeholder="John Doe" 
                      className="form-control" 
                    />
                  </div>
                  <div className="form-group">
                    <label>Card Number</label>
                    <input 
                      type="text" 
                      id="cardNumber"
                      placeholder="1234 5678 9012 3456" 
                      className="form-control"
                      onInput={(e) => formatCardNumber(e.target)}
                      maxLength="19"
                    />
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Expiry Date</label>
                      <input 
                        type="text" 
                        id="expiryDate"
                        placeholder="MM/YY" 
                        className="form-control"
                        onInput={(e) => formatExpiryDate(e.target)}
                        maxLength="5"
                      />
                    </div>
                    <div className="form-group">
                      <label>CVV</label>
                      <input 
                        type="text" 
                        id="cvv"
                        placeholder="123" 
                        className="form-control"
                        onInput={(e) => formatCVV(e.target)}
                        maxLength="4"
                      />
                    </div>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button 
                  onClick={handleClosePaymentModal}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button 
                  onClick={handlePaymentSubmit}
                  className="btn btn-primary"
                >
                  Complete Payment
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserDashboard; 