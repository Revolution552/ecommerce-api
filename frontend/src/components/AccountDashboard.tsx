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
import UserManagementView from './UserManagementView';
import EmailVerificationView from './EmailVerificationView';

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

    // Shared UI Feedback States for Login
    const [loginLoading, setLoginLoading] = useState<boolean>(false);
    const [loginError, setLoginError] = useState<string | null>(null);
    const [loginSuccessMessage, setLoginSuccessMessage] = useState<string | null>(null);

    // State to manage which authentication-related view is active
    // Added 'verifyEmail' to the possible states
    const [currentAuthView, setCurrentAuthView] = useState<'login' | 'register' | 'forgotPassword' | 'resetPassword' | 'verifyEmail'>('login');

    // Current Main View State (after successful login)
    const [currentDashboardView, setCurrentDashboardView] = useState<string>('users'); // Default to users for admin demo

    // Base URL for your Spring Boot API.
    const API_BASE_URL = 'http://localhost:8080';

    /**
     * Helper to get common fetch headers, conditionally including Authorization.
     * This function is defined here because authToken is in this scope and passed down.
     */
    const getAuthHeaders = useCallback((contentType: string = 'application/json'): HeadersInit => {
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
     * Handles the login form submission.
     */
    const handleLogin = async (event: React.FormEvent) => {
        event.preventDefault();

        setLoginLoading(true);
        setLoginError(null);
        setLoginSuccessMessage(null);
        setAuthToken(null);
        setIsLoggedIn(false);

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
                setCurrentDashboardView('users'); // Switch to users view after login
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
            setCurrentDashboardView('users');
            setCurrentAuthView('login');
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
            setCurrentDashboardView('users');
            setCurrentAuthView('login');
            setLoginLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-100 to-pink-200 flex items-center justify-center p-4 font-inter">
            <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-4xl transform transition-all duration-300 hover:scale-[1.01] overflow-hidden">

                {!isLoggedIn ? (
                    // Render different authentication views based on currentAuthView state
                    <>
                        {currentAuthView === 'login' && (
                            <AuthenticationView
                                username={username}
                                setUsername={setUsername}
                                password={password}
                                setPassword={setPassword}
                                handleLogin={handleLogin}
                                loginLoading={loginLoading}
                                loginError={loginError}
                                loginSuccessMessage={loginSuccessMessage}
                                onRegisterClick={() => setCurrentAuthView('register')}
                                onForgotPasswordClick={() => setCurrentAuthView('forgotPassword')}
                            />
                        )}

                        {currentAuthView === 'register' && (
                            <RegistrationView
                                API_BASE_URL={API_BASE_URL}
                                onBackToLogin={() => setCurrentAuthView('login')}
                                onRegistrationSuccess={() => setCurrentAuthView('verifyEmail')} // FIXED: Correctly navigate to EmailVerificationView
                            />
                        )}

                        {currentAuthView === 'forgotPassword' && (
                            <ForgotPasswordView
                                API_BASE_URL={API_BASE_URL}
                                onBackToLogin={() => setCurrentAuthView('login')}
                                onResetPasswordSuccess={() => setCurrentAuthView('resetPassword')}
                            />
                        )}

                        {currentAuthView === 'resetPassword' && (
                            <ResetPasswordView
                                API_BASE_URL={API_BASE_URL}
                                onBackToLogin={() => setCurrentAuthView('login')}
                            />
                        )}

                        {/* Email Verification View - User would typically land here via a link in an email */}
                        {currentAuthView === 'verifyEmail' && (
                            <EmailVerificationView
                                API_BASE_URL={API_BASE_URL}
                                onBackToLogin={() => setCurrentAuthView('login')}
                            />
                        )}
                    </>
                ) : (
                    // Render authenticated dashboard content
                    <>
                        <div className="flex justify-between items-center mb-8">
                            <h1 className="text-4xl font-extrabold text-gray-800 tracking-tight">
                                <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-700">
                                    {currentDashboardView === 'users' ? 'User Management' : 'Dashboard'}
                                </span>
                            </h1>
                            <button
                                onClick={handleLogout}
                                className="bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 transform hover:scale-105 active:scale-95"
                                disabled={loginLoading} // Disable logout button while logging out
                            >
                                {loginLoading ? 'Logging out...' : 'Logout'}
                            </button>
                        </div>

                        {/* Navigation Tabs for Dashboard */}
                        <div className="mb-8 border-b border-gray-200">
                            <nav className="-mb-px flex space-x-8" aria-label="Tabs">
                                <button
                                    onClick={() => setCurrentDashboardView('users')}
                                    className={`${currentDashboardView === 'users' ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'} whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200`}
                                >
                                    Manage Users
                                </button>
                                {/* Add more dashboard tabs here if needed for other functionalities */}
                            </nav>
                        </div>

                        {/* Conditionally render dashboard views */}
                        {currentDashboardView === 'users' && (
                            <UserManagementView
                                API_BASE_URL={API_BASE_URL}
                                authToken={authToken}
                                getAuthHeaders={getAuthHeaders}
                            />
                        )}
                    </>
                )}

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
