"use client";

import React, { useState } from 'react';
import MessageDisplay from './MessageDisplay'; // Assuming MessageDisplay is in the same directory

interface RegistrationViewProps {
    API_BASE_URL: string;
    onBackToLogin: () => void;
    onRegistrationSuccess: () => void; // NEW: Callback to navigate to EmailVerificationView
}

const RegistrationView: React.FC<RegistrationViewProps> = ({ API_BASE_URL, onBackToLogin, onRegistrationSuccess }) => {
    const [firstName, setFirstName] = useState<string>('');
    const [surname, setSurname] = useState<string>('');
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');

    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const handleRegister = async (event: React.FormEvent) => {
        event.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        try {
            const response = await fetch(`${API_BASE_URL}/api/users/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ firstName, surname, email, password }),
            });

            const data = await response.json();

            if (response.ok && data.status === 'OK') {
                setSuccessMessage(data.message || 'Registration successful! Please verify your email.');
                setFirstName('');
                setSurname('');
                setEmail('');
                setPassword('');
                onRegistrationSuccess(); // NEW: Call the success callback
            } else {
                setError(data.message || 'Registration failed. Please try again.');
            }
        } catch (err: any) {
            console.error('Error during registration:', err);
            setError('Network error or an unexpected issue occurred during registration.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <h1 className="text-4xl font-extrabold text-center text-gray-800 mb-8 tracking-tight">
                <span className="bg-clip-text text-transparent bg-gradient-to-r from-purple-600 to-pink-700">
                    Register
                </span>
            </h1>

            <form onSubmit={handleRegister} className="space-y-6">
                <div>
                    <label htmlFor="firstName" className="block text-gray-700 text-sm font-semibold mb-2">
                        First Name:
                    </label>
                    <input
                        type="text"
                        id="firstName"
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition-all duration-200"
                        placeholder="Enter your first name"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="surname" className="block text-gray-700 text-sm font-semibold mb-2">
                        Surname:
                    </label>
                    <input
                        type="text"
                        id="surname"
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition-all duration-200"
                        placeholder="Enter your surname"
                        value={surname}
                        onChange={(e) => setSurname(e.target.value)}
                        required
                    />
                </div>
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
                <div>
                    <label htmlFor="password" className="block text-gray-700 text-sm font-semibold mb-2">
                        Password:
                    </label>
                    <input
                        type="password"
                        id="password"
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none transition-all duration-200"
                        placeholder="Enter your password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className={`w-full py-3 px-6 rounded-full font-bold text-white transition-all duration-300 ease-in-out shadow-lg flex items-center justify-center space-x-2
                        ${loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700 transform hover:scale-105 active:scale-95'}`}
                >
                    {loading ? (
                        <>
                            <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            <span>Registering...</span>
                        </>
                    ) : (
                        <span>Register Account</span>
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

export default RegistrationView;
