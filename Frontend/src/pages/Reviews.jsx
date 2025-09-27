import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { reviewService, packageService, userService } from '../services';
import { toast } from 'react-hot-toast';
import '../styles/Reviews.css';

const Reviews = () => {
  const { currentUser, isAuthenticated } = useAuth();
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [allReviews, setAllReviews] = useState([]);
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newReview, setNewReview] = useState({
    rating: 5,
    comment: '',
    packageId: ''
  });

  useEffect(() => {
    loadData();
  }, []);



  const loadData = async () => {
    setLoading(true);
    try {
      const [reviewsResult, packagesResult] = await Promise.all([
        reviewService.getAllReviews(),
        packageService.getAllPackages()
      ]);
      
      if (reviewsResult.success) {
        // Handle potential double-wrapping of API responses
        const actualReviewsData = reviewsResult.data?.data || reviewsResult.data;
        setAllReviews(Array.isArray(actualReviewsData) ? actualReviewsData : []);
      }
      
      if (packagesResult.success) {
        // Handle potential double-wrapping of API responses
        const actualPackagesData = packagesResult.data?.data || packagesResult.data;
        setPackages(Array.isArray(actualPackagesData) ? actualPackagesData : []);
      } else {
        toast.error('Failed to load packages for review form');
      }
    } catch (error) {
      console.error('Failed to load data:', error);
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    
    // Enhanced validation
    if (!newReview.comment.trim() || !newReview.packageId) {
      toast.error('Please fill in all fields');
      return;
    }
    
    if (newReview.rating < 1 || newReview.rating > 5) {
      toast.error('Rating must be between 1 and 5');
      return;
    }
    
    if (newReview.comment.trim().length === 0) {
      toast.error('Please provide a comment');
      return;
    }

    try {
      // Create new review using direct API call (UserID will be extracted from JWT)
      const reviewData = {
        PackageID: parseInt(newReview.packageId),
        Rating: newReview.rating,
        Comment: newReview.comment
      };
      
      const result = await reviewService.createReview(reviewData);
      
      if (result.success) {
        setAllReviews(Array.isArray(allReviews) ? [...allReviews, result.data] : [result.data]);
        setNewReview({ rating: 5, comment: '', packageId: '' });
        setShowReviewForm(false);
        toast.success('Review submitted successfully');
      } else {
        console.error('❌ Review submission failed:', result);
        const errorMsg = result.error || result.message || 'Failed to submit review';
        toast.error(errorMsg);
      }
    } catch (error) {
      console.error('❌ Exception during review submission:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Failed to submit review';
      toast.error(errorMsg);
    }
  };

  const handleReplySubmit = async (reviewId, reply) => {
    try {
      // Update review with agent reply using direct API call
      const result = await reviewService.addAgentReply(reviewId, { agentReply: reply });
      if (result.success) {
        // Update local state
        setAllReviews(Array.isArray(allReviews) ? allReviews.map(review => 
          (review.reviewID || review.ReviewID) === reviewId 
            ? { ...review, agentReply: reply }
            : review
        ) : []);
        toast.success('Reply added successfully');
      } else {
        toast.error('Failed to add reply');
      }
    } catch (error) {
      console.error('Failed to add reply:', error);
      toast.error('Failed to add reply');
    }
  };

  const getUserById = (userId) => {
    // Since we're not loading all users, return placeholder
    return { Name: `User ${userId}`, UserID: userId };
  };

  const getPackageById = (packageId) => {
    return Array.isArray(packages) ? packages.find(pkg => (pkg.packageId || pkg.PackageID) === packageId) : null;
  };

  const renderStars = (rating) => {
    return '⭐'.repeat(rating) + '☆'.repeat(5 - rating);
  };

  // Helper function to get user role (handles both camelCase and PascalCase)
  const getUserRole = () => {
    return currentUser?.role || currentUser?.Role;
  };

  // Helper function to get user ID (handles both camelCase and PascalCase)
  const getUserId = () => {
    return currentUser?.userId || currentUser?.UserID;
  };

  const canReplyToReview = (review) => {
    if (getUserRole() !== 'agent') return false;
    
    const packageInfo = getPackageById(review.PackageID);
    return packageInfo && packageInfo.AgentID === getUserId();
  };

  return (
    <div className="reviews-page">
      <div className="container">
        <div className="reviews-header">
          <h1>Customer Reviews</h1>
          <p>Read what our travelers have to say about their experiences</p>
          

          {isAuthenticated && getUserRole() === 'customer' && (
            <button 
              onClick={() => setShowReviewForm(true)}
              className="btn btn-primary write-review-btn"
            >
              ✍️ Write a Review
            </button>
          )}
          
          {/* Show alternative message for non-customers */}
          {isAuthenticated && getUserRole() !== 'customer' && getUserRole() && (
            <div className="role-info">
              <p style={{ color: '#666', fontStyle: 'italic' }}>
                Only customers can write reviews. You are logged in as: <strong>{getUserRole()}</strong>
              </p>
            </div>
          )}
          
          {/* Show login prompt for unauthenticated users */}
          {!isAuthenticated && (
            <div className="login-prompt">
              <p style={{ color: '#666', fontStyle: 'italic' }}>
                <a href="/signin" style={{ color: '#007bff' }}>Sign in</a> as a customer to write reviews
              </p>
            </div>
          )}
        </div>

        {/* Review Form Modal */}
        {showReviewForm && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h3>Write Your Review</h3>
                <button 
                  onClick={() => setShowReviewForm(false)}
                  className="modal-close"
                >
                  ×
                </button>
              </div>
              <div className="modal-body">
                <form onSubmit={handleReviewSubmit}>
                  <div className="form-group">
                    <label>Select Package</label>
                    <select
                      value={newReview.packageId}
                      onChange={(e) => setNewReview({...newReview, packageId: e.target.value})}
                      className="form-control"
                      required
                    >
                      <option value="">Choose a package...</option>
                      {Array.isArray(packages) ? packages.map((pkg) => (
                        <option key={pkg.packageId || pkg.PackageID} value={pkg.packageId || pkg.PackageID}>
                          {pkg.title || pkg.Title}
                        </option>
                      )) : (
                        <option value="">No packages available</option>
                      )}

                    </select>
                  </div>

                  <div className="form-group">
                    <label>Rating</label>
                    <div className="rating-input">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          className={`star-btn ${newReview.rating >= star ? 'active' : ''}`}
                          onClick={() => setNewReview({...newReview, rating: star})}
                        >
                          ⭐
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Your Review</label>
                    <textarea
                      value={newReview.comment}
                      onChange={(e) => setNewReview({...newReview, comment: e.target.value})}
                      className="form-control"
                      rows="4"
                      placeholder="Share your experience..."
                      required
                    />
                  </div>

                  <div className="modal-footer">
                    <button 
                      type="button"
                      onClick={() => setShowReviewForm(false)}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      Submit Review
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* Reviews List */}
        <div className="reviews-container">
          {!Array.isArray(allReviews) || allReviews.length === 0 ? (
            <div className="no-reviews">
              <p>No reviews yet. Be the first to share your experience!</p>
            </div>
          ) : (
            allReviews.map((review) => {
              const user = getUserById(review.UserID);
              const packageInfo = getPackageById(review.PackageID);
              
              return (
                <div key={review.ReviewID} className="review-card">
                  <div className="review-header">
                    <div className="reviewer-info">
                      <h3>{user?.Name}</h3>
                      <span className="review-date">
                        {new Date(review.Timestamp).toLocaleDateString()}
                      </span>
                    </div>
                    <div className="review-rating">
                      {renderStars(review.Rating)}
                      <span className="rating-text">({review.Rating}/5)</span>
                    </div>
                  </div>
                  
                  <div className="package-info">
                    <strong>Package:</strong> {packageInfo?.Title}
                  </div>
                  
                  <div className="review-content">
                    <p>{review.Comment}</p>
                  </div>

                  {/* Agent Reply */}
                  {review.agentReply && (
                    <div className="agent-reply">
                      <div className="reply-header">
                        <strong>Agent Response:</strong>
                        <span className="reply-date">
                          {new Date().toLocaleDateString()}
                        </span>
                      </div>
                      <p>{review.agentReply}</p>
                    </div>
                  )}

                  {/* Reply Form for Agents */}
                  {canReplyToReview(review) && !review.agentReply && (
                    <div className="reply-form">
                      <textarea
                        placeholder="Write your reply..."
                        className="form-control"
                        rows="2"
                        id={`reply-${review.ReviewID}`}
                      />
                      <button
                        onClick={() => {
                          const reply = document.getElementById(`reply-${review.ReviewID}`).value;
                          if (reply.trim()) {
                            handleReplySubmit(review.ReviewID, reply);
                          }
                        }}
                        className="btn btn-secondary btn-sm"
                      >
                        Reply
                      </button>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>

        {/* Review Statistics */}
        {allReviews.length > 0 && (
          <div className="review-stats">
            <h3>Review Summary</h3>
            <div className="stats-grid">
              <div className="stat-item">
                <span className="stat-number">{Array.isArray(allReviews) ? allReviews.length : 0}</span>
                <span className="stat-label">Total Reviews</span>
              </div>
              <div className="stat-item">
                <span className="stat-number">
                  {Array.isArray(allReviews) && allReviews.length > 0 
                    ? (allReviews.reduce((sum, review) => sum + review.Rating, 0) / allReviews.length).toFixed(1)
                    : '0.0'
                  }
                </span>
                <span className="stat-label">Average Rating</span>
              </div>
              <div className="stat-item">
                <span className="stat-number">
                  {Array.isArray(allReviews) ? allReviews.filter(review => review.Rating >= 4).length : 0}
                </span>
                <span className="stat-label">4+ Star Reviews</span>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default Reviews; 