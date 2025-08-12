import React, { useState, useEffect, useCallback } from 'react';
import { Package, Store, User as UserIcon, LogOut, ArrowLeft } from 'lucide-react'; // Lucide icons

// Ensure Tailwind CSS is loaded in your environment for styling
// <script src="https://cdn.tailwindcss.com"></script>

// --- Interfaces for Type Definitions (Essential for all components here) ---
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
    jwt?: string;
}

// --- MessageDisplay Component (Provided for self-containment) ---
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
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onProductCreated: () => void;
    onBack: () => void;
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
    const [dataLoading, setDataLoading] = useState(true);
    const [dataError, setDataError] = useState<string | null>(null);

    useEffect(() => {
        const fetchDropdownData = async () => {
            setDataLoading(true);
            setDataError(null);
            try {
                const categoriesResponse = await fetch(`${API_BASE_URL}/api/categories`, {
                    headers: getAuthHeaders(),
                });
                const categoriesData: ApiResponse = await categoriesResponse.json();
                if (categoriesResponse.ok && categoriesData.success && Array.isArray(categoriesData.categories)) {
                    setCategories(categoriesData.categories);
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

                const shopsResponse = await fetch(`${API_BASE_URL}/shops/getall`, {
                    headers: getAuthHeaders(),
                });
                const shopsData: ApiResponse = await shopsResponse.json();
                if (shopsResponse.ok && shopsData.success && Array.isArray(shopsData.shops)) {
                    setShops(shopsData.shops);
                    if (shopsData.shops.length > 0) {
                        setSelectedShopId(shopsData.shops[0].id.toString());
                    } else {
                        setDataError(prev => prev ? prev + " No shops found." : "No shops found. Please add shops in the backend.");
                    }
                } else {
                    const msg = shopsData.message || 'Failed to fetch shops.';
                    console.error('Failed to fetch shops:', msg);
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
    }, [API_BASE_URL, getAuthHeaders]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setImageFile(e.target.files[0]);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

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

        const formData = new FormData();
        formData.append('product', new Blob([JSON.stringify({
            name,
            description,
            price: parseFloat(price as string),
            categoryId: parseInt(selectedCategoryId),
            shopId: parseInt(selectedShopId)
        })], { type: 'application/json' }));
        formData.append('image', imageFile);

        try {
            const response = await fetch(`${API_BASE_URL}/products`, {
                method: 'POST',
                headers: getAuthHeaders(null),
                body: formData,
            });

            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Product created successfully!');
                setName('');
                setDescription('');
                setPrice('');
                setSelectedCategoryId(categories.length > 0 ? categories[0].id.toString() : '');
                setSelectedShopId(shops.length > 0 ? shops[0].id.toString() : '');
                setImageFile(null);
                onProductCreated();
            } else {
                setError(data.message || 'Failed to create product.');
            }
        } catch (err: any) {
            setError(`Network error: Could not create product. Check backend logs.`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <button
                onClick={onBack}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Products List
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Add New Product</h2>
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />
            <MessageDisplay message={dataError} type="error" />

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
                    step="0.01"
                    required
                />

                <div>
                    <label htmlFor="category-select" className="block text-gray-700 text-sm font-bold mb-2">Category</label>
                    <select
                        id="category-select"
                        value={selectedCategoryId}
                        onChange={(e) => setSelectedCategoryId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || categories.length === 0}
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

                <div>
                    <label htmlFor="shop-select" className="block text-gray-700 text-sm font-bold mb-2">Shop</label>
                    <select
                        id="shop-select"
                        value={selectedShopId}
                        onChange={(e) => setSelectedShopId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || shops.length === 0}
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

                <div>
                    <label className="block text-gray-700 text-sm font-bold mb-2">Product Image</label>
                    <input
                        type="file"
                        name="image"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                    />
                </div>
                <button
                    type="submit"
                    className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 px-6 rounded-md shadow-md transition-all duration-200"
                    disabled={loading || dataLoading || categories.length === 0 || shops.length === 0}
                >
                    {loading ? 'Creating...' : 'Create Product'}
                </button>
            </form>
        </div>
    );
};

// --- ProductUpdateForm Component ---
interface ProductUpdateFormProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    initialProduct: Product;
    onProductUpdated: () => void;
    onBack: () => void;
}

const ProductUpdateForm: React.FC<ProductUpdateFormProps> = ({ API_BASE_URL, authToken, getAuthHeaders, initialProduct, onProductUpdated, onBack }) => {
    const [name, setName] = useState(initialProduct.name);
    const [description, setDescription] = useState(initialProduct.description);
    const [price, setPrice] = useState<string | number>(initialProduct.price);
    const [selectedCategoryId, setSelectedCategoryId] = useState<string>(initialProduct.categoryId?.toString() || '');
    const [selectedShopId, setSelectedShopId] = useState<string>(initialProduct.shopId?.toString() || '');
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [currentImageUrl, setCurrentImageUrl] = useState(initialProduct.imageUrl);

    const [categories, setCategories] = useState<Category[]>([]);
    const [shops, setShops] = useState<Shop[]>([]);

    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [dataLoading, setDataLoading] = useState(true);
    const [dataError, setDataError] = useState<string | null>(null);

    useEffect(() => {
        const fetchDropdownData = async () => {
            setDataLoading(true);
            setDataError(null);
            try {
                const categoriesResponse = await fetch(`${API_BASE_URL}/api/categories`, {
                    headers: getAuthHeaders(),
                });
                const categoriesData: ApiResponse = await categoriesResponse.json();
                if (categoriesResponse.ok && categoriesData.success && Array.isArray(categoriesData.categories)) {
                    setCategories(categoriesData.categories);
                    if (categoriesData.categories.length > 0) {
                        const initialCatId = initialProduct.categoryId?.toString();
                        if (categoriesData.categories.some(c => c.id.toString() === initialCatId)) {
                            // @ts-ignore
                            setSelectedCategoryId(initialCatId);
                        } else {
                            setSelectedCategoryId(categoriesData.categories[0].id.toString());
                        }
                    } else {
                        setDataError("No categories found. Please add categories in the backend.");
                    }
                } else {
                    const msg = categoriesData.message || 'Failed to fetch categories.';
                    console.error('Failed to fetch categories:', msg);
                    setDataError(`Failed to load categories: ${msg}`);
                }

                const shopsResponse = await fetch(`${API_BASE_URL}/shops/getall`, {
                    headers: getAuthHeaders(),
                });
                const shopsData: ApiResponse = await shopsResponse.json();
                if (shopsResponse.ok && shopsData.success && Array.isArray(shopsData.shops)) {
                    setShops(shopsData.shops);
                    if (shopsData.shops.length > 0) {
                        const initialShopId = initialProduct.shopId?.toString();
                        if (shopsData.shops.some(s => s.id.toString() === initialShopId)) {
                            // @ts-ignore
                            setSelectedShopId(initialShopId);
                        } else {
                            setSelectedShopId(shopsData.shops[0].id.toString());
                        }
                    } else {
                        setDataError(prev => prev ? prev + " No shops found." : "No shops found. Please add shops in the backend.");
                    }
                } else {
                    const msg = shopsData.message || 'Failed to fetch shops.';
                    console.error('Failed to fetch shops:', msg);
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
    }, [API_BASE_URL, getAuthHeaders, initialProduct.categoryId, initialProduct.shopId]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setImageFile(e.target.files[0]);
            // @ts-ignore
            setCurrentImageUrl(null);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

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

        const formData = new FormData();
        formData.append('product', new Blob([JSON.stringify({
            id: initialProduct.id,
            name,
            description,
            price: parseFloat(price as string),
            categoryId: parseInt(selectedCategoryId),
            shopId: parseInt(selectedShopId)
        })], { type: 'application/json' }));

        if (imageFile) {
            formData.append('image', imageFile);
        }

        try {
            const response = await fetch(`${API_BASE_URL}/products/${initialProduct.id}`, {
                method: 'PUT',
                headers: getAuthHeaders(null),
                body: formData,
            });

            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Product updated successfully!');
                onProductUpdated();
            } else {
                setError(data.message || 'Failed to update product.');
            }
        } catch (err: any) {
            setError(`Network error: Could not update product. Check backend logs.`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <button
                onClick={onBack}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Products List
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Edit Product: {initialProduct.name}</h2>
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />
            <MessageDisplay message={dataError} type="error" />

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
                    step="0.01"
                    required
                />
                <div>
                    <label htmlFor="edit-category-select" className="block text-gray-700 text-sm font-bold mb-2">Category</label>
                    <select
                        id="edit-category-select"
                        value={selectedCategoryId}
                        onChange={(e) => setSelectedCategoryId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || categories.length === 0}
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

                <div>
                    <label htmlFor="edit-shop-select" className="block text-gray-700 text-sm font-bold mb-2">Shop</label>
                    <select
                        id="edit-shop-select"
                        value={selectedShopId}
                        onChange={(e) => setSelectedShopId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || shops.length === 0}
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

                <div>
                    <label className="block text-gray-700 text-sm font-bold mb-2">Product Image</label>
                    <input
                        type="file"
                        name="image"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full p-3 border border-gray-300 rounded-md"
                    />
                    {currentImageUrl && (
                        <p className="text-sm text-gray-500 mt-2">Current Image: <a href={currentImageUrl} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:underline">View Image</a></p>
                    )}
                    {imageFile && (
                        <p className="text-sm text-gray-500 mt-2">New Image Selected: {imageFile.name}</p>
                    )}
                </div>
                <button
                    type="submit"
                    className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 px-6 rounded-md shadow-md transition-all duration-200"
                    disabled={loading || dataLoading || categories.length === 0 || shops.length === 0}
                >
                    {loading ? 'Updating...' : 'Update Product'}
                </button>
            </form>
        </div>
    );
};

// --- ProductListSection Component ---
interface ProductListSectionProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onCreateProductClick: () => void;
    onViewProductDetails: (productId: number) => void;
    onEditProductClick: (product: Product) => void;
}

const ProductListSection: React.FC<ProductListSectionProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onCreateProductClick, onViewProductDetails, onEditProductClick }) => {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const fetchProducts = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/products`, {
                headers: getAuthHeaders(),
            });
            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setProducts(data.products || []);
            } else {
                setError(data.message || 'Failed to fetch products.');
            }
        } catch (err: any) {
            setError('Network error fetching products.');
        } finally {
            setLoading(false);
        }
    }, [API_BASE_URL, getAuthHeaders]);

    useEffect(() => {
        fetchProducts();
    }, [fetchProducts]);

    const handleDelete = async (id: number) => {
        if (!window.confirm('Are you sure you want to delete this product?')) return;
        setLoading(true);
        setError(null);
        setSuccessMessage(null);
        try {
            const response = await fetch(`${API_BASE_URL}/products/${id}`, {
                method: 'DELETE',
                headers: getAuthHeaders(),
            });
            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Product deleted successfully!');
                fetchProducts();
            } else {
                setError(data.message || 'Failed to delete product.');
            }
        } catch (err: any) {
            setError('Network error deleting product.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Product Management</h2>
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />

            <div className="mb-6">
                <button
                    onClick={onCreateProductClick}
                    className="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-md shadow-md transition-all duration-200"
                >
                    Add New Product
                </button>
            </div>

            <h3 className="text-2xl font-bold text-gray-800 mb-4">Existing Products</h3>
            {loading && !products.length && <MessageDisplay message="Loading products..." type="info" />}
            {!loading && products.length === 0 && <MessageDisplay message="No products to display." type="info" />}
            <div className="overflow-x-auto">
                <table className="min-w-full bg-white rounded-lg shadow-md">
                    <thead className="bg-gray-200">
                    <tr>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Price</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Category</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Shop</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Image</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {products.map(product => (
                        <tr key={product.id} className="border-b border-gray-200 hover:bg-gray-50">
                            <td className="py-3 px-4 text-sm text-gray-700">{product.id}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">{product.name}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">${product.price?.toFixed(2)}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">{product.categoryName}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">{product.shopName}</td>
                            <td className="py-3 px-4">
                                {product.imageUrl && (
                                    <img src={product.imageUrl} alt="Product" className="w-16 h-12 object-cover rounded" />
                                )}
                            </td>
                            <td className="py-3 px-4 text-sm text-gray-700 space-x-2">
                                <button
                                    onClick={() => onEditProductClick(product)}
                                    className="bg-blue-500 hover:bg-blue-600 text-white p-2 rounded-md transition duration-200"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDelete(product.id)}
                                    className="bg-red-500 hover:bg-red-600 text-white p-2 rounded-md transition duration-200"
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

// --- ProductDetailView Component ---
interface ProductDetailViewProps {
    API_BASE_URL: string;
    productId: number;
    onBack: () => void;
}

const ProductDetailView: React.FC<ProductDetailViewProps> = ({ API_BASE_URL, productId, onBack }) => {
    const [product, setProduct] = useState<Product | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchProductDetails = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await fetch(`${API_BASE_URL}/products/${productId}`);
                const data: ApiResponse = await response.json();
                if (response.ok && data.success && data.product) {
                    setProduct(data.product);
                } else {
                    setError(data.message || `Failed to fetch product details for ID: ${productId}.`);
                }
            } catch (err: any) {
                setError(`Network error fetching product details: ${err.message}`);
            } finally {
                setLoading(false);
            }
        };

        if (productId) {
            void fetchProductDetails();
        } else {
            setError("Invalid product ID provided.");
            setLoading(false);
        }
    }, [API_BASE_URL, productId]);


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
                        src={product.imageUrl || 'https://placehold.co/400x300/e0e0e0/000000?text=Product+Image'}
                        alt={product.name}
                        className="w-full h-auto object-cover rounded-lg shadow-lg max-w-md"
                        onError={(e: React.SyntheticEvent<HTMLImageElement, Event>) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://placehold.co/400x300/e0e0e0/000000?text=Error'; }}
                    />
                </div>
                <div>
                    <h1 className="text-4xl font-extrabold text-gray-800 mb-4">{product.name}</h1>
                    <p className="text-indigo-600 font-bold text-2xl mb-6">${product.price?.toFixed(2)}</p>
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
                    </div>

                    <button className="mt-8 bg-green-500 hover:bg-green-600 text-white font-semibold py-3 px-6 rounded-md shadow-lg transition-all duration-200 transform hover:scale-105 active:scale-95">
                        Add to Cart
                    </button>
                </div>
            </div>
        </div>
    );
};

// --- ProductManagementView Component ---
interface ProductManagementViewProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onBackToAdminDashboard: () => void;
}

const ProductManagementView: React.FC<ProductManagementViewProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onBackToAdminDashboard }) => {
    const [currentSubView, setCurrentSubView] = useState<'list' | 'create' | 'edit' | 'detail'>('list');
    const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
    const [selectedProductToEdit, setSelectedProductToEdit] = useState<Product | null>(null);

    const handleCreateProductClick = () => setCurrentSubView('create');
    const handleProductCreated = () => setCurrentSubView('list');
    const handleViewProductDetails = (productId: number) => {
        setSelectedProductId(productId);
        setCurrentSubView('detail');
    };
    const handleEditProductClick = (product: Product) => {
        setSelectedProductToEdit(product);
        setCurrentSubView('edit');
    };
    const handleProductUpdated = () => {
        setSelectedProductToEdit(null);
        setCurrentSubView('list');
    };
    const handleBackToList = () => {
        setSelectedProductId(null);
        setSelectedProductToEdit(null);
        setCurrentSubView('list');
    };

    const renderProductContent = () => {
        switch (currentSubView) {
            case 'list':
                return (
                    <ProductListSection
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onCreateProductClick={handleCreateProductClick}
                        onViewProductDetails={handleViewProductDetails}
                        onEditProductClick={handleEditProductClick}
                    />
                );
            case 'create':
                return (
                    <ProductCreationForm
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onProductCreated={handleProductCreated}
                        onBack={handleBackToList}
                    />
                );
            case 'edit':
                if (selectedProductToEdit === null) {
                    return <MessageDisplay message="No product selected for editing." type="error" />;
                }
                return (
                    <ProductUpdateForm
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        initialProduct={selectedProductToEdit}
                        onProductUpdated={handleProductUpdated}
                        onBack={handleBackToList}
                    />
                );
            case 'detail':
                if (selectedProductId === null) {
                    return <MessageDisplay message="No product selected to view details." type="error" />;
                }
                return (
                    <ProductDetailView
                        API_BASE_URL={API_BASE_URL}
                        productId={selectedProductId}
                        onBack={handleBackToList}
                    />
                );
            default:
                return <MessageDisplay message="Invalid product management view." type="error" />;
        }
    };

    return (
        <div className="p-4">
            <button
                onClick={onBackToAdminDashboard}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Dashboard
            </button>
            {renderProductContent()}
        </div>
    );
};

// --- ShopCreationForm Component ---
interface ShopCreationFormProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onShopCreated: () => void;
    onBack: () => void;
}

const ShopCreationForm: React.FC<ShopCreationFormProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onShopCreated, onBack }) => {
    const [name, setName] = useState('');
    const [location, setLocation] = useState('');
    const [description, setDescription] = useState('');
    // Removed selectedUserId state, as it will be determined by JWT on backend
    const [logoFile, setLogoFile] = useState<File | null>(null);

    // Removed users state and fetchUsers useEffect, as user selection is no longer needed
    // const [users, setUsers] = useState<User[]>([]);

    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    // dataLoading and dataError related to fetching users are no longer needed here
    // const [dataLoading, setDataLoading] = useState(true);
    // const [dataError, setDataError] = useState<string | null>(null);

    // useEffect(() => {
    //     const fetchUsers = async () => { /* ... */ };
    //     fetchUsers();
    // }, [API_BASE_URL, getAuthHeaders]); // This useEffect is removed

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setLogoFile(e.target.files[0]);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        // No longer check for selectedUserId as it's not provided by the client
        // if (!selectedUserId) {
        //     setError("Please select a shop owner (User ID).");
        //     setLoading(false);
        //     return;
        // }

        const formData = new FormData();
        formData.append('shop', new Blob([JSON.stringify({
            name,
            location,
            description,
            // userId is explicitly excluded here, backend will derive it
        })], { type: 'application/json' }));

        if (logoFile) {
            formData.append('logo', logoFile);
        }

        try {
            const response = await fetch(`${API_BASE_URL}/shops`, {
                method: 'POST',
                headers: getAuthHeaders(null), // Pass null for Content-Type to let browser set multipart boundary
                body: formData,
            });

            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Shop created successfully!');
                setName('');
                setLocation('');
                setDescription('');
                // setSelectedUserId(users.length > 0 ? users[0].id.toString() : ''); // Removed
                setLogoFile(null);
                onShopCreated();
            } else {
                setError(data.message || 'Failed to create shop.');
            }
        } catch (err: any) {
            setError(`Network error: Could not create shop. Check backend logs.`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <button
                onClick={onBack}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Shops List
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Add New Shop</h2>
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />
            {/* <MessageDisplay message={dataError} type="error" /> Removed */}

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

                {/* Removed the entire user selection dropdown */}
                {/* <div>
                    <label htmlFor="user-select" className="block text-gray-700 text-sm font-bold mb-2">Shop Owner (User ID)</label>
                    <select
                        id="user-select"
                        value={selectedUserId}
                        onChange={(e) => setSelectedUserId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || users.length === 0}
                    >
                        {dataLoading && <option value="">Loading users...</option>}
                        {!dataLoading && users.length === 0 && <option value="">No users available</option>}
                        {users.map(user => (
                            <option key={user.id} value={user.id.toString()}>
                                {user.email} (ID: {user.id})
                            </option>
                        ))}
                    </select>
                </div> */}

                <div>
                    <label className="block text-gray-700 text-sm font-bold mb-2">Shop Logo (Optional)</label>
                    <input
                        type="file"
                        name="logo"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full p-3 border border-gray-300 rounded-md"
                    />
                </div>
                <button
                    type="submit"
                    className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 px-6 rounded-md shadow-md transition-all duration-200"
                    disabled={loading /* || dataLoading || users.length === 0 Removed */}
                >
                    {loading ? 'Creating...' : 'Create Shop'}
                </button>
            </form>
        </div>
    );
};

// --- ShopUpdateForm Component ---
interface ShopUpdateFormProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    initialShop: Shop;
    onShopUpdated: () => void;
    onBack: () => void;
}

const ShopUpdateForm: React.FC<ShopUpdateFormProps> = ({ API_BASE_URL, authToken, getAuthHeaders, initialShop, onShopUpdated, onBack }) => {
    const [name, setName] = useState(initialShop.name);
    const [location, setLocation] = useState(initialShop.location);
    const [description, setDescription] = useState(initialShop.description);
    // Removed selectedUserId state
    const [logoFile, setLogoFile] = useState<File | null>(null);
    const [currentLogoUrl, setCurrentLogoUrl] = useState(initialShop.logoUrl);

    // Removed users state and fetchUsers useEffect
    // const [users, setUsers] = useState<User[]>([]);

    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    // dataLoading and dataError related to fetching users are no longer needed
    // const [dataLoading, setDataLoading] = useState(true);
    // const [dataError, setDataError] = useState<string | null>(null);

    // useEffect(() => {
    //     const fetchUsers = async () => { /* ... */ };
    //     fetchUsers();
    // }, [API_BASE_URL, getAuthHeaders, initialShop.userId]); // This useEffect is removed

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setLogoFile(e.target.files[0]);
            // @ts-ignore
            setCurrentLogoUrl(null);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccessMessage(null);

        // No longer check for selectedUserId as it's not provided by the client
        // if (!selectedUserId) {
        //     setError("Please select a shop owner (User ID).");
        //     setLoading(false);
        //     return;
        // }

        const formData = new FormData();
        formData.append('shop', new Blob([JSON.stringify({
            id: initialShop.id,
            name,
            location,
            description,
            // userId is explicitly excluded here, backend will derive it for updates too if designed that way
        })], { type: 'application/json' }));

        if (logoFile) {
            formData.append('logo', logoFile);
        }

        try {
            const response = await fetch(`${API_BASE_URL}/shops/${initialShop.id}`, {
                method: 'PUT',
                headers: getAuthHeaders(null), // Pass null for Content-Type to let browser set multipart boundary
                body: formData,
            });

            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Shop updated successfully!');
                onShopUpdated();
            } else {
                setError(data.message || 'Failed to update shop.');
            }
        } catch (err: any) {
            setError(`Network error: Could not update shop. Check backend logs.`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <button
                onClick={onBack}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Shops List
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Edit Shop: {initialShop.name}</h2>
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />
            {/* <MessageDisplay message={dataError} type="error" /> Removed */}

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

                {/* Removed the entire user selection dropdown for update as well */}
                {/* <div>
                    <label htmlFor="edit-user-select" className="block text-gray-700 text-sm font-bold mb-2">Shop Owner (User ID)</label>
                    <select
                        id="edit-user-select"
                        value={selectedUserId}
                        onChange={(e) => setSelectedUserId(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md"
                        required
                        disabled={dataLoading || users.length === 0}
                    >
                        {dataLoading && <option value="">Loading users...</option>}
                        {!dataLoading && users.length === 0 && <option value="">No users available</option>}
                        {users.map(user => (
                            <option key={user.id} value={user.id.toString()}>
                                {user.email} (ID: {user.id})
                            </option>
                        ))}
                    </select>
                </div> */}

                <div>
                    <label className="block text-gray-700 text-sm font-bold mb-2">Shop Logo (Optional)</label>
                    <input
                        type="file"
                        name="logo"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="w-full p-3 border border-gray-300 rounded-md"
                    />
                    {currentLogoUrl && (
                        <p className="text-sm text-gray-500 mt-2">Current Logo: <a href={currentLogoUrl} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:underline">View Logo</a></p>
                    )}
                    {logoFile && (
                        <p className="text-sm text-gray-500 mt-2">New Logo Selected: {logoFile.name}</p>
                    )}
                </div>
                <button
                    type="submit"
                    className="w-full bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-3 px-6 rounded-md shadow-md transition-all duration-200"
                    disabled={loading /* || dataLoading || users.length === 0 Removed */}
                >
                    {loading ? 'Updating...' : 'Update Shop'}
                </button>
            </form>
        </div>
    );
};

// --- ShopListSection Component ---
interface ShopListSectionProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onViewShopDetails: (shopId: number) => void;
    onCreateShopClick: () => void;
    onEditShopClick: (shop: Shop) => void;
}

const ShopListSection: React.FC<ShopListSectionProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onViewShopDetails, onCreateShopClick, onEditShopClick }) => {
    const [shops, setShops] = useState<Shop[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const fetchShops = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/shops/getall`, {
                headers: getAuthHeaders(),
            });
            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setShops(data.shops || []);
            } else {
                setError(data.message || 'Failed to fetch shops.');
            }
        } catch (err: any) {
            setError('Network error fetching shops.');
        } finally {
            setLoading(false);
        }
    }, [API_BASE_URL, getAuthHeaders]);

    useEffect(() => {
        fetchShops();
    }, [fetchShops]);

    const handleDelete = async (id: number) => {
        if (!window.confirm('Are you sure you want to delete this shop?')) return;
        setLoading(true);
        setError(null);
        setSuccessMessage(null);
        try {
            const response = await fetch(`${API_BASE_URL}/shops/${id}`, {
                method: 'DELETE',
                headers: getAuthHeaders(),
            });
            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setSuccessMessage(data.message || 'Shop deleted successfully!');
                fetchShops();
            } else {
                setError(data.message || 'Failed to delete shop.');
            }
        } catch (err: any) {
            setError('Network error deleting shop.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <h2 className="text-3xl font-bold text-gray-800 mb-6">Shop Management</h2>
            <MessageDisplay message={error} type="error" />
            <MessageDisplay message={successMessage} type="success" />

            <div className="mb-6">
                <button
                    onClick={onCreateShopClick}
                    className="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-md shadow-md transition-all duration-200"
                >
                    Add New Shop
                </button>
            </div>

            <h3 className="text-2xl font-bold text-gray-800 mb-4">Existing Shops</h3>
            {loading && !shops.length && <MessageDisplay message="Loading shops..." type="info" />}
            {!loading && shops.length === 0 && <MessageDisplay message="No shops to display." type="info" />}
            <div className="overflow-x-auto">
                <table className="min-w-full bg-white rounded-lg shadow-md">
                    <thead className="bg-gray-200">
                    <tr>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Location</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Owner ID</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Logo</th>
                        <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {shops.map(shop => (
                        <tr
                            key={shop.id}
                            className="border-b border-gray-200 hover:bg-gray-50 cursor-pointer"
                            onClick={() => onViewShopDetails(shop.id)}
                        >
                            <td className="py-3 px-4 text-sm text-gray-700">{shop.id}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">{shop.name}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">{shop.location}</td>
                            <td className="py-3 px-4 text-sm text-gray-700">{shop.userId}</td>
                            <td className="py-3 px-4">
                                {shop.logoUrl && (
                                    <img src={shop.logoUrl} alt="Shop Logo" className="w-16 h-12 object-cover rounded" />
                                )}
                            </td>
                            <td className="py-3 px-4 text-sm text-gray-700 space-x-2">
                                <button
                                    onClick={(e) => { e.stopPropagation(); onEditShopClick(shop); }}
                                    className="bg-blue-500 hover:bg-blue-600 text-white p-2 rounded-md transition duration-200"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={(e) => { e.stopPropagation(); handleDelete(shop.id); }}
                                    className="bg-red-500 hover:bg-red-600 text-white p-2 rounded-md transition duration-200"
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

// --- ShopPageView Component ---
interface ShopPageViewProps {
    API_BASE_URL: string;
    shopId: number;
    onBack: () => void;
}

const ShopPageView: React.FC<ShopPageViewProps> = ({ API_BASE_URL, shopId, onBack }) => {
    const [shop, setShop] = useState<Shop | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchShopDetails = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/shops/${shopId}`);
            const data: ApiResponse = await response.json();

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
        if (shopId) {
            void fetchShopDetails();
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
                    </div>

                    <button className="mt-8 bg-purple-500 hover:bg-purple-600 text-white font-semibold py-3 px-6 rounded-md shadow-lg transition-all duration-200 transform hover:scale-105 active:scale-95">
                        View Shop Products
                    </button>
                </div>
            </div>
        </div>
    );
};

// --- ShopManagementView Component ---
interface ShopManagementViewProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onBackToAdminDashboard: () => void;
}

const ShopManagementView: React.FC<ShopManagementViewProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onBackToAdminDashboard }) => {
    const [currentSubView, setCurrentSubView] = useState<'list' | 'create' | 'edit' | 'detail'>('list');
    const [selectedShopId, setSelectedShopId] = useState<number | null>(null);
    const [selectedShopToEdit, setSelectedShopToEdit] = useState<Shop | null>(null);

    const handleCreateShopClick = () => setCurrentSubView('create');
    const handleShopCreated = () => setCurrentSubView('list');
    const handleViewShopDetails = (shopId: number) => {
        setSelectedShopId(shopId);
        setCurrentSubView('detail');
    };
    const handleEditShopClick = (shop: Shop) => {
        setSelectedShopToEdit(shop);
        setCurrentSubView('edit');
    };
    const handleShopUpdated = () => {
        setSelectedShopToEdit(null);
        setCurrentSubView('list');
    };
    const handleBackToList = () => {
        setSelectedShopId(null);
        setSelectedShopToEdit(null);
        setCurrentSubView('list');
    };

    const renderShopContent = () => {
        switch (currentSubView) {
            case 'list':
                return (
                    <ShopListSection
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onCreateShopClick={handleCreateShopClick}
                        onViewShopDetails={handleViewShopDetails}
                        onEditShopClick={handleEditShopClick}
                    />
                );
            case 'create':
                return (
                    <ShopCreationForm
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onShopCreated={handleShopCreated}
                        onBack={handleBackToList}
                    />
                );
            case 'edit':
                if (selectedShopToEdit === null) {
                    return <MessageDisplay message="No shop selected for editing." type="error" />;
                }
                return (
                    <ShopUpdateForm
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        initialShop={selectedShopToEdit}
                        onShopUpdated={handleShopUpdated}
                        onBack={handleBackToList}
                    />
                );
            case 'detail':
                if (selectedShopId === null) {
                    return <MessageDisplay message="No shop selected to view details." type="error" />;
                }
                return (
                    <ShopPageView
                        API_BASE_URL={API_BASE_URL}
                        shopId={selectedShopId}
                        onBack={handleBackToList}
                    />
                );
            default:
                return <MessageDisplay message="Invalid shop management view." type="error" />;
        }
    };

    return (
        <div className="p-4">
            <button
                onClick={onBackToAdminDashboard}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Dashboard
            </button>
            {renderShopContent()}
        </div>
    );
};

// --- UserManagementView Component ---
interface UserManagementViewProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onBackToAdminDashboard: () => void;
}

const UserManagementView: React.FC<UserManagementViewProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onBackToAdminDashboard }) => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [message, setMessage] = useState("User management functionality is under construction. Admin users would typically see a list of all registered users here, with options to view, edit roles, or deactivate accounts.");

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/users`, {
                headers: getAuthHeaders(),
            });
            const data: ApiResponse = await response.json();
            if (response.ok && data.success) {
                setUsers(data.users || []);
            } else {
                setError(data.message || 'Failed to fetch users.');
            }
        } catch (err: any) {
            setError('Network error fetching users.');
        } finally {
            setLoading(false);
        }
    }, [API_BASE_URL, getAuthHeaders]);

    useEffect(() => {
        // This useEffect is uncommented to enable fetching users for display.
        fetchUsers();
    }, [fetchUsers]);

    return (
        <div className="p-4">
            <button
                onClick={onBackToAdminDashboard}
                className="mb-6 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 flex items-center gap-2"
            >
                <ArrowLeft size={20} /> Back to Dashboard
            </button>
            <h2 className="text-3xl font-bold text-gray-800 mb-6">User Management</h2>
            <MessageDisplay message={message} type="info" />
            {loading && <MessageDisplay message="Loading users..." type="info" />}
            <MessageDisplay message={error} type="error" />
            {users.length > 0 ? (
                <div className="overflow-x-auto">
                    <table className="min-w-full bg-white rounded-lg shadow-md">
                        <thead className="bg-gray-200">
                        <tr>
                            <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                            <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Email</th>
                            <th className="py-3 px-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Roles</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map(user => (
                            <tr key={user.id} className="border-b border-gray-200 hover:bg-gray-50">
                                <td className="py-3 px-4 text-sm text-gray-700">{user.id}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{user.email}</td>
                                <td className="py-3 px-4 text-sm text-gray-700">{user.roles?.join(', ') || 'N/A'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                !loading && <MessageDisplay message="No users to display." type="info" />
            )}
        </div>
    );
};


// --- AdminDashboardView Component ---
interface AdminDashboardViewProps {
    API_BASE_URL: string;
    authToken: string | null;
    getAuthHeaders: (contentType?: string | null) => HeadersInit;
    onLogout: () => void;
    userRole: string;
}

const AdminDashboardView: React.FC<AdminDashboardViewProps> = ({ API_BASE_URL, authToken, getAuthHeaders, onLogout, userRole }) => {
    // State to manage which top-level admin view is active
    const [currentAdminView, setCurrentAdminView] = useState<'products' | 'shops' | 'users'>('products');

    const renderAdminContent = () => {
        switch (currentAdminView) {
            case 'products':
                return (
                    <ProductManagementView
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onBackToAdminDashboard={() => setCurrentAdminView('products')} // Stays on current view after back (as it's a sub-router)
                    />
                );
            case 'shops':
                return (
                    <ShopManagementView
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onBackToAdminDashboard={() => setCurrentAdminView('shops')} // Stays on current view after back
                    />
                );
            case 'users':
                return (
                    <UserManagementView
                        API_BASE_URL={API_BASE_URL}
                        authToken={authToken}
                        getAuthHeaders={getAuthHeaders}
                        onBackToAdminDashboard={() => setCurrentAdminView('users')} // Stays on current view after back
                    />
                );
            default:
                return (
                    <MessageDisplay message="Invalid admin view selected." type="error" />
                );
        }
    };

    return (
        <div className="relative">
            <div className="flex justify-between items-center mb-8 p-4 bg-white rounded-t-2xl shadow-sm">
                <h1 className="text-4xl font-extrabold text-gray-800 tracking-tight">
                    <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-700">
                        Admin Dashboard
                    </span>
                </h1>
                <div className="flex items-center gap-4">
                    <span className="text-gray-600 text-sm">Role: {userRole.toUpperCase()}</span>
                    <button
                        onClick={onLogout}
                        className="bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded-full shadow-md transition-all duration-200 transform hover:scale-105 active:scale-95 flex items-center gap-2"
                    >
                        <LogOut size={20} /> Logout
                    </button>
                </div>
            </div>

            <div className="mb-8 border-b border-gray-200 p-4 bg-white shadow-sm">
                <nav className="-mb-px flex space-x-8" aria-label="Tabs">
                    <button
                        onClick={() => setCurrentAdminView('products')}
                        className={`${currentAdminView === 'products' ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'} whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200 flex items-center gap-2`}
                    >
                        <Package size={20} /> Product Management
                    </button>
                    <button
                        onClick={() => setCurrentAdminView('shops')}
                        className={`${currentAdminView === 'shops' ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'} whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200 flex items-center gap-2`}
                    >
                        <Store size={20} /> Shop Management
                    </button>
                    <button
                        onClick={() => setCurrentAdminView('users')}
                        className={`${currentAdminView === 'users' ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'} whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200 flex items-center gap-2`}
                    >
                        <UserIcon size={20} /> User Management
                    </button>
                </nav>
            </div>

            {renderAdminContent()}
        </div>
    );
};

export default AdminDashboardView; // Export the AdminDashboardView component
