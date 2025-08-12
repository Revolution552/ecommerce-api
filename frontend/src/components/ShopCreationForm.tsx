import React, { useState, useEffect, useCallback } from 'react';
import { ArrowLeft } from 'lucide-react'; // Needed for the back button icon

// --- Interfaces for Type Definitions (essential for this component) ---
interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    imageUrl?: string;
    categoryId?: number;
    categoryName?: string;
    shopId?: number;
    shopName?: string;
}

interface Shop {
    id: number;
    name: string;
    location: string;
    description: string;
    userId: number; // Owner's User ID
    logoUrl?: string; // Optional logo URL
}

interface Category {
    id: number;
    name: string;
}

interface User {
    id: number;
    email: string;
    roles: string[];
    username?: string;
}

interface ApiResponse {
    success: boolean;
    message?: string;
    products?: Product[];
    shops?: Shop[];
    users?: User[];
    product?: Product;
    shop?: Shop;
    user?: User;
    categories?: Category[];
    jwt?: string; // Assuming JWT is part of login response
}

// --- MessageDisplay Component (copied for self-containment) ---
// This component is crucial for displaying success, error, or info messages.
interface MessageDisplayProps {
    message: string | null;
    type?: 'success' | 'error' | 'info';
    extraContent?: React.ReactNode;
}

const MessageDisplay: React.FC<MessageDisplayProps> = ({ message, type, extraContent }) => {
    if (!message) return null;

    const baseClasses = "mt-6 p-4 border-l-4 rounded-lg shadow-sm";
    const successClasses = "bg-green-100 border-green-500 text-green-700";
    const errorClasses = "bg-red-100 border-red-500 text-red-700";
    const infoClasses = "bg-blue-100 border-blue-500 text-blue-700";

    let currentClasses = '';
    let headerText = '';

    switch (type) {
        case 'success':
            currentClasses = successClasses;
            headerText = 'Success:';
            break;
        case 'error':
            currentClasses = errorClasses;
            headerText = 'Error:';
            break;
        case 'info':
            currentClasses = infoClasses;
            headerText = 'Info:';
            break;
        default:
            currentClasses = infoClasses;
            headerText = 'Info:';
    }

    return (
        <div className={`${baseClasses} ${currentClasses}`}>
            <p className="font-semibold">{headerText}</p>
            <p>{message}</p>
            {extraContent && <div className="mt-4">{extraContent}</div>}
        </div>
    );
};

// --- ShopCreationForm Component ---
interface ShopCreationFormProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit; // Passed down from App
    onShopCreated: () => void; // Callback after successful creation
    onBack: () => void; // Callback to navigate back (e.g., to shop list)
}

const ShopCreationForm: React.FC<ShopCreationFormProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onShopCreated, onBack }) => {
    const [name, setName] = useState('');
    const [location, setLocation] = useState('');
    const [description, setDescription] = useState('');
    const [selectedUserId, setSelectedUserId] = useState<string>(''); // To hold the ID of the selected user (owner)
    const [logoFile, setLogoFile] = useState<File | null>(null);

    const [users, setUsers] = useState<User[]>([]); // State to hold the list of users fetched from API

    const [loading, setLoading] = useState<boolean>(false); // For form submission loading
    const [error, setError] = useState<string | null>(null); // For form submission errors
    const [successMessage, setSuccessMessage] = useState<string | null>(null); // For form submission success messages
    const [dataLoading, setDataLoading] = useState(true); // For initial data fetching of users
    const [dataError, setDataError] = useState<string | null>(null); // For errors during initial data fetch

    // Effect to fetch the list of users (potential shop owners) when the component mounts
    useEffect(() => {
        const fetchUsers = async () => {
            setDataLoading(true);
            setDataError(null);
            try {
                const usersResponse = await fetch(`${API_BASE_URL}/api/users/getall`, {
                    headers: getAuthHeaders(),
                });
                const usersData: ApiResponse = await usersResponse.json();
                if (usersResponse.ok && usersData.success && Array.isArray(usersData.users)) {
                    setUsers(usersData.users);
                    // Set default selected user if available
                    if (usersData.users.length > 0) {
                        setSelectedUserId(usersData.users[0].id.toString());
                    } else {
                        setDataError("No users found. Please add users in the backend or check the /api/users/getall endpoint.");
                    }
                } else {
                    const msg = usersData.message || 'Failed to fetch users.';
                    console.error('Failed to fetch users:', msg);
                    setDataError(`Failed to load users: ${msg}`);
                }
            } catch (err: any) {
                console.error('Network error fetching users:', err);
                setDataError('Network error fetching users. Ensure backend is running and /api/users/getall endpoint is correct.');
            } finally {
                setDataLoading(false);
            }
        };
        fetchUsers();
    }, [API_BASE_URL, getAuthHeaders]); // Re-run if API_BASE_URL or getAuthHeaders changes

    // Handle file input changes for shop logo
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setLogoFile(e.target.files[0]);
        }
    };

    // Handle form submission to create a new shop
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault(); // Prevent default form submission behavior

        setLoading(true); // Indicate loading state
        setError(null); // Clear previous errors
        setSuccessMessage(null); // Clear previous success messages

        // Basic validation
        if (!selectedUserId) {
            setError("Please select a shop owner (User ID).");
            setLoading(false);
            return;
        }

        // Create FormData object to send both JSON data and file
        const formData = new FormData();
        // Append shop details as a JSON blob
        formData.append('shop', new Blob([JSON.stringify({
            name,
            location,
            description,
            userId: parseInt(selectedUserId), // Convert user ID to number
        })], { type: 'application/json' }));

        // Append the logo file if selected
        if (logoFile) {
            formData.append('logo', logoFile);
        }

        try {
            // Send POST request to shop creation endpoint
            const response = await fetch(`${API_BASE_URL}/shops`, {
                method: 'POST',
                // For FormData, do NOT set 'Content-Type': 'application/json'.
                // The browser sets 'Content-Type': 'multipart/form-data' automatically,
                // including the correct boundary. We only add Authorization header.
                headers: getAuthHeaders(null), // Pass null to avoid setting 'Content-Type' explicitly
                body: formData,
            });

            const data: ApiResponse = await response.json();

            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Shop created successfully!');
                // Clear form fields after successful creation
                setName('');
                setLocation('');
                setDescription('');
                setSelectedUserId(users.length > 0 ? users[0].id.toString() : ''); // Reset to first user or empty
                setLogoFile(null); // Clear the selected logo file
                onShopCreated(); // Call the callback to indicate shop was created
            } else {
                setError(data.message || 'Failed to create shop.');
            }
        } catch (err: any) {
            console.error('Error creating shop:', err);
            setError(`Network error: Could not create shop. Check backend logs.`);
        } finally {
            setLoading(false); // End loading state
        }
    };

    return (
        <div className="p-4">
            {/* Back button to navigate to the previous view */}
            <button
                onClick={onBack}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Shops List
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Add New Shop</h2>
            {/* Message display for errors, successes, or info */}
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />
            <MessageDisplay message={dataError} type="error" /> {/* Display errors from initial data fetch */}

            {/* Shop Creation Form */}
            <form onSubmit={handleSubmit} className="bg-gray-50 p-6 rounded-lg shadow-inner mb-8 space-y-4">
                <input
                    type="text"
                    name="name"
                    placeholder="Shop Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full p-3 border border-gray-300 rounded-md"
                    required
                />
                <input
                    type="text"
                    name="location"
                    placeholder="Location"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    className="w-full p-3 border border-gray-300 rounded-md"
                    required
                />
                <textarea
                    name="description"
                    placeholder="Description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full p-3 border border-gray-300 rounded-md"
                    rows={3}
                    required
                ></textarea>

                {/* Shop Owner (User) Selection Dropdown */}
                <div>
                    <label htmlFor="user-select" className="block text-gray-700 text-sm font-bold mb-2">Shop Owner (User ID)</label>
                    <select
                        id="user-select"
                        value={selectedUserId}
                        onChange={(e) => setSelectedUserId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || users.length === 0} // Disable if loading or no users
                    >
                        {dataLoading && <option value="">Loading users...</option>}
                        {!dataLoading && users.length === 0 && <option value="">No users available</option>}
                        {users.map(user => (
                            <option key={user.id} value={user.id.toString()}>
                                {user.email} (ID: {user.id}) {/* Display user email and ID for selection */}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Shop Logo Upload */}
                <div>
                    <label className="block text-gray-700 text-sm font-bold mb-2">Shop Logo (Optional)</label>
                    <input
                        type="file"
                        name="logo"
                        accept="image/*" // Only allow image files
                        onChange={handleFileChange}
                        className="w-full p-3 border border-gray-300 rounded-md"
                    />
                </div>
                {/* Submit Button */}
                <button
                    type="submit"
                    className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 px-6 rounded-md shadow-md transition-all duration-200"
                    // Disable if loading, data is still fetching, or no users are available
                    disabled={loading || dataLoading || users.length === 0}
                >
                    {loading ? 'Creating...' : 'Create Shop'}
                </button>
            </form>
        </div>
    );
};

export default ShopCreationForm; // Export the component for use in other files
