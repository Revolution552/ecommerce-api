import React, { useState, useEffect, useCallback } from 'react';
import MessageDisplay from './MessageDisplay'; // Assuming MessageDisplay is in the same directory
// @ts-ignore
import { ArrowLeft } from 'lucide-react'; // Import an icon for back button

// --- Interfaces for Type Definitions ---
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
    // Add any other product details your backend provides
}

interface ProductApiResponse {
    success: boolean;
    message?: string;
    product?: Product;
}

interface ProductDetailViewProps {
    API_BASE_URL: string;
    productId: number;
    onBack: () => void; // Function to go back to the previous view (e.g., home page)
}

const ProductDetailView: React.FC<ProductDetailViewProps> = ({ API_BASE_URL, productId, onBack }) => {
    const [product, setProduct] = useState<Product | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchProductDetails = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            // This endpoint corresponds to your Spring Boot controller's @GetMapping("/{id}")
            const response = await fetch(`${API_BASE_URL}/products/${productId}`);
            const data: ProductApiResponse = await response.json();
            if (response.ok && data.success && data.product) {
                setProduct(data.product);
            } else {
                setError(data.message || 'Failed to fetch product details.');
            }
        } catch (err: any) {
            setError(`Network error fetching product details: ${err.message}`);
        } finally {
            setLoading(false);
        }
    }, [API_BASE_URL, productId]);

    useEffect(() => {
        fetchProductDetails();
    }, [fetchProductDetails]);

    if (loading) {
        return (
            <div className="p-8 text-center">
                <MessageDisplay message="Loading product details..." type="info" />
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
                    <ArrowLeft size={20} /> Back to Products
                </button>
            </div>
        );
    }

    if (!product) {
        return (
            <div className="p-8 text-center">
                <MessageDisplay message="Product not found." type="error" />
                <button
                    onClick={onBack}
                    className="mt-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2 mx-auto"
                >
                    <ArrowLeft size={20} /> Back to Products
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
                <ArrowLeft size={20} /> Back to Products
            </button>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="flex justify-center items-center">
                    <img
                        src={product.imageUrl || 'https://placehold.co/400x300/e0e0e0/000000?text=No+Image'}
                        alt={product.name}
                        className="w-full h-auto object-cover rounded-lg shadow-lg max-w-md"
                        onError={(e: React.SyntheticEvent<HTMLImageElement, Event>) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://placehold.co/400x300/e0e0e0/000000?text=Image+Error'; }}
                    />
                </div>
                <div>
                    <h1 className="text-4xl font-extrabold text-gray-800 mb-4">{product.name}</h1>
                    <p className="text-2xl font-bold text-indigo-600 mb-6">${product.price?.toFixed(2)}</p>
                    <p className="text-gray-700 leading-relaxed mb-6">{product.description}</p>

                    <div className="grid grid-cols-2 gap-4 text-gray-600 text-sm">
                        <div className="flex items-center gap-2">
                            <span className="font-semibold">Category:</span> {product.categoryName || 'N/A'}
                        </div>
                        <div className="flex items-center gap-2">
                            <span className="font-semibold">Shop:</span> {product.shopName || 'N/A'}
                        </div>
                        <div className="flex items-center gap-2">
                            <span className="font-semibold">Product ID:</span> {product.id}
                        </div>
                        {/* Add more detail fields here as needed */}
                    </div>

                    {/* Example Add to Cart Button (placeholder) */}
                    <button className="mt-8 bg-green-500 hover:bg-green-600 text-white font-semibold py-3 px-6 rounded-md shadow-lg transition-all duration-200 transform hover:scale-105 active:scale-95">
                        Add to Cart
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailView;
