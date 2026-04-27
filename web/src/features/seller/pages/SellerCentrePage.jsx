import { useState } from 'react';
import { useAuth } from '../../auth/context/AuthContext';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import {
    Leaf, LogOut, Package, User, Settings,
    Flower2, CreditCard, MapPin, Heart, Edit2, Shield, Gift, Plus, Search, ArrowRight, ChevronDown, Store, AlertCircle
} from 'lucide-react';
import KpiCard from '../components/KpiCard';

export default function SellerCentre() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Check query params for initial tab (e.g., ?tab=shopInfo)
    const searchParams = new URLSearchParams(location.search);
    const initialTab = searchParams.get('tab') || 'overview';

    // Artisan Shop States
    const [artisanTab, setArtisanTab] = useState(initialTab);
    const [orderTab, setOrderTab] = useState('all');
    const [productTab, setProductTab] = useState('live');
    const [isEditingShop, setIsEditingShop] = useState(false);

    // Mock Shop Data
    const [shopBio, setShopBio] = useState("We curate premium, locally-sourced floral arrangements for every occasion. Our focus is on sustainable aesthetics and timeless design.");
    const [shopName, setShopName] = useState(user?.name ? `${user.name}'s Studio` : 'My Floral Studio');

    // Mock Products Data (Reused from previous Profile)
    const products = [
        { id: 1, name: "Wild Pampas", subtitle: "Dried Reed Grass", price: "$45", artisan: "Atelier Vert", image: "/images/product_pampas_1771726515735.png", tag: "Bestseller" },
        { id: 2, name: "Eucalyptus Cinerea", subtitle: "Preserved Foliage", price: "$28", artisan: "Maison Fleuri", image: "/images/product_eucalyptus_1771726530879.png" },
        { id: 3, name: "Cotton Softness", subtitle: "Natural Cotton Stems", price: "$32", artisan: "Studio Petal", image: "/images/product_cotton_1771726545396.png" },
        { id: 4, name: "Kanso Vase", subtitle: "Artisan Ceramic", price: "$55", artisan: "Ceramics by Jo", image: "/images/product_ceramic_vase_1771726567287.png" },
        { id: 5, name: "The Aurora", subtitle: "Hydrangea & Immortelle", price: "$49", artisan: "L'Herbier", image: "/images/product_aurora_hydrangea_1771726583839.png" },
        { id: 6, name: "Winter Wreath", subtitle: "Pine & Berries", price: "$65", artisan: "Forest & Co.", image: "/images/product_winter_wreath_1771726603408.png", tag: "Unique Piece" },
    ];

    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';
    const initials = shopName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Protect route: redirect if not an artisan
    if (user && !isArtisan) {
        navigate('/dashboard');
        return null; // Or a loading spinner
    }

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900 font-sans">
            {/* ─── NAVIGATION ─── */}
            <nav className="fixed top-0 w-full z-50 bg-[#FDFCF8]/90 backdrop-blur-md border-b border-stone-200 py-4">
                <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <Link to="/dashboard" className="text-xl font-serif tracking-tight text-stone-900">
                            Petal
                        </Link>
                        <span className="text-stone-300">|</span>
                        <span className="text-sm font-medium text-stone-600">Seller Centre</span>
                    </div>

                    <div className="flex items-center gap-6">
                        <Link to="/seller-education" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Seller Education
                        </Link>
                        <Link to="/dashboard" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Return to Shop
                        </Link>
                        <button onClick={handleLogout} className="text-sm font-medium text-stone-900 hover:text-red-700 transition-colors">
                            Sign Out
                        </button>
                    </div>
                </div>
            </nav>

            <main className="pt-32 pb-24 max-w-7xl mx-auto px-6 min-h-[80vh]">
                <div className="mb-8 flex justify-between items-end">
                    <div>
                        <h1 className="text-3xl font-serif text-stone-900">Seller Centre</h1>
                        <p className="text-stone-500 text-sm mt-1">Manage your shop, products, and orders.</p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                    {/* ─── SIDEBAR ─── */}
                    <aside className="lg:col-span-3">
                        <div className="bg-white border border-stone-200 rounded-sm p-4 sticky top-24">
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Dashboard</h3>
                                <ul className="space-y-1">
                                    <li>
                                        <button onClick={() => setArtisanTab('overview')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'overview' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>Overview</button>
                                    </li>
                                </ul>
                            </div>
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Orders</h3>
                                <ul className="space-y-1">
                                    <li><button onClick={() => setArtisanTab('orders')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'orders' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>My Orders</button></li>
                                    <li><button className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">Cancellations</button></li>
                                    <li><button className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">Return / Refund</button></li>
                                </ul>
                            </div>
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Products</h3>
                                <ul className="space-y-1">
                                    <li><button onClick={() => setArtisanTab('products')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'products' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>My Products</button></li>
                                    <li><button className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">Add New Product</button></li>
                                </ul>
                            </div>
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Shop</h3>
                                <ul className="space-y-1">
                                    <li><button onClick={() => setArtisanTab('shopInfo')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'shopInfo' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>Shop Information</button></li>
                                    <li><button className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">Shop Setting</button></li>
                                </ul>
                            </div>
                            <div>
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Finance</h3>
                                <ul className="space-y-1">
                                    <li><button className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">My Income</button></li>
                                    <li><button className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">Bank Accounts</button></li>
                                </ul>
                            </div>
                        </div>
                    </aside>

                    {/* ─── MAIN CONTENT ─── */}
                    <div className="lg:col-span-9 space-y-6">

                        {/* ─── OVERVIEW TAB ─── */}
                        {artisanTab === 'overview' && (
                            <>
                                {/* To Do List */}
                                <div className="bg-white border border-stone-200 rounded-sm p-6">
                                    <h3 className="text-lg font-serif text-stone-900 mb-6 font-medium">To Do List</h3>
                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                                        <div className="text-center group cursor-pointer">
                                            <p className="text-2xl font-light text-stone-900 group-hover:text-amber-600 transition-colors">0</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Unpaid</p>
                                        </div>
                                        <div className="text-center group cursor-pointer border-l border-stone-100">
                                            <p className="text-2xl font-light text-stone-900 group-hover:text-amber-600 transition-colors">3</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">To Process Shipment</p>
                                        </div>
                                        <div className="text-center group cursor-pointer border-l border-stone-100">
                                            <p className="text-2xl font-light text-stone-900 group-hover:text-amber-600 transition-colors">0</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Pending Return</p>
                                        </div>
                                        <div className="text-center group cursor-pointer border-l border-stone-100">
                                            <p className="text-2xl font-light text-stone-900 group-hover:text-amber-600 transition-colors">1</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Sold Out</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Business Insights */}
                                <div className="bg-white border border-stone-200 rounded-sm p-6">
                                    <div className="flex justify-between items-center mb-6">
                                        <h3 className="text-lg font-serif text-stone-900 font-medium">Business Insights</h3>
                                        <button className="text-xs text-stone-500 hover:text-stone-900 flex items-center gap-1 transition-colors">More <ArrowRight size={14} /></button>
                                    </div>
                                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                        <KpiCard title="Visitors" value="124" />
                                        <KpiCard title="Page Views" value="459" />
                                        <KpiCard title="Orders" value="4" />
                                    </div>
                                </div>
                            </>
                        )}

                        {/* ─── ORDERS TAB ─── */}
                        {artisanTab === 'orders' && (
                            <div className="bg-white border border-stone-200 rounded-sm">
                                <div className="border-b border-stone-200">
                                    <nav className="flex px-2 overflow-x-auto" aria-label="Tabs">
                                        {['All', 'Unpaid', 'To Ship', 'Shipping', 'Completed', 'Cancelled'].map((tab) => (
                                            <button
                                                key={tab}
                                                onClick={() => setOrderTab(tab.toLowerCase())}
                                                className={`whitespace-nowrap py-4 px-6 text-sm font-medium border-b-2 transition-colors ${orderTab === tab.toLowerCase()
                                                    ? 'border-stone-900 text-stone-900'
                                                    : 'border-transparent text-stone-500 hover:text-stone-700 hover:border-stone-300'
                                                    }`}
                                            >
                                                {tab}
                                            </button>
                                        ))}
                                    </nav>
                                </div>
                                <div className="p-6">
                                    <div className="flex gap-4 mb-6">
                                        <div className="flex-1 relative">
                                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400" size={16} />
                                            <input type="text" placeholder="Search Order ID or Buyer Name" className="w-full pl-10 pr-4 py-2 text-sm border border-stone-200 rounded-sm focus:outline-none focus:border-stone-400 bg-stone-50" />
                                        </div>
                                        <button className="px-4 py-2 bg-stone-900 text-white text-sm font-medium rounded-sm hover:bg-stone-800 transition-colors">Search</button>
                                    </div>

                                    {/* Orders Table */}
                                    <div className="overflow-x-auto">
                                        <table className="w-full text-sm text-left">
                                            <thead className="text-xs text-stone-500 uppercase bg-stone-50 border-y border-stone-200">
                                                <tr>
                                                    <th className="px-4 py-3 font-medium">Products</th>
                                                    <th className="px-4 py-3 font-medium text-center">Total Price</th>
                                                    <th className="px-4 py-3 font-medium text-center">Status</th>
                                                    <th className="px-4 py-3 font-medium text-center">Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr className="border-b border-stone-100">
                                                    <td className="px-4 py-4 min-w-[250px]">
                                                        <div className="flex gap-3 items-start">
                                                            <img src={products[0].image} className="w-12 h-12 object-cover border border-stone-200 rounded-sm flex-shrink-0" alt="" />
                                                            <div>
                                                                <p className="text-stone-900 font-medium">{products[0].name}</p>
                                                                <p className="text-stone-500 text-xs">x1</p>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-4 text-center font-medium text-stone-900 whitespace-nowrap">{products[0].price}</td>
                                                    <td className="px-4 py-4 text-center">
                                                        <span className="bg-amber-100 text-amber-800 text-[10px] uppercase font-bold px-2 py-1 rounded-sm whitespace-nowrap">To Ship</span>
                                                    </td>
                                                    <td className="px-4 py-4 text-center">
                                                        <button className="text-sm text-stone-600 border border-stone-300 px-3 py-1.5 rounded-sm hover:bg-stone-50 transition-colors whitespace-nowrap">Arrange Shipment</button>
                                                    </td>
                                                </tr>
                                                <tr className="border-b border-stone-100">
                                                    <td className="px-4 py-4 min-w-[250px]">
                                                        <div className="flex gap-3 items-start">
                                                            <img src={products[3].image} className="w-12 h-12 object-cover border border-stone-200 rounded-sm flex-shrink-0" alt="" />
                                                            <div>
                                                                <p className="text-stone-900 font-medium">{products[3].name}</p>
                                                                <p className="text-stone-500 text-xs">x2</p>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-4 text-center font-medium text-stone-900 whitespace-nowrap">$110</td>
                                                    <td className="px-4 py-4 text-center">
                                                        <span className="bg-blue-100 text-blue-800 text-[10px] uppercase font-bold px-2 py-1 rounded-sm whitespace-nowrap">Shipping</span>
                                                    </td>
                                                    <td className="px-4 py-4 text-center">
                                                        <button className="text-sm text-stone-600 hover:text-stone-900 transition-colors border-b border-stone-300 hover:border-stone-900 pb-0.5 whitespace-nowrap">Check Logistics</button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* ─── PRODUCTS TAB ─── */}
                        {artisanTab === 'products' && (
                            <div className="bg-white border border-stone-200 rounded-sm">
                                <div className="border-b border-stone-200 flex flex-col sm:flex-row justify-between sm:items-center pr-6 gap-4 sm:gap-0">
                                    <nav className="flex px-2 overflow-x-auto" aria-label="Tabs">
                                        {['All', 'Live', 'Sold Out', 'Reviewing', 'Violation'].map((tab) => (
                                            <button
                                                key={tab}
                                                onClick={() => setProductTab(tab.toLowerCase())}
                                                className={`whitespace-nowrap py-4 px-6 text-sm font-medium border-b-2 transition-colors ${productTab === tab.toLowerCase()
                                                    ? 'border-stone-900 text-stone-900'
                                                    : 'border-transparent text-stone-500 hover:text-stone-700 hover:border-stone-300'
                                                    }`}
                                            >
                                                {tab} {tab === 'Live' && '(5)'} {tab === 'Sold Out' && '(1)'}
                                            </button>
                                        ))}
                                    </nav>
                                    <div className="px-6 pb-4 sm:p-0 sm:pl-4 self-start sm:self-center">
                                        <button className="flex items-center gap-2 bg-stone-900 text-white px-4 py-2 rounded-sm text-sm font-medium hover:bg-stone-800 transition-colors whitespace-nowrap">
                                            <Plus size={16} /> Add New Product
                                        </button>
                                    </div>
                                </div>
                                <div className="p-6">
                                    <div className="flex gap-4 mb-6">
                                        <div className="flex-1 relative">
                                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400" size={16} />
                                            <input type="text" placeholder="Search product name or SKU" className="w-full pl-10 pr-4 py-2 text-sm border border-stone-200 rounded-sm focus:outline-none focus:border-stone-400 bg-stone-50" />
                                        </div>
                                        <button className="px-4 py-2 bg-stone-100 text-stone-900 border border-stone-200 text-sm font-medium rounded-sm hover:bg-stone-200 transition-colors whitespace-nowrap">Category <ChevronDown size={14} className="inline ml-1" /></button>
                                    </div>

                                    {/* Products Table */}
                                    <div className="overflow-x-auto">
                                        <table className="w-full text-sm text-left">
                                            <thead className="text-xs text-stone-500 uppercase bg-stone-50 border-y border-stone-200">
                                                <tr>
                                                    <th className="px-4 py-3 font-medium">Product Name</th>
                                                    <th className="px-4 py-3 font-medium text-center">Price</th>
                                                    <th className="px-4 py-3 font-medium text-center">Stock</th>
                                                    <th className="px-4 py-3 font-medium text-center">Sales</th>
                                                    <th className="px-4 py-3 font-medium text-center">Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {products.map((product) => (
                                                    <tr key={product.id} className="border-b border-stone-100 hover:bg-stone-50/50 transition-colors">
                                                        <td className="px-4 py-4 min-w-[250px]">
                                                            <div className="flex gap-3 items-start">
                                                                <img src={product.image} className="w-12 h-12 object-cover border border-stone-200 rounded-sm flex-shrink-0" alt="" />
                                                                <div>
                                                                    <p className="text-stone-900 font-medium line-clamp-2">{product.name}</p>
                                                                    <p className="text-stone-500 text-xs mt-1 border border-stone-200 inline-block px-1 bg-white">SKU: {product.name.substring(0, 3).toUpperCase()}-00{product.id}</p>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td className="px-4 py-4 text-center font-medium text-stone-900 whitespace-nowrap">{product.price}</td>
                                                        <td className="px-4 py-4 text-center">
                                                            {product.id === 2 ? <span className="text-red-500 font-medium">0</span> : <span className="text-stone-900">{Math.floor(Math.random() * 50) + 5}</span>}
                                                        </td>
                                                        <td className="px-4 py-4 text-center text-stone-500">
                                                            {Math.floor(Math.random() * 20)}
                                                        </td>
                                                        <td className="px-4 py-4 text-center">
                                                            <div className="flex items-center justify-center gap-3">
                                                                <button className="text-sm text-stone-600 hover:text-stone-900 transition-colors font-medium">Edit</button>
                                                                <button className="text-sm text-stone-400 hover:text-stone-900 transition-colors">More</button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* ─── SHOP INFO TAB ─── */}
                        {artisanTab === 'shopInfo' && (
                            <div className="bg-white border border-stone-200 rounded-sm p-6 lg:p-8">
                                <div className="flex items-center justify-between mb-8 pb-4 border-b border-stone-100">
                                    <div>
                                        <h3 className="text-xl font-serif text-stone-900">Shop Information</h3>
                                        <p className="text-stone-500 text-sm mt-1">Manage your studio's public-facing profile and branding.</p>
                                    </div>
                                    <button
                                        onClick={() => setIsEditingShop(!isEditingShop)}
                                        className="text-xs uppercase tracking-widest text-stone-500 hover:text-stone-900 flex items-center gap-2 transition-colors border border-stone-200 px-4 py-2 rounded-sm hover:bg-stone-50"
                                    >
                                        <Edit2 size={14} /> {isEditingShop ? 'Cancel' : 'Edit Profile'}
                                    </button>
                                </div>

                                <div className="flex flex-col md:flex-row gap-10">
                                    <div className="flex-1 space-y-8">
                                        <div className="space-y-2">
                                            <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Shop Name</label>
                                            {isEditingShop ? (
                                                <input
                                                    type="text"
                                                    value={shopName}
                                                    onChange={(e) => setShopName(e.target.value)}
                                                    className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                />
                                            ) : (
                                                <p className="text-stone-800 text-lg font-medium">{shopName}</p>
                                            )}
                                        </div>

                                        <div className="space-y-2">
                                            <label className="text-xs uppercase tracking-wider text-stone-400 font-medium flex items-center gap-2">
                                                Studio Bio
                                                <AlertCircle size={14} className="text-stone-300 cursor-help" />
                                            </label>
                                            {isEditingShop ? (
                                                <textarea
                                                    rows={4}
                                                    value={shopBio}
                                                    onChange={(e) => setShopBio(e.target.value)}
                                                    className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                />
                                            ) : (
                                                <p className="text-stone-600 font-light leading-relaxed">{shopBio}</p>
                                            )}
                                        </div>

                                        {isEditingShop && (
                                            <div className="pt-4 flex justify-end">
                                                <button
                                                    onClick={() => setIsEditingShop(false)}
                                                    className="bg-stone-900 text-white px-6 py-2.5 text-sm font-medium hover:bg-stone-800 transition-colors rounded-sm"
                                                >
                                                    Save Shop Information
                                                </button>
                                            </div>
                                        )}
                                    </div>

                                    {/* Avatar / Logo Section */}
                                    <div className="md:w-64 space-y-6 md:border-l md:border-stone-100 md:pl-10">
                                        <div className="space-y-4 text-center">
                                            <div className="w-32 h-32 mx-auto bg-stone-50 rounded-full flex items-center justify-center text-4xl font-serif italic text-stone-700 border-2 border-dashed border-stone-200 relative group overflow-hidden">
                                                <span>{initials}</span>
                                                {isEditingShop && (
                                                    <div className="absolute inset-0 bg-stone-900/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                                                        <span className="text-white text-xs uppercase tracking-widest font-medium">Upload</span>
                                                    </div>
                                                )}
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium text-stone-900">Studio Mark / Logo</p>
                                                <p className="text-xs text-stone-500 mt-1 px-4 leading-relaxed">JPEG, PNG. Recommended 500x500px.</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                    </div>
                </div>
            </main>
        </div>
    );
}
