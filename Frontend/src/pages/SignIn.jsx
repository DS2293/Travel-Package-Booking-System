import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-hot-toast';
import '../styles/Auth.css';

const SignIn = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');
  const [isResettingPassword, setIsResettingPassword] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const { register, handleSubmit, formState: { errors } } = useForm();

  // Show success message if redirected from register (using useEffect to prevent re-renders)
  useEffect(() => {
    if (location.state?.message) {
      toast.success(location.state.message);
      // Clear the state to prevent showing it again
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const onSubmit = async (data) => {
    setIsLoading(true);
    
    try {
      console.log('🔐 Attempting login with:', { email: data.email });
      
      const result = await login(data.email, data.password);
      
      console.log('📥 Login result:', result);
      
      if (result.success) {
        // Check if agent is approved (using lowercase field names)
        if (result.user.role === 'agent' && result.user.approval !== 'approved') {
          toast.error('Your agent account is pending approval. Please wait for admin approval before signing in.');
          setIsLoading(false);
          return;
        }

        toast.success('Sign in successful!');
        
        // Set loading to false before navigation
        setIsLoading(false);
        
        // Navigate based on role
        setTimeout(() => {
          switch (result.user.role) {
            case 'admin': 
              navigate('/admin-dashboard'); 
              break;
            case 'agent': 
              navigate('/agent-dashboard'); 
              break;
            case 'customer': 
              navigate('/user-dashboard'); 
              break;
            default: 
              navigate('/user-dashboard');
          }
        }, 1000);
      } else {
        toast.error(result.message || 'Invalid email or password');
        setIsLoading(false);
      }
    } catch (error) {
      console.error('💥 Login error:', error);
      toast.error('An error occurred during sign in');
      setIsLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    if (!forgotPasswordEmail.trim()) {
      toast.error('Please enter your email address');
      return;
    }

    setIsResettingPassword(true);
    
    // Simulate password reset request
    setTimeout(() => {
      toast.success('Password reset instructions have been sent to your email');
      setShowForgotPassword(false);
      setForgotPasswordEmail('');
      setIsResettingPassword(false);
    }, 1500);
  };

  const handleBackToSignIn = () => {
    setShowForgotPassword(false);
    setForgotPasswordEmail('');
  };



  if (showForgotPassword) {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <h2>Forgot Password</h2>
            <p>Enter your email to change your password</p>
          </div>

          <form onSubmit={handleForgotPassword} className="auth-form">
            <div className="form-group">
              <label htmlFor="resetEmail" className="form-label">Email Address</label>
              <input
                type="email"
                id="resetEmail"
                className="form-control"
                placeholder="Enter your email"
                value={forgotPasswordEmail}
                onChange={(e) => setForgotPasswordEmail(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary w-100"
            >
              Continue
            </button>
          </form>

          <div className="auth-footer">
            <button
              type="button"
              className="btn btn-link"
              onClick={handleBackToSignIn}
            >
              ← Back to Sign In
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h2>Welcome Back</h2>
          <p>Sign in to your TravelEase account</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <div className="form-group">
            <label htmlFor="email" className="form-label">Email Address</label>
            <input
              type="email"
              id="email"
              className={`form-control ${errors.email ? 'is-invalid' : ''}`}
              placeholder="Enter your email"
              {...register('email', {
                required: 'Email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address'
                }
              })}
            />
            {errors.email && (
              <div className="invalid-feedback">
                {errors.email.message}
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              type="password"
              id="password"
              className={`form-control ${errors.password ? 'is-invalid' : ''}`}
              placeholder="Enter your password"
              {...register('password', {
                required: 'Password is required',
                minLength: {
                  value: 6,
                  message: 'Password must be at least 6 characters'
                }
              })}
            />
            {errors.password && (
              <div className="invalid-feedback">
                {errors.password.message}
              </div>
            )}
          </div>

          <div className="form-group">
            <button
              type="button"
              className="btn btn-link forgot-password-link"
              onClick={() => setShowForgotPassword(true)}
            >
              Forgot your password?
            </button>
          </div>

          <button
            type="submit"
            className="btn btn-primary w-100"
            disabled={isLoading}
          >
            {isLoading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Don't have an account?{' '}
            <Link to="/register" className="auth-link">
              Sign up here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignIn; 