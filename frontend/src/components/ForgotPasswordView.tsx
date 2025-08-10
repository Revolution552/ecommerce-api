"use client";

import React, { useState } from 'react';
import MessageDisplay from './MessageDisplay'; // Assuming MessageDisplay is in the same directory

interface ForgotPasswordViewProps {
    API_BASE_URL: string;
    onBackToLogin: () => void;
    onResetPasswordSuccess: () => void; // Callback to transition to ResetPasswordView
}

const ForgotPasswordView: React.FC<ForgotPasswordViewProps> = ({ API_BASE_URL, onBackToLogin, onResetPasswordSuccess }) => {
    const [email, setEmail] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const handleRequestResetToken = async (event: React.FormEvent) => {
        event.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            // Note: Your backend uses @RequestParam for email, so it's a query parameter
            const response = await fetch(`${API_BASE_URL}/api/users/forgot?email=${encodeURIComponent(email)}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }, // Still send Content-Type, though body is empty
            });

            const data = await response.json();

            if (response.ok && data.success) { // Backend uses 'success' boolean
                setSuccessMessage(data.message || 'Password reset token requested successfully. Check your email!');
                onResetPasswordSuccess(); // Transition to ResetPasswordView
            } else {
                setError(data.message || 'Failed to request password reset token. Please check your email address.');
            }
        } catch (err: any) {
            console.error('Error requesting password reset token:', err);
            setError('Network error or an unexpected issue occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <h1 className="text-4xl font-extrabold text-center text-gray-800 mb-8 tracking-tight">
                <span className="bg-clip-text text-transparent bg-gradient-to-r from-purple-600 to-pink-700">
                    Forgot Password
                </span>
            </h1>

            <form onSubmit={handleRequestResetToken} className="space-y-6">
                <div>
                    <label htmlFor="email" className="block text-gray-700 text-sm font-semibold mb-2">
                        Email:
                    </label>
                    <input
                        type="email"
                        id="email"
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition-all duration-200"
                        placeholder="Enter your email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className={`w-full py-3 px-6 rounded-full font-bold text-white transition-all duration-300 ease-in-out shadow-lg flex items-center justify-center space-x-2
                        ${loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-purple-600 hover:bg-purple-700 transform hover:scale-105 active:scale-95'}`}
                >
                    {loading ? (
                        <>
                            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            <span>Requesting...</span>
                        </>
                    ) : (
                        <span>Request Reset Token</span>
                    )}
                </button>
            </form>

            <div className="mt-4 text-center">
                <button
                    onClick={onBackToLogin}
                    className="text-sm text-blue-600 hover:underline"
                >
                    Back to Login
                </button>
            </div>

            <MessageDisplay type="error" message={error} />
            <MessageDisplay type="success" message={successMessage} />
        </>
    );
};

export default ForgotPasswordView;
