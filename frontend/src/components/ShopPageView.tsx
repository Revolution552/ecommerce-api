import React, { useState, useEffect, useCallback } from 'react';
import MessageDisplay from './MessageDisplay'; // Assuming MessageDisplay is in the same directory
// @ts-ignore
import { ArrowLeft } from 'lucide-react'; // Import an icon for back button

// --- Interfaces for Type Definitions ---
interface Shop {
    id: number;
    name: string;
    location: string;
    description: string;
    userId: number; // Owner's User ID
    logoUrl?: string; // Optional logo URL
    // Add any other shop details your backend provides in ShopDTO
}

interface ShopApiResponse {
    success: boolean;
    message?: string;
    shop?: Shop; // The actual shop object from the API response
}

interface ShopPageViewProps {
    API_BASE_URL: string;
    shopId: number;
    onBack: () => void; // Function to go back to the previous view (e.g., shop management list)
}

const ShopPageView: React.FC<ShopPageViewProps> = ({ API_BASE_URL, shopId, onBack }) => {
    const [shop, setShop] = useState<Shop | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchShopDetails = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            // This endpoint corresponds to your Spring Boot ShopController's @GetMapping("/{shopId}")
            const response = await fetch(`${API_BASE_URL}/shops/${shopId}`);
            const data: ShopApiResponse = await response.json();

            if (response.ok && data.success && data.shop) {
                setShop(data.shop);
            } else {
                setError(data.message || `Failed to fetch shop details for ID: ${shopId}.`);
            }
        } catch (err: any) {
            setError(`Network error fetching shop details: ${err.message}`);
        } finally {
            setLoading(false);
        }
    }, [API_BASE_URL, shopId]);

    useEffect(() => {
        // Fetch details only if shopId is valid
        if (shopId) {
            fetchShopDetails();
        } else {
            setError("Invalid shop ID provided.");
            setLoading(false);
        }
    }, [fetchShopDetails, shopId]);

    if (loading) {
        return (
            <div className="p-8 text-center">
                <MessageDisplay message="Loading shop details..." type="info" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-8 text-center">
                <MessageDisplay message={error} type="error" />
                <button
                    onClick={onBack}
                    className="mt-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2 mx-auto"
                >
                    <ArrowLeft size={20} /> Back to Shops
                </button>
            </div>
        );
    }

    if (!shop) {
        return (
            <div className="p-8 text-center">
                <MessageDisplay message="Shop not found." type="error" />
                <button
                    onClick={onBack}
                    className="mt-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2 mx-auto"
                >
                    <ArrowLeft size={20} /> Back to Shops
                </button>
            </div>
        );
    }

    return (
        <div className="p-8">
            <button
                onClick={onBack}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Shops
            </button>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="flex justify-center items-center">
                    <img
                        src={shop.logoUrl || 'https://placehold.co/400x300/e0e0e0/000000?text=Shop+Logo'}
                        alt={shop.name}
                        className="w-full h-auto object-cover rounded-lg shadow-lg max-w-md"
                        onError={(e: React.SyntheticEvent<HTMLImageElement, Event>) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://placehold.co/400x300/e0e0e0/000000?text=Error'; }}
                    />
                </div>
                <div>
                    <h1 className="text-4xl font-extrabold text-gray-800 mb-4">{shop.name}</h1>
                    <p className="text-gray-700 leading-relaxed mb-4">Location: {shop.location}</p>
                    <p className="text-gray-700 leading-relaxed mb-6">{shop.description}</p>

                    <div className="grid grid-cols-2 gap-4 text-gray-600 text-sm">
                        <div className="flex items-center gap-2">
                            <span className="font-semibold">Shop ID:</span> {shop.id}
                        </div>
                        <div className="flex items-center gap-2">
                            <span className="font-semibold">Owner ID:</span> {shop.userId}
                        </div>
                        {/* Add more detail fields here as needed */}
                    </div>

                    {/* Example Call to Action Button (placeholder) */}
                    <button className="mt-8 bg-purple-500 hover:bg-purple-600 text-white font-semibold py-3 px-6 rounded-md shadow-lg transition-all duration-200 transform hover:scale-105 active:scale-95">
                        View Shop Products
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ShopPageView;
