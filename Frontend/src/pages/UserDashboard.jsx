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
  const [userBookings, setUserBookings] = useState([]);
  const [userPayments, setUserPayments] = useState([]);
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUserData();
  }, [currentUser.UserID]);

  const loadUserData = async () => {
    setLoading(true);
    try {
      // Use enhanced booking service to get bookings with package details
      const bookingsResult = await bookingService.getUserBookingsWithDetails(currentUser.UserID);
      if (bookingsResult.success) {
        setUserBookings(bookingsResult.data);
      }

      // Get user's payments
      const paymentsResult = await paymentService.getPaymentsByUserId(currentUser.UserID);
      if (paymentsResult.success) {
        setUserPayments(paymentsResult.data);
      }

      // Get all packages for booking
      const packagesResult = await packageService.getAllPackages();
      if (packagesResult.success) {
        setPackages(packagesResult.data);
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
      // Create new booking using direct API call
      const newBookingData = {
        userId: currentUser.UserID,
        packageId: selectedPackage.packageId || selectedPackage.PackageID,
        startDate: bookingData.startDate,
        endDate: bookingData.endDate,
        status: 'pending',
        paymentId: null
      };

      const result = await bookingService.createBooking(newBookingData);
      if (result.success) {
        setUserBookings([...userBookings, { booking: result.data, package: selectedPackage }]);
        setShowBookingModal(false);
        setShowPaymentModal(true);
        toast.success('Booking created successfully!');
      } else {
        toast.error('Failed to create booking');
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
      // Create payment using enhanced payment service
      const totalAmount = (selectedPackage.price || selectedPackage.Price) + (selectedInsurance ? selectedInsurance.price : 0);
      const latestBooking = userBookings[userBookings.length - 1];
      
      const paymentData = {
        userId: currentUser.UserID,
        bookingId: latestBooking.booking.bookingId,
        amount: totalAmount,
        paymentMethod: 'CREDIT_CARD',
        cardLastFour: cardNumber.slice(-4).replace(/\s/g, ''),
        description: `Payment for ${selectedPackage.title || selectedPackage.Title}`
      };

      const paymentResult = await paymentService.processPayment(paymentData);
      
      if (paymentResult.success) {
        // Update booking status to confirmed
        await bookingService.confirmBooking(latestBooking.booking.bookingId);
        
        toast.success('Payment processed successfully!');
        setShowPaymentModal(false);
        
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

  const canCancelBooking = (booking) => {
    const startDate = new Date(booking.StartDate);
    const today = new Date();
    const daysDifference = Math.ceil((startDate - today) / (1000 * 60 * 60 * 24));
    return daysDifference > 7;
  };

  const handleCancelBooking = (bookingId) => {
    // In a real app, this would be sent to the backend
    setUserBookings(userBookings.map(booking => 
      booking.BookingID === bookingId 
        ? { ...booking, Status: 'cancelled' }
        : booking
    ));
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
            {packages.map((pkg) => (
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
            ))}
          </div>
        </section>

        {/* User Bookings */}
        <section className="dashboard-section">
          <h2>Your Bookings</h2>
          {userBookings.length === 0 ? (
            <div className="no-bookings">
              <p>You haven't made any bookings yet.</p>
            </div>
          ) : (
            <div className="bookings-table">
              <table>
                <thead>
                  <tr>
                    <th>Package</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Status</th>
                    <th>Payment</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {userBookings.map((booking) => {
                    const packageInfo = getPackageById(booking.PackageID);
                    const paymentInfo = getPaymentById(booking.PaymentID);
                    
                    return (
                      <tr key={booking.BookingID}>
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
                          {canCancelBooking(booking) && booking.Status !== 'cancelled' && (
                            <button
                              onClick={() => handleCancelBooking(booking.BookingID)}
                              className="btn btn-danger btn-sm"
                            >
                              Cancel
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
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
                  onClick={() => setShowBookingModal(false)}
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
                  onClick={() => setShowBookingModal(false)}
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
                  onClick={() => setShowPaymentModal(false)}
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
                  onClick={() => setShowPaymentModal(false)}
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