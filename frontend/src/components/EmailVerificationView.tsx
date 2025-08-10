"use client";

import React, { useState, useEffect } from 'react';
// Removed useSearchParams as it's no longer needed for automatic URL token extraction
import MessageDisplay from './MessageDisplay'; // Assuming MessageDisplay is in the same directory

interface EmailVerificationViewProps {
    API_BASE_URL: string;
    onBackToLogin: () => void; // This callback will now be used to go back to login after verification
}

const EmailVerificationView: React.FC<EmailVerificationViewProps> = ({ API_BASE_URL, onBackToLogin }) => {
    // Removed urlToken state as automatic URL token extraction is no longer needed
    const [manualToken, setManualToken] = useState<string>(''); // Manually entered token
    const [loading, setLoading] = useState<boolean>(false); // Set to false initially as no auto-verification is happening
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    // Removed the useEffect that handled automatic token extraction from URL.
    // The component now directly presents the manual input form.

    const verifyEmail = async (verificationToken: string) => {
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(`${API_BASE_URL}/api/users/verify`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token: verificationToken }), // Backend expects 'token' in request body
            });

            const data = await response.json();

            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Your email has been successfully verified! You can now log in.');
                // Automatically navigate back to login after a short delay
                setTimeout(onBackToLogin, 3000); // Navigate to login after 3 seconds
            } else {
                setError(data.message || 'Email verification failed. The token might be invalid or expired.');
            }
        } catch (err: any) {
            console.error('Error during email verification:', err);
            setError('Network error or an unexpected issue occurred during verification. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleManualVerificationSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        if (manualToken.trim()) {
            void verifyEmail(manualToken.trim());
        } else {
            setError('Please enter a verification token.');
        }
    };

    return (
        <>
            <h1 className="text-4xl font-extrabold text-center text-gray-800 mb-8 tracking-tight">
                <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-green-700">
                    Email Verification
                </span>
            </h1>

            {loading && (
                <div className="text-center text-gray-600 p-4">
                    <svg className="animate-spin mx-auto h-8 w-8 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <p className="mt-2">Verifying your email...</p>
                </div>
            )}

            <MessageDisplay type="error" message={error} />
            <MessageDisplay type="success" message={successMessage} />

            {/* Manual Token Input Form - always show as it's the only way to verify now */}
            {!successMessage && ( // Only hide if verification was successful and redirect is pending
                <form onSubmit={handleManualVerificationSubmit} className="mt-6 space-y-4">
                    <div>
                        <label htmlFor="manualToken" className="block text-gray-700 text-sm font-semibold mb-2">
                            Enter your verification token:
                        </label>
                        <input
                            type="text"
                            id="manualToken"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition-all duration-200"
                            placeholder="Paste your token here"
                            value={manualToken}
                            onChange={(e) => setManualToken(e.target.value)}
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={loading}
                        className={`w-full py-3 px-6 rounded-full font-bold text-white transition-all duration-300 ease-in-out shadow-lg flex items-center justify-center space-x-2
                            ${loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 transform hover:scale-105 active:scale-95'}`}
                    >
                        {loading ? 'Verifying...' : 'Verify Token'}
                    </button>
                </form>
            )}

            {/* The "Go to Login" button will now only appear if not loading and no auto-redirect is in progress */}
            {!loading && !successMessage && ( // Only show if not loading and no success message (meaning auto-redirect hasn't happened yet)
                <div className="mt-6 text-center">
                    <button
                        onClick={onBackToLogin}
                        className="py-2 px-6 rounded-full font-bold text-white bg-purple-600 hover:bg-purple-700 transition-all duration-300 ease-in-out shadow-lg transform hover:scale-105 active:scale-95"
                    >
                        Go to Login
                    </button>
                </div>
            )}
        </>
    );
};

export default EmailVerificationView;
