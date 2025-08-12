"use client"; // This marks the component as a Client Component in Next.js

import React, { useState, useEffect, useCallback } from 'react';

// Import all sub-components from their respective files in the same directory.
// IMPORTANT: Ensure these files exist in 'frontend/src/components/'
// with their EXACT names (including capitalization) and have 'export default ComponentName;'.
// Next.js/TypeScript usually infers the .tsx extension, so it's typically omitted here.
import MessageDisplay from './MessageDisplay';
import AuthenticationView from './AuthenticationView';
import RegistrationView from './RegistrationView';
import ForgotPasswordView from './ForgotPasswordView';
import ResetPasswordView from './ResetPasswordView';
import EmailVerificationView from './EmailVerificationView';
import HomePageView from './HomePageView';
import AdminDashboardView from './AdminDashboardView';
import ProductDetailView from './ProductDetailView';

// =========================================================================
// Main App Component: AccountDashboard
// This component acts as the main container for the entire application.
// It manages global state like authentication and switches between different views.
// =========================================================================
const AccountDashboard = () => {
    // Shared Authentication States
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [authToken, setAuthToken] = useState<string | null>(null);
    const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
    const [userRole, setUserRole] = useState<string>('user'); // Default to 'user'

    // Shared UI Feedback States for Login
    const [loginLoading, setLoginLoading] = useState<boolean>(false);
    const [loginError, setLoginError] = useState<string | null>(null);
    const [loginSuccessMessage, setLoginSuccessMessage] = useState<string | null>(null);

    // State to manage which authentication-related view is active
    const [currentAuthView, setCurrentAuthView] = useState<'login' | 'register' | 'forgotPassword' | 'resetPassword' | 'verifyEmail'>('login');

    // Current Main View State (after successful login/public access)
    // 'productDetail' is now primarily managed by HomePageView for regular users.
    const [currentAppView, setCurrentAppView] = useState<'home' | 'login' | 'admin' | 'register' | 'forgotPassword' | 'resetPassword' | 'verifyEmail' | 'productDetail'>('home');

    // State to hold the ID of the product being viewed in detail for REGULAR USERS
    const [selectedProductId, setSelectedProductId] = useState<number | null>(null);

    // Product States for HomePage (if rendered directly by App)
    const [products, setProducts] = useState<any[]>([]); // Using 'any' for simplicity, define Product interface if available
    const [productsLoading, setProductsLoading] = useState<boolean>(false);
    const [productsError, setProductsError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');

    // Base URL for your Spring Boot API.
    const API_BASE_URL = 'http://localhost:8080';

    /**
     * Helper to get common fetch headers, conditionally including Authorization.
     */
    const getAuthHeaders = useCallback((contentType: string | null = 'application/json'): HeadersInit => {
        const headers: Record<string, string> = {};
        if (contentType) {
            headers['Content-Type'] = contentType;
        }
        if (authToken) {
            headers['Authorization'] = `Bearer ${authToken}`;
        }
        return headers;
    }, [authToken]);

    /**
     * Fetches products for the home page or search.
     * This is used by HomePageView for regular users.
     */
    const fetchProducts = useCallback(async (keyword: string = '') => {
        setProductsLoading(true);
        setProductsError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/products/search?keyword=${encodeURIComponent(keyword)}`);
            const data = await response.json();
            if (response.ok && data.success) {
                setProducts(data.products || []);
            } else {
                setProductsError(data.message || 'Failed to fetch products.');
            }
        } catch (err: any) {
            setProductsError('Network error fetching products. Ensure backend is running.');
        } finally {
            setProductsLoading(false);
        }
    }, [API_BASE_URL]);

    useEffect(() => {
        // Only fetch products if on the home page view and not an admin
        if (currentAppView === 'home' && userRole !== 'admin') {
            fetchProducts(searchTerm);
        }
    }, [fetchProducts, searchTerm, currentAppView, userRole]);

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(e.target.value);
    };

    /**
     * Function to navigate to product detail page for regular users.
     */
    const handleProductClick = (productId: number) => {
        setSelectedProductId(productId);
        setCurrentAppView('productDetail');
    };

    /**
     * Function to go back to the home page from product detail for regular users.
     */
    const handleBackToHome = () => {
        setSelectedProductId(null);
        setCurrentAppView('home');
        setSearchTerm(''); // Clear search when returning home
        fetchProducts(''); // Re-fetch all products for a fresh home view
    };

    /**
     * Handles the login form submission.
     */
    const handleLogin = async (event: React.FormEvent) => {
        event.preventDefault();

        setLoginLoading(true);
        setLoginError(null);
        setLoginSuccessMessage(null);
        setAuthToken(null);
        setIsLoggedIn(false);
        setUserRole('user'); // Reset role on new login attempt

        const requestBody = { email: username, password }; // Backend expects 'email' for login

        try {
            const response = await fetch(`${API_BASE_URL}/api/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();

            if (response.ok && data.success && data.jwt) { // Check for 'success' boolean and 'jwt' token
                setAuthToken(data.jwt);
                setIsLoggedIn(true);
                // Simple role determination: if username contains 'admin', assume admin role
                const determinedRole = username.includes('admin') ? 'admin' : 'user';
                setUserRole(determinedRole);

                if (determinedRole === 'admin') {
                    setCurrentAppView('admin'); // Navigate to admin dashboard for admin
                } else {
                    setCurrentAppView('home'); // Navigate to home page for regular users
                }

                setLoginSuccessMessage(data.message || 'Authentication successful!');
                setUsername('');
                setPassword('');
            } else {
                const errorMessage = data.message || `Authentication failed. Server responded with HTTP Status: ${response.status} ${response.statusText}.`;
                setLoginError(errorMessage);
            }
        } catch (err: any) {
            console.error('Error during authentication:', err);
            setLoginError('Network error or an unexpected issue occurred during login. Please check your API_BASE_URL and network connection.');
        } finally {
            setLoginLoading(false);
        }
    };

    /**
     * Logout function.
     */
    const handleLogout = async () => {
        setLoginLoading(true); // Indicate logout is in progress
        setLoginError(null);
        setLoginSuccessMessage(null);

        if (!authToken) {
            // Already logged out or no token, just reset state
            setAuthToken(null);
            setIsLoggedIn(false);
            setUsername('');
            setPassword('');
            setUserRole('user'); // Reset role
            setCurrentAppView('home'); // Go to home page after logout
            setCurrentAuthView('login'); // Reset auth view to login
            setLoginSuccessMessage("Successfully logged out.");
            setLoginLoading(false);
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/users/logout`, {
                method: 'POST',
                headers: getAuthHeaders(), // Use getAuthHeaders for logout request
            });

            const data = await response.json();

            if (response.ok && data.success) {
                setLoginSuccessMessage(data.message || 'Logged out successfully.');
            } else {
                setLoginError(data.message || 'Logout failed. Please try again.');
            }
        } catch (err: any) {
            console.error('Error during logout:', err);
            setLoginError('Network error or an unexpected issue occurred during logout.');
        } finally {
            setAuthToken(null);
            setIsLoggedIn(false);
            setUsername('');
            setPassword('');
            setUserRole('user'); // Reset role
            setCurrentAppView('home'); // Go to home page after logout
            setCurrentAuthView('login'); // Reset auth view to login
            setLoginLoading(false);
            fetchProducts(''); // Re-fetch products for the public home page after logout
        }
    };

    const renderContent = () => {
        if (!isLoggedIn) {
            // Render different authentication views based on currentAuthView state
            switch (currentAuthView) {
                case 'login':
                    return (
                        <AuthenticationView
                            username={username}
                            setUsername={setUsername}
                            password={password}
                            setPassword={setPassword}
                            handleLogin={handleLogin}
                            loading={loginLoading}
                            error={loginError}
                            successMessage={loginSuccessMessage}
                            onRegisterClick={() => setCurrentAuthView('register')}
                            onForgotPasswordClick={() => setCurrentAuthView('forgotPassword')}
                        />
                    );
                case 'register':
                    return (
                        <RegistrationView
                            API_BASE_URL={API_BASE_URL}
                            onBackToLogin={() => setCurrentAuthView('login')}
                            onRegistrationSuccess={() => setCurrentAuthView('verifyEmail')}
                        />
                    );
                case 'forgotPassword':
                    return (
                        <ForgotPasswordView
                            API_BASE_URL={API_BASE_URL}
                            onBackToLogin={() => setCurrentAuthView('login')}
                            onResetPasswordSuccess={() => setCurrentAuthView('resetPassword')}
                        />
                    );
                case 'resetPassword':
                    return (
                        <ResetPasswordView
                            API_BASE_URL={API_BASE_URL}
                            onBackToLogin={() => setCurrentAuthView('login')}
                        />
                    );
                case 'verifyEmail':
                    return (
                        <EmailVerificationView
                            API_BASE_URL={API_BASE_URL}
                            onBackToLogin={() => setCurrentAuthView('login')}
                        />
                    );
                default:
                    // Fallback to login if currentAuthView is unknown when not logged in
                    return (
                        <AuthenticationView
                            username={username}
                            setUsername={setUsername}
                            password={password}
                            setPassword={setPassword}
                            handleLogin={handleLogin}
                            loading={loginLoading}
                            error={loginError}
                            successMessage={loginSuccessMessage}
                            onRegisterClick={() => setCurrentAuthView('register')}
                            onForgotPasswordClick={() => setCurrentAuthView('forgotPassword')}
                        />
                    );
            }
        } else { // User is logged in
            if (userRole === 'admin') {
                // AdminDashboardView now manages its own sub-views (product/shop creation, detail views)
                return (
                    <AdminDashboardView
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onLogout={handleLogout}
                        userRole={userRole}
                    />
                );
            } else { // Regular user logged in
                switch (currentAppView) { // Use the 'currentAppView' for regular user navigation
                    case 'home':
                        return (
                            <HomePageView
                                onLoginClick={() => setCurrentAuthView('login')}
                                products={products}
                                loading={productsLoading}
                                error={productsError}
                                searchTerm={searchTerm}
                                onSearchChange={handleSearchChange}
                                onLogout={handleLogout}
                                onProductClick={handleProductClick} // Pass handler for product clicks
                            />
                        );
                    case 'productDetail':
                        if (selectedProductId === null) {
                            return <MessageDisplay message="No product selected to view details." type="error" />;
                        }
                        return (
                            <ProductDetailView
                                API_BASE_URL={API_BASE_URL}
                                productId={selectedProductId}
                                onBack={handleBackToHome} // Pass back handler
                            />
                        );
                    default:
                        // Fallback for logged-in user to home if 'currentAppView' is unhandled
                        return (
                            <HomePageView
                                onLoginClick={() => setCurrentAuthView('login')}
                                products={products}
                                loading={productsLoading}
                                error={productsError}
                                searchTerm={searchTerm}
                                onSearchChange={handleSearchChange}
                                onLogout={handleLogout}
                                onProductClick={handleProductClick}
                            />
                        );
                }
            }
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-100 to-pink-200 flex items-center justify-center p-4 font-inter">
            <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-6xl transform transition-all duration-300 hover:scale-[1.01] overflow-hidden">
                {renderContent()}

                <div className="mt-8 text-center text-gray-500 text-sm">
                    <p className="mt-2">
                        <strong className="text-gray-600">Important:</strong> Ensure your Spring Boot APIs are running and accessible at the configured `API_BASE_URL`.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AccountDashboard;
