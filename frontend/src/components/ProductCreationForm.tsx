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


// --- ProductCreationForm Component ---
interface ProductCreationFormProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit; // Passed down from App
    onProductCreated: () => void; // Callback after successful creation
    onBack: () => void; // Callback to navigate back (e.g., to product list)
}

const ProductCreationForm: React.FC<ProductCreationFormProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onProductCreated, onBack }) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [price, setPrice] = useState<string | number>('');
    const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
    const [selectedShopId, setSelectedShopId] = useState<string>('');
    const [imageFile, setImageFile] = useState<File | null>(null);

    const [categories, setCategories] = useState<Category[]>([]);
    const [shops, setShops] = useState<Shop[]>([]);

    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [dataLoading, setDataLoading] = useState(true); // For initial data fetching of categories/shops
    const [dataError, setDataError] = useState<string | null>(null); // For errors during initial data fetch

    // Effect to fetch categories and shops when the component mounts
    useEffect(() => {
        const fetchDropdownData = async () => {
            setDataLoading(true);
            setDataError(null);
            try {
                // Fetch Categories
                const categoriesResponse = await fetch(`${API_BASE_URL}/api/categories`, {
                    headers: getAuthHeaders(),
                });
                const categoriesData: ApiResponse = await categoriesResponse.json();
                if (categoriesResponse.ok && categoriesData.success && Array.isArray(categoriesData.categories)) {
                    setCategories(categoriesData.categories);
                    // Set default selected category if available
                    if (categoriesData.categories.length > 0) {
                        setSelectedCategoryId(categoriesData.categories[0].id.toString());
                    } else {
                        setDataError("No categories found. Please add categories in the backend.");
                    }
                } else {
                    const msg = categoriesData.message || 'Failed to fetch categories.';
                    console.error('Failed to fetch categories:', msg);
                    setDataError(`Failed to load categories: ${msg}`);
                }

                // Fetch Shops
                const shopsResponse = await fetch(`${API_BASE_URL}/shops/getall`, {
                    headers: getAuthHeaders(),
                });
                const shopsData: ApiResponse = await shopsResponse.json();
                if (shopsResponse.ok && shopsData.success && Array.isArray(shopsData.shops)) {
                    setShops(shopsData.shops);
                    // Set default selected shop if available
                    if (shopsData.shops.length > 0) {
                        setSelectedShopId(shopsData.shops[0].id.toString());
                    } else {
                        // Append error if categories already had an error
                        setDataError(prev => prev ? prev + " No shops found." : "No shops found. Please add shops in the backend.");
                    }
                } else {
                    const msg = shopsData.message || 'Failed to fetch shops.';
                    console.error('Failed to fetch shops:', msg);
                    // Append error if categories already had an error
                    setDataError(prev => prev ? prev + ` Failed to load shops: ${msg}` : `Failed to load shops: ${msg}`);
                }

            } catch (err: any) {
                console.error('Network error fetching dropdown data:', err);
                setDataError('Network error fetching categories/shops. Ensure backend is running and endpoints are correct.');
            } finally {
                setDataLoading(false);
            }
        };
        fetchDropdownData();
    }, [API_BASE_URL, getAuthHeaders]); // Re-run if API_BASE_URL or getAuthHeaders changes

    // Handle file input changes for product image
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setImageFile(e.target.files[0]);
        }
    };

    // Handle form submission to create a new product
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault(); // Prevent default form submission behavior

        setLoading(true); // Indicate loading state
        setError(null); // Clear previous errors
        setSuccessMessage(null); // Clear previous success messages

        // Basic validation
        if (!imageFile) {
            setError("Product image is required.");
            setLoading(false);
            return;
        }
        if (!selectedCategoryId) {
            setError("Please select a category.");
            setLoading(false);
            return;
        }
        if (!selectedShopId) {
            setError("Please select a shop.");
            setLoading(false);
            return;
        }

        // Create FormData object to send both JSON data and file
        const formData = new FormData();
        // Append product details as a JSON blob
        formData.append('product', new Blob([JSON.stringify({
            name,
            description,
            price: parseFloat(price as string), // Convert price to number
            categoryId: parseInt(selectedCategoryId), // Convert category ID to number
            shopId: parseInt(selectedShopId) // Convert shop ID to number
        })], { type: 'application/json' }));
        // Append the image file
        formData.append('image', imageFile);

        try {
            // Send POST request to product creation endpoint
            const response = await fetch(`${API_BASE_URL}/products`, {
                method: 'POST',
                // For FormData, do NOT set 'Content-Type': 'application/json'.
                // The browser sets 'Content-Type': 'multipart/form-data' automatically,
                // including the correct boundary. We only add Authorization header.
                headers: getAuthHeaders(null), // Pass null to avoid setting 'Content-Type' explicitly
                body: formData,
            });

            const data: ApiResponse = await response.json();

            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Product created successfully!');
                // Clear form fields after successful creation
                setName('');
                setDescription('');
                setPrice('');
                setSelectedCategoryId(categories.length > 0 ? categories[0].id.toString() : '');
                setSelectedShopId(shops.length > 0 ? shops[0].id.toString() : '');
                setImageFile(null); // Clear the selected image file
                onProductCreated(); // Call the callback to indicate product was created
            } else {
                setError(data.message || 'Failed to create product.');
            }
        } catch (err: any) {
            console.error('Error creating product:', err);
            setError(`Network error: Could not create product. Check backend logs.`);
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
                <ArrowLeft size={20} /> Back to Products List
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Add New Product</h2>
            {/* Message display for errors, successes, or info */}
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />
            <MessageDisplay message={dataError} type="error" /> {/* Display errors from initial data fetch */}

            {/* Product Creation Form */}
            <form onSubmit={handleSubmit} className="bg-gray-50 p-6 rounded-lg shadow-inner mb-8 space-y-4">
                <input
                    type="text"
                    name="name"
                    placeholder="Product Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
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
                <input
                    type="number"
                    name="price"
                    placeholder="Price"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    className="w-full p-3 border border-gray-300 rounded-md"
                    step="0.01" // Allow decimal values for price
                    required
                />

                {/* Category Selection Dropdown */}
                <div>
                    <label htmlFor="category-select" className="block text-gray-700 text-sm font-bold mb-2">Category</label>
                    <select
                        id="category-select"
                        value={selectedCategoryId}
                        onChange={(e) => setSelectedCategoryId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || categories.length === 0} // Disable if loading or no categories
                    >
                        {dataLoading && <option value="">Loading categories...</option>}
                        {!dataLoading && categories.length === 0 && <option value="">No categories available</option>}
                        {categories.map(category => (
                            <option key={category.id} value={category.id.toString()}>
                                {category.name}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Shop Selection Dropdown */}
                <div>
                    <label htmlFor="shop-select" className="block text-gray-700 text-sm font-bold mb-2">Shop</label>
                    <select
                        id="shop-select"
                        value={selectedShopId}
                        onChange={(e) => setSelectedShopId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || shops.length === 0} // Disable if loading or no shops
                    >
                        {dataLoading && <option value="">Loading shops...</option>}
                        {!dataLoading && shops.length === 0 && <option value="">No shops available</option>}
                        {shops.map(shop => (
                            <option key={shop.id} value={shop.id.toString()}>
                                {shop.name}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Product Image Upload */}
                <div>
                    <label className="block text-gray-700 text-sm font-bold mb-2">Product Image</label>
                    <input
                        type="file"
                        name="image"
                        accept="image/*" // Only allow image files
                        onChange={handleFileChange}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                    />
                </div>
                {/* Submit Button */}
                <button
                    type="submit"
                    className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 px-6 rounded-md shadow-md transition-all duration-200"
                    // Disable if loading, data is still fetching, or no categories/shops are available
                    disabled={loading || dataLoading || categories.length === 0 || shops.length === 0}
                >
                    {loading ? 'Creating...' : 'Create Product'}
                </button>
            </form>
        </div>
    );
};

export default ProductCreationForm; // Export the component for use in other files
