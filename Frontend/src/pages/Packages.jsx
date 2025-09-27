import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { packageService, bookingService, paymentService } from '../services';
import { insuranceTypes } from '../constants/insuranceTypes';
import { toast } from 'react-hot-toast';
import '../styles/Packages.css';

const Packages = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [selectedPackage, setSelectedPackage] = useState(null);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedInsurance, setSelectedInsurance] = useState(null);
  const [filterDuration, setFilterDuration] = useState('all');
  const [sortBy, setSortBy] = useState('name');
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);

  // Load packages from API
  useEffect(() => {
    loadPackages();
  }, []);

  const loadPackages = async () => {
    setLoading(true);
    try {
      const result = await packageService.getAllPackages();
      if (result.success) {
        setPackages(result.data);
      } else {
        toast.error('Failed to load packages');
      }
    } catch (error) {
      console.error('Failed to load packages:', error);
      toast.error('Failed to load packages');
    } finally {
      setLoading(false);
    }
  };


  // Filter and sort packages
  const filteredAndSortedPackages = packages
    .filter(pkg => {
      if (filterDuration === 'all') return true;
      const duration = pkg.duration || pkg.Duration || '';
      const days = parseInt(duration.split(' ')[0]);
      switch (filterDuration) {
        case 'short': return days <= 3;
        case 'medium': return days > 3 && days <= 7;
        case 'long': return days > 7;
        default: return true;
      }
    })
    .sort((a, b) => {
      switch (sortBy) {
        case 'name':
          return (a.title || a.Title || '').localeCompare(b.title || b.Title || '');
        case 'price-low':
          return (a.price || a.Price || 0) - (b.price || b.Price || 0);
        case 'price-high':
          return (b.price || b.Price || 0) - (a.price || a.Price || 0);
        case 'duration':
          const aDays = parseInt((a.duration || a.Duration || '').split(' ')[0]);
          const bDays = parseInt((b.duration || b.Duration || '').split(' ')[0]);
          return aDays - bDays;
        default:
          return 0;
      }
    });

  const handlePackageSelect = (pkg) => {
    if (!isAuthenticated) {
      toast.error('Please sign in to book packages');
      return;
    }
    setSelectedPackage(pkg);
    setShowBookingModal(true);
  };

  const handleBookingSubmit = async (bookingData) => {
    // Validate dates
    if (!bookingData.startDate || !bookingData.endDate) {
      toast.error('Please select both start and end dates');
      return;
    }

    const startDate = new Date(bookingData.startDate);
    const endDate = new Date(bookingData.endDate);
    const today = new Date();

    // Check if start date is in the future
    if (startDate <= today) {
      toast.error('Start date must be in the future');
      return;
    }

    // Check if end date is after start date
    if (endDate <= startDate) {
      toast.error('End date must be after start date');
      return;
    }

    try {
      // Create booking using JWT-based API call
      const newBookingData = {
        // userId will be extracted from JWT token by backend
        packageId: selectedPackage.packageId || selectedPackage.PackageID,
        startDate: bookingData.startDate,
        endDate: bookingData.endDate,
        status: 'pending'
        // paymentId will be set automatically when payment is processed
      };

      const result = await bookingService.createBooking(newBookingData);
      if (result.success) {
        // Store booking data for payment
        setSelectedPackage(prev => ({ ...prev, bookingId: result.data.bookingId }));
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

  const getAgentName = (agentId) => {
    // Return placeholder for agent name
    // In a real app, you might want to fetch agent details separately
    return `Agent ${agentId}`;
  };

  const getDurationDays = (duration) => {
    return parseInt(duration.split(' ')[0]);
  };

  const handleStartDateChange = (startDate) => {
    if (startDate && selectedPackage) {
      const durationDays = getDurationDays(selectedPackage.Duration);
      const endDate = new Date(startDate);
      endDate.setDate(endDate.getDate() + durationDays);
      
      // Format end date for input field (YYYY-MM-DD)
      const endDateFormatted = endDate.toISOString().split('T')[0];
      document.getElementById('endDate').value = endDateFormatted;
    }
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

  const handlePaymentSubmit = async (paymentData) => {
    // Get form values
    const cardNumber = document.getElementById('cardNumber').value;
    const expiryDate = document.getElementById('expiryDate').value;
    const cvv = document.getElementById('cvv').value;
    const cardholderName = document.getElementById('cardholderName').value;

    // Validation using regex
    const cardNumberRegex = /^\d{4}\s\d{4}\s\d{4}\s\d{4}$/;
    const expiryDateRegex = /^(0[1-9]|1[0-2])\/([0-9]{2})$/;
    const cvvRegex = /^\d{3,4}$/;
    const cardholderNameRegex = /^[a-zA-Z\s]{2,50}$/;

    // Check if all fields are filled
    if (!cardNumber || !expiryDate || !cvv || !cardholderName) {
      toast.error('Please fill in all payment fields');
      return;
    }

    // Validate card number format (XXXX XXXX XXXX XXXX)
    if (!cardNumberRegex.test(cardNumber)) {
      toast.error('Please enter a valid card number in format: XXXX XXXX XXXX XXXX');
      return;
    }

    // Validate expiry date format (MM/YY)
    if (!expiryDateRegex.test(expiryDate)) {
      toast.error('Please enter expiry date in format: MM/YY');
      return;
    }

    // Validate CVV (3-4 digits)
    if (!cvvRegex.test(cvv)) {
      toast.error('Please enter a valid CVV (3-4 digits)');
      return;
    }

    // Validate cardholder name
    if (!cardholderNameRegex.test(cardholderName)) {
      toast.error('Please enter a valid cardholder name (2-50 characters, letters and spaces only)');
      return;
    }

    // Check if expiry date is not in the past
    const [month, year] = expiryDate.split('/');
    const expiry = new Date(2000 + parseInt(year), parseInt(month) - 1);
    const today = new Date();
    if (expiry < today) {
      toast.error('Card has expired. Please use a valid card.');
      return;
    }

    try {
      // Create payment using JWT-based API call
      const totalAmount = (selectedPackage.price || selectedPackage.Price) + (selectedInsurance ? selectedInsurance.price : 0);
      
      const paymentData = {
        // userId will be extracted from JWT token by backend
        bookingId: selectedPackage.bookingId, // Set during booking creation
        amount: totalAmount,
        paymentMethod: 'CREDIT_CARD',
        cardLastFour: cardNumber.slice(-4).replace(/\s/g, ''),
        description: `Payment for ${selectedPackage.title || selectedPackage.Title}`
      };

      const paymentResult = await paymentService.processPayment(paymentData);
      
      if (paymentResult.success) {
        // Update booking status to confirmed
        await bookingService.confirmBooking(selectedPackage.bookingId);
        
        // Show success message
        toast.success(`🎉 Payment successful! Your booking has been confirmed. Redirecting to your dashboard...`);
        
        setShowPaymentModal(false);
        setSelectedPackage(null);
        setSelectedInsurance(null);
        
        // Redirect to user dashboard using React Router
        navigate('/user-dashboard');
      } else {
        toast.error('Payment processing failed. Please try again.');
      }
    } catch (error) {
      toast.error('❌ Payment failed. Please try again or contact support.');
      console.error('Payment error:', error);
    }
  };

  if (loading) {
    return (
      <div className="packages-page">
        <div className="container">
          <div className="loading-spinner">
            <div className="spinner"></div>
            <p>Loading packages...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="packages-page">
      <div className="container">
        {/* Header Section */}
        <div className="packages-header">
          <h1>Travel Packages</h1>
          <p>Discover amazing destinations and experiences curated by our travel experts</p>
        </div>

        {/* Filters and Sorting */}
        <div className="packages-controls">
          <div className="filter-section">
            <label htmlFor="duration-filter">Duration:</label>
            <select
              id="duration-filter"
              value={filterDuration}
              onChange={(e) => setFilterDuration(e.target.value)}
              className="filter-select"
            >
              <option value="all">All Durations</option>
              <option value="short">Short (1-3 days)</option>
              <option value="medium">Medium (4-7 days)</option>
              <option value="long">Long (8+ days)</option>
            </select>
          </div>

          <div className="sort-section">
            <label htmlFor="sort-by">Sort by:</label>
            <select
              id="sort-by"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="filter-select"
            >
              <option value="name">Name (A-Z)</option>
              <option value="price-low">Price (Low to High)</option>
              <option value="price-high">Price (High to Low)</option>
              <option value="duration">Duration</option>
            </select>
          </div>
        </div>

        {/* Packages Grid */}
        <div className="packages-container">
          {filteredAndSortedPackages.length === 0 ? (
            <div className="no-packages">
              <div className="no-packages-icon">✈️</div>
              <h3>No packages found</h3>
              <p>Try adjusting your filters to see more packages.</p>
            </div>
          ) : (
            <div className="packages-grid">
              {filteredAndSortedPackages.map((pkg) => (
                <div key={pkg.PackageID} className="package-card">
                  <div className="package-image">
                    <img 
                      src={pkg.Image} 
                      alt={pkg.Title}
                      onError={(e) => {
                        e.target.style.display = 'none';
                        e.target.parentElement.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
                      }}
                    />
                    <div className="package-badge">
                      <span className="duration-badge">{pkg.Duration}</span>
                      <span className="price-badge">${pkg.Price}</span>
                    </div>
                  </div>
                  
                  <div className="package-content">
                    <div className="package-header">
                      <h3>{pkg.Title}</h3>
                      <div className="package-meta">
                        <span className="agent-name">by {getAgentName(pkg.AgentID)}</span>
                        <span className="package-rating">⭐⭐⭐⭐⭐ (4.8)</span>
                      </div>
                    </div>
                    
                    <p className="package-description">{pkg.Description}</p>
                    
                    <div className="package-features">
                      <h4>What's Included:</h4>
                      <ul>
                        {pkg.IncludedServices.split(', ').map((service, index) => (
                          <li key={index}>✓ {service}</li>
                        ))}
                      </ul>
                    </div>
                    
                    <div className="package-footer">
                      <button
                        onClick={() => handlePackageSelect(pkg)}
                        className="btn btn-primary"
                      >
                        {isAuthenticated ? 'Book Now' : 'Sign In to Book'}
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>



        {/* Package Statistics */}
        <div className="packages-stats">
          <div className="stat-item">
            <span className="stat-number">{travelPackages.length}</span>
            <span className="stat-label">Total Packages</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">
              ${Math.min(...travelPackages.map(p => p.Price)).toFixed(0)}
            </span>
            <span className="stat-label">Starting From</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">
              {Math.max(...travelPackages.map(p => getDurationDays(p.Duration)))}
            </span>
            <span className="stat-label">Longest Trip</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">
              {users.filter(u => u.Role === 'agent').length}
            </span>
            <span className="stat-label">Travel Experts</span>
          </div>
        </div>

        {/* Booking Modal */}
        {showBookingModal && selectedPackage && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h3>Book Package: {selectedPackage.Title}</h3>
                <button 
                  onClick={() => setShowBookingModal(false)}
                  className="modal-close"
                >
                  ×
                </button>
              </div>
              <div className="modal-body">
                <div className="package-summary">
                  <p><strong>Price:</strong> ${selectedPackage.Price}</p>
                  <p><strong>Duration:</strong> {selectedPackage.Duration}</p>
                  <p><strong>Agent:</strong> {getAgentName(selectedPackage.AgentID)}</p>
                  <p><strong>Services:</strong> {selectedPackage.IncludedServices}</p>
                </div>
                
                <div className="date-selection">
                  <div className="form-group">
                    <label>Start Date</label>
                    <input 
                      type="date" 
                      id="startDate" 
                      className="form-control"
                      min={new Date().toISOString().split('T')[0]}
                      onChange={(e) => handleStartDateChange(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label>End Date</label>
                    <input 
                      type="date" 
                      id="endDate" 
                      className="form-control"
                      min={new Date().toISOString().split('T')[0]}
                    />
                  </div>
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

                <div className="total-price">
                  <strong>Total Price: </strong>
                  ${selectedPackage.Price + (selectedInsurance ? selectedInsurance.price : 0)}
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
        {showPaymentModal && selectedPackage && (
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
                  <p><strong>Package:</strong> {selectedPackage.Title}</p>
                  <p><strong>Package Price:</strong> ${selectedPackage.Price}</p>
                  {selectedInsurance && (
                    <p><strong>Insurance:</strong> {selectedInsurance.name} - ${selectedInsurance.price}</p>
                  )}
                  <p><strong>Total Amount:</strong> ${selectedPackage.Price + (selectedInsurance ? selectedInsurance.price : 0)}</p>
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
                  onClick={() => handlePaymentSubmit({
                    paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value
                  })}
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

export default Packages; 