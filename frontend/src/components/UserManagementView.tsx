"use client";

import React, { useState, useEffect, useCallback } from 'react';
import MessageDisplay from './MessageDisplay'; // Assuming MessageDisplay is in the same directory

interface UserManagementViewProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string) => HeadersInit; // Ensure this is present
}

interface UserResponseDto {
    id: number;
    firstName: string;
    surname: string;
    email: string;
    emailVerified: boolean;
    // Add other fields if your UserResponseDto has them and you need to display them
}

const UserManagementView: React.FC<UserManagementViewProps> = ({ API_BASE_URL, authToken, getAuthHeaders }) => {
    const [users, setUsers] = useState<UserResponseDto[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        if (!authToken) {
            setError("Authentication token is missing. Please log in as an ADMIN.");
            setLoading(false);
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/users`, {
                method: 'GET',
                headers: getAuthHeaders(),
            });

            // The backend returns a List<UserResponseDto> directly, not a wrapped success/data object.
            if (response.ok) {
                const data: UserResponseDto[] = await response.json();
                setUsers(data);
                setSuccessMessage("Users loaded successfully!");
            } else {
                // If the backend sends an error response (e.g., 403 Forbidden for non-admin)
                const errorData = await response.json(); // Try to parse error message
                setError(errorData.message || `Failed to fetch users. Status: ${response.status} ${response.statusText}. You might not have ADMIN privileges.`);
            }
        } catch (err: any) {
            console.error('Error fetching users:', err);
            setError('Network error or an unexpected issue occurred while fetching users.');
        } finally {
            setLoading(false);
        }
    }, [API_BASE_URL, authToken, getAuthHeaders]);

    useEffect(() => {
        if (authToken) {
            void fetchUsers();
        } else {
            setUsers([]); // Clear users if not authenticated
            setError("Please log in to view user management.");
        }
    }, [authToken, fetchUsers]);

    return (
        <div className="space-y-8">
            <h2 className="text-2xl font-bold text-gray-800 mb-4 flex items-center justify-between">
                User Management (Admin Only)
                <button
                    onClick={() => void fetchUsers()}
                    className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-1 px-3 rounded-full text-sm transition-colors duration-200"
                    disabled={loading || !authToken}
                >
                    {loading ? 'Refreshing...' : 'Refresh Users'}
                </button>
            </h2>

            <MessageDisplay type="success" message={successMessage} />
            <MessageDisplay type="error" message={error} />

            {loading && users.length === 0 && (
                <div className="text-center text-gray-500">Loading users...</div>
            )}

            {users.length > 0 ? (
                <div className="overflow-x-auto rounded-lg shadow-md border border-gray-200">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-100">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">First Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Surname</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Verified</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {users.map((user) => (
                            <tr key={user.id}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.id}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.firstName}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.surname}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.email}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${user.emailVerified ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                                            {user.emailVerified ? 'Yes' : 'No'}
                                        </span>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                !loading && error === null && (
                    <div className="text-center text-gray-500 p-4 border border-gray-200 rounded-lg">No users found or you do not have permission.</div>
                )
            )}
        </div>
    );
};

export default UserManagementView;
