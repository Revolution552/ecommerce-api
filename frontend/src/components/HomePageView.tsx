import React, { useState, useEffect, useCallback } from 'react';
// @ts-ignore
import { Search, LogIn } from 'lucide-react'; // Lucide icons for search and login

// --- Interfaces for Type Definitions ---
// Define the structure of a product object based on your backend DTO
interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    imageUrl?: string; // Optional, as it might not always be present
    categoryId?: number;
    categoryName?: string;
    shopId?: number;
    shopName?: string;
}

// Define the structure of the API response for products
interface ProductsApiResponse {
    success: boolean;
    message?: string;
    products?: Product[];
}

// --- MessageDisplay Component (included for self-containment) ---
interface MessageDisplayProps {
    message: string | null;
    type?: 'success' | 'error' | 'info';
}

const MessageDisplay: React.FC<MessageDisplayProps> = ({ message, type }) => {
    if (!message) return null;
    const baseClasses = "p-3 rounded-lg text-sm mb-4";
    const typeClasses = {
        success: "bg-green-100 text-green-700",
        error: "bg-red-100 text-red-700",
        info: "bg-blue-100 text-blue-700",
    };
    return (
        <div className={`${baseClasses} ${typeClasses[type || 'info']}`}>
            {message}
        </div>
    );
};

// --- ProductList Component (nested within HomePageView for self-containment) ---
interface ProductListProps {
    products: Product[];
    loading: boolean;
    error: string | null;
    searchTerm: string;
    onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const ProductList: React.FC<ProductListProps> = ({ products, loading, error, searchTerm, onSearchChange }) => {
    return (
        <div className="p-4">
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Explore Products</h2>
            <div className="mb-6">
                <div className="relative">
                    <input
                        type="text"
                        placeholder="Search products..."
                        value={searchTerm}
                        onChange={onSearchChange}
                        className="w-full p-3 pl-10 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-400 outline-none transition duration-200"
                    />
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                </div>
            </div>
            <MessageDisplay message={error} type="error" />
            {loading && <MessageDisplay message="Loading products..." type="info" />}
            {!loading && products.length === 0 && <MessageDisplay message="No products found." type="info" />}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map(product => (
                    <div key={product.id} className="bg-white rounded-lg shadow-md p-4 flex flex-col items-center text-center">
                        <img
                            src={product.imageUrl || 'https://placehold.co/200x150/e0e0e0/000000?text=No+Image'}
                            alt={product.name}
                            className="w-full h-40 object-cover rounded-md mb-4"
                            onError={(e: React.SyntheticEvent<HTMLImageElement, Event>) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://placehold.co/200x150/e0e0e0/000000?text=Image+Error'; }}
                        />
                        <h3 className="text-lg font-semibold text-gray-800 mb-1">{product.name}</h3>
                        <p className="text-gray-600 text-sm mb-2">{product.description}</p>
                        <p className="text-indigo-600 font-bold text-xl">${product.price?.toFixed(2)}</p>
                        <p className="text-gray-500 text-xs">Category: {product.categoryName || 'N/A'}</p>
                        <p className="text-gray-500 text-xs">Shop: {product.shopName || 'N/A'}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};


// --- HomePageView Component ---
interface HomePageViewProps {
    onLoginClick: () => void;
}

const HomePageView: React.FC<HomePageViewProps> = ({ onLoginClick }) => {
    // Product States for HomePage
    const [products, setProducts] = useState<Product[]>([]);
    const [productsLoading, setProductsLoading] = useState<boolean>(false);
    const [productsError, setProductsError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');

    const API_BASE_URL = 'http://localhost:8080'; // Your Spring Boot API Base URL

    /**
     * Fetches products for the home page or search.
     */
    const fetchProducts = useCallback(async (keyword: string = '') => {
        setProductsLoading(true);
        setProductsError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/products/search?keyword=${encodeURIComponent(keyword)}`);
            const data: ProductsApiResponse = await response.json(); // Type assertion
            if (response.ok && data.success) {
                setProducts(data.products || []);
            } else {
                setProductsError(data.message || 'Failed to fetch products.');
            }
        } catch (err: any) { // Catching 'any' for general network errors
            setProductsError('Network error fetching products. Ensure backend is running.');
        } finally {
            setProductsLoading(false);
        }
    }, [API_BASE_URL]);

    useEffect(() => {
        fetchProducts(searchTerm);
    }, [fetchProducts, searchTerm]);

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(e.target.value);
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center p-4 font-inter">
            <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-6xl transform transition-all duration-300 hover:scale-[1.01] overflow-hidden">
                <div className="relative">
                    <div className="absolute top-4 right-4 z-10"> {/* Added z-10 to ensure button is clickable */}
                        <button
                            onClick={onLoginClick}
                            className="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 transform hover:scale-105 active:scale-95 flex items-center gap-2"
                        >
                            <LogIn size={20} /> Login
                        </button>
                    </div>
                    <ProductList
                        products={products}
                        loading={productsLoading}
                        error={productsError}
                        searchTerm={searchTerm}
                        onSearchChange={handleSearchChange}
                    />
                </div>

                <div className="mt-8 text-center text-gray-500 text-sm">
                    <p className="mt-2">
                        <strong className="text-gray-600">Important:</strong> Ensure your Spring Boot APIs are running and accessible at the configured `API_BASE_URL`.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default HomePageView;
