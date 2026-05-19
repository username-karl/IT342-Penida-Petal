import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import {
    AlertCircle, CheckCircle2, Gift, Heart, Leaf, MapPin, PackageCheck, Plus, Shield, Star, Trash2, Truck, Edit2
} from 'lucide-react';
import { addressesAPI, ordersAPI } from '../services/api';

function currency(value) {
    const number = Number(value || 0);
    return `PHP ${number.toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function statusLabel(status) {
    const labels = {
        PENDING: 'Order Placed',
        PREPARING: 'Being Prepared',
        READY_FOR_PICKUP: 'Ready for Pickup',
        COMPLETED: 'Delivered',
        CANCELLED: 'Cancelled',
    };
    return labels[status] || status;
}

function statusClass(status) {
    if (status === 'COMPLETED' || status === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (status === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (status === 'PREPARING') return 'bg-amber-50 text-amber-800 border-amber-200';
    return 'bg-rose-50 text-rose-800 border-rose-200';
}

function paymentLabel(value) {
    const labels = {
        COD: 'Cash on Delivery',
        GCASH: 'GCash',
        MAYA: 'Maya',
        CARD: 'Card',
    };
    return labels[value] || value;
}

function deliveryLabel(order) {
    if (!order.deliveryDate) return order.timeSlot || 'Delivery pending';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} · ${order.timeSlot}`;
}

export default function Profile() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [isEditing, setIsEditing] = useState(false);
    const [orders, setOrders] = useState([]);
    const [ordersLoading, setOrdersLoading] = useState(true);
    const [ordersError, setOrdersError] = useState('');
    const [addresses, setAddresses] = useState([]);
    const [addressesLoading, setAddressesLoading] = useState(true);
    const [addressesError, setAddressesError] = useState('');
    const [addressForm, setAddressForm] = useState({
        label: 'Home',
        recipientName: '',
        phoneNumber: '',
        addressLine: '',
        defaultAddress: false,
    });

    const displayName = user?.name || 'Guest';
    const email = user?.email || 'guest@example.com';
    const role = user?.role || 'Customer';
    const initials = displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';

    useEffect(() => {
        const loadOrders = async () => {
            setOrdersLoading(true);
            setOrdersError('');
            try {
                const response = await ordersAPI.getBuyerOrders();
                setOrders(response.data.data || []);
            } catch (err) {
                setOrdersError(err.response?.data?.message || err.message || 'Unable to load order history');
            } finally {
                setOrdersLoading(false);
            }
        };

        if (user && !isArtisan) {
            loadOrders();
        } else {
            setOrdersLoading(false);
        }
    }, [isArtisan, user]);

    useEffect(() => {
        const loadAddresses = async () => {
            setAddressesLoading(true);
            setAddressesError('');
            try {
                const response = await addressesAPI.getAddresses();
                setAddresses(response.data.data || []);
            } catch (err) {
                setAddressesError(err.response?.data?.message || err.message || 'Unable to load saved addresses');
            } finally {
                setAddressesLoading(false);
            }
        };

        if (user && !isArtisan) {
            loadAddresses();
        } else {
            setAddressesLoading(false);
        }
    }, [isArtisan, user]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const updateAddressForm = (field, value) => {
        setAddressForm((current) => ({ ...current, [field]: value }));
    };

    const saveAddress = async (event) => {
        event.preventDefault();
        setAddressesError('');
        try {
            const response = await addressesAPI.createAddress(addressForm);
            const savedAddress = response.data.data;
            setAddresses((current) => {
                const next = addressForm.defaultAddress
                    ? current.map((address) => ({ ...address, defaultAddress: false }))
                    : current;
                return [savedAddress, ...next];
            });
            setAddressForm({
                label: 'Home',
                recipientName: '',
                phoneNumber: '',
                addressLine: '',
                defaultAddress: false,
            });
        } catch (err) {
            setAddressesError(err.response?.data?.message || err.message || 'Unable to save address');
        }
    };

    const makeDefaultAddress = async (address) => {
        setAddressesError('');
        try {
            const response = await addressesAPI.updateAddress(address.id, { ...address, defaultAddress: true });
            const updatedAddress = response.data.data;
            setAddresses((current) => current.map((item) => (
                item.id === updatedAddress.id
                    ? updatedAddress
                    : { ...item, defaultAddress: false }
            )));
        } catch (err) {
            setAddressesError(err.response?.data?.message || err.message || 'Unable to update address');
        }
    };

    const removeAddress = async (id) => {
        setAddressesError('');
        try {
            await addressesAPI.deleteAddress(id);
            setAddresses((current) => current.filter((address) => address.id !== id));
        } catch (err) {
            setAddressesError(err.response?.data?.message || err.message || 'Unable to remove address');
        }
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900 font-sans">
            {/* ─── NAVIGATION (Simplified for Profile) ─── */}
            <nav className="fixed top-0 w-full z-50 bg-[#FDFCF8]/90 backdrop-blur-md border-b border-stone-200 py-4">
                <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
                    <Link to="/dashboard" className="text-xl font-serif tracking-tight text-stone-900">
                        Petal
                    </Link>
                    <div className="flex items-center gap-6">
                        <Link to="/dashboard" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Return to Shop
                        </Link>
                        <button onClick={handleLogout} className="text-sm font-medium text-stone-900 hover:text-red-700 transition-colors">
                            Sign Out
                        </button>
                    </div>
                </div>
            </nav>

            <main className="pt-32 pb-24 max-w-5xl mx-auto px-6">

                {/* ─── HEADER ─── */}
                <header className="mb-16 text-center">
                    <div className="w-24 h-24 mx-auto bg-stone-100 rounded-full flex items-center justify-center text-3xl font-serif italic text-stone-800 mb-6 border border-stone-200">
                        {initials}
                    </div>
                    <h1 className="text-4xl md:text-5xl font-serif text-stone-900 mb-2">
                        {displayName}
                    </h1>
                    <p className="text-stone-500 font-light tracking-wide uppercase text-xs">
                        {role} • Member since 2026
                    </p>
                </header>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
                    {/* ─── LEFT: ACCOUNT DETAILS ─── */}
                    <div className="lg:col-span-2 space-y-12">

                        {/* Personal Information */}
                        <section>
                            <div className="flex items-center justify-between mb-6 border-b border-stone-200 pb-4">
                                <h3 className="text-2xl font-serif text-stone-900">Personal Information</h3>
                                <button onClick={() => setIsEditing(!isEditing)} className="text-xs uppercase tracking-widest text-stone-500 hover:text-stone-900 flex items-center gap-2 transition-colors">
                                    <Edit2 size={14} /> {isEditing ? 'Cancel' : 'Edit'}
                                </button>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-1">
                                    <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Full Name</label>
                                    {isEditing ? (
                                        <input type="text" defaultValue={displayName} className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors" />
                                    ) : (
                                        <p className="text-stone-800 text-lg font-light">{displayName}</p>
                                    )}
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Email Address</label>
                                    <p className="text-stone-800 text-lg font-light">{email}</p>
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Phone</label>
                                    {isEditing ? (
                                        <input type="tel" placeholder="+1 (555) 000-0000" className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors" />
                                    ) : (
                                        <p className="text-stone-400 text-lg font-light italic">Not provided</p>
                                    )}
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Location</label>
                                    {isEditing ? (
                                        <input type="text" placeholder="Paris, France" className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors" />
                                    ) : (
                                        <p className="text-stone-400 text-lg font-light italic">Not provided</p>
                                    )}
                                </div>
                            </div>

                            {isEditing && (
                                <div className="mt-6 flex justify-end">
                                    <button className="bg-stone-900 text-white px-6 py-2 text-sm font-medium hover:bg-stone-800 transition-colors">
                                        Save Changes
                                    </button>
                                </div>
                            )}
                        </section>

                        {/* ─── FORGET-ME-NOT / IMPORTANT DATES ─── */}
                        {!isArtisan && (
                            <section id="saved-addresses">
                                <div className="flex items-center justify-between mb-6 border-b border-stone-200 pb-4">
                                    <h3 className="text-2xl font-serif text-stone-900">Saved Addresses</h3>
                                    <span className="text-xs uppercase tracking-widest text-stone-400">{addresses.length} saved</span>
                                </div>

                                {addressesError && (
                                    <div className="mb-4 border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 flex items-center gap-2">
                                        <AlertCircle size={16} /> {addressesError}
                                    </div>
                                )}

                                {addressesLoading ? (
                                    <div className="h-32 bg-stone-100 animate-pulse" />
                                ) : (
                                    <div className="space-y-4">
                                        {addresses.map((address) => (
                                            <div key={address.id} className="border border-stone-200 bg-white p-5">
                                                <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
                                                    <div className="flex gap-4">
                                                        <div className="h-11 w-11 border border-stone-200 bg-stone-50 flex items-center justify-center text-stone-500 shrink-0">
                                                            <MapPin size={19} strokeWidth={1.5} />
                                                        </div>
                                                        <div>
                                                            <div className="flex flex-wrap items-center gap-2">
                                                                <h4 className="font-serif text-xl text-stone-950">{address.label}</h4>
                                                                {address.defaultAddress && (
                                                                    <span className="border border-green-200 bg-green-50 px-2 py-0.5 text-[10px] uppercase tracking-wide text-green-800">Default</span>
                                                                )}
                                                            </div>
                                                            <p className="mt-1 text-sm font-medium text-stone-800">{address.recipientName}</p>
                                                            <p className="mt-1 text-sm text-stone-500 leading-relaxed">{address.addressLine}</p>
                                                            {address.phoneNumber && <p className="mt-1 text-xs text-stone-400">{address.phoneNumber}</p>}
                                                        </div>
                                                    </div>
                                                    <div className="flex gap-2 sm:justify-end">
                                                        {!address.defaultAddress && (
                                                            <button onClick={() => makeDefaultAddress(address)} className="h-9 px-3 border border-stone-200 text-xs font-medium text-stone-600 hover:border-stone-900 hover:text-stone-900 inline-flex items-center gap-2">
                                                                <Star size={14} /> Default
                                                            </button>
                                                        )}
                                                        <button onClick={() => removeAddress(address.id)} className="h-9 px-3 border border-stone-200 text-xs font-medium text-stone-600 hover:border-red-200 hover:bg-red-50 hover:text-red-700 inline-flex items-center gap-2">
                                                            <Trash2 size={14} /> Remove
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}

                                        {addresses.length === 0 && (
                                            <div className="border border-dashed border-stone-300 bg-stone-50 px-6 py-8 text-center">
                                                <MapPin className="mx-auto mb-3 text-stone-400" size={24} />
                                                <h4 className="font-serif text-xl text-stone-900">No saved addresses yet</h4>
                                                <p className="mt-1 text-sm text-stone-500">Save a recipient here or during checkout.</p>
                                            </div>
                                        )}
                                    </div>
                                )}

                                <form onSubmit={saveAddress} className="mt-5 border border-stone-200 bg-white p-5">
                                    <h4 className="font-serif text-xl text-stone-900 mb-4">Add Recipient</h4>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <label className="block">
                                            <span className="text-xs uppercase tracking-wider text-stone-400 font-medium">Label</span>
                                            <input required value={addressForm.label} onChange={(event) => updateAddressForm('label', event.target.value)} className="mt-2 w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900" />
                                        </label>
                                        <label className="block">
                                            <span className="text-xs uppercase tracking-wider text-stone-400 font-medium">Recipient</span>
                                            <input required value={addressForm.recipientName} onChange={(event) => updateAddressForm('recipientName', event.target.value)} className="mt-2 w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900" />
                                        </label>
                                        <label className="block">
                                            <span className="text-xs uppercase tracking-wider text-stone-400 font-medium">Phone Optional</span>
                                            <input value={addressForm.phoneNumber} onChange={(event) => updateAddressForm('phoneNumber', event.target.value)} className="mt-2 w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900" />
                                        </label>
                                        <label className="flex items-end gap-3 pb-2 text-sm text-stone-700">
                                            <input type="checkbox" checked={addressForm.defaultAddress} onChange={(event) => updateAddressForm('defaultAddress', event.target.checked)} className="h-4 w-4 accent-stone-900" />
                                            Make default
                                        </label>
                                    </div>
                                    <label className="mt-4 block">
                                        <span className="text-xs uppercase tracking-wider text-stone-400 font-medium">Delivery Address</span>
                                        <textarea required rows={3} value={addressForm.addressLine} onChange={(event) => updateAddressForm('addressLine', event.target.value)} className="mt-2 w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 resize-none" />
                                    </label>
                                    <div className="mt-4 flex justify-end">
                                        <button className="h-10 px-5 bg-stone-900 text-white text-sm font-medium hover:bg-stone-800 inline-flex items-center gap-2">
                                            <Plus size={15} /> Save Address
                                        </button>
                                    </div>
                                </form>
                            </section>
                        )}

                        <section>
                            <div className="flex items-center justify-between mb-6 border-b border-stone-200 pb-4">
                                <div className="flex items-center gap-3">
                                    <h3 className="text-2xl font-serif text-stone-900">Forget-Me-Not</h3>
                                    <span className="bg-stone-100 text-stone-600 text-[10px] uppercase font-bold px-2 py-1 tracking-wider rounded-sm">Automated</span>
                                </div>
                                <button className="text-xs uppercase tracking-widest text-stone-500 hover:text-stone-900 flex items-center gap-2 transition-colors">
                                    <Plus size={14} /> Add Date
                                </button>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="bg-white border border-stone-200 p-6 relative group hover:border-stone-400 transition-all">
                                    <div className="absolute top-4 right-4 text-stone-300 group-hover:text-stone-900 transition-colors">
                                        <Shield size={16} />
                                    </div>
                                    <p className="text-xs uppercase tracking-widest text-stone-500 mb-2">Upcoming</p>
                                    <h4 className="text-xl font-serif text-stone-900 mb-1">My Wife's Birthday</h4>
                                    <p className="text-sm text-stone-600 mb-4">February 14, 2026</p>
                                    <div className="flex items-center gap-2 my-4">
                                        <div className="h-1.5 flex-1 bg-stone-100 rounded-full overflow-hidden">
                                            <div className="h-full bg-stone-900 w-3/4"></div>
                                        </div>
                                        <span className="text-[10px] text-stone-500 font-medium whitespace-nowrap">3 Days Left</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <label className="custom-checkbox flex items-center gap-2 cursor-pointer">
                                            <div className="w-8 h-4 bg-stone-900 rounded-full relative">
                                                <div className="absolute right-1 top-0.5 w-3 h-3 bg-white rounded-full"></div>
                                            </div>
                                            <span className="text-[10px] uppercase tracking-wider text-stone-900 font-bold">Auto-Send</span>
                                        </label>
                                    </div>
                                </div>

                                <div className="bg-stone-50 border border-stone-200 p-6 flex flex-col justify-center items-center text-center hover:bg-white transition-colors cursor-pointer border-dashed">
                                    <div className="w-10 h-10 bg-stone-200 rounded-full flex items-center justify-center text-stone-500 mb-3">
                                        <Plus size={20} />
                                    </div>
                                    <h4 className="font-serif text-lg text-stone-900">Add New Date</h4>
                                    <p className="text-xs text-stone-500 mt-1">Never miss an important moment.</p>
                                </div>
                            </div>
                        </section>

                        {!isArtisan && (
                            <section>
                                <div className="flex items-center justify-between mb-6 border-b border-stone-200 pb-4">
                                    <h3 className="text-2xl font-serif text-stone-900">Order History</h3>
                                    <Link to="/dashboard" className="text-xs uppercase tracking-widest text-stone-500 hover:text-stone-900 transition-colors">
                                        Continue Shopping
                                    </Link>
                                </div>

                                {ordersError && (
                                    <div className="mb-4 border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 flex items-center gap-2">
                                        <AlertCircle size={16} /> {ordersError}
                                    </div>
                                )}

                                {ordersLoading ? (
                                    <div className="space-y-4">
                                        {[1, 2].map((item) => (
                                            <div key={item} className="h-28 bg-stone-100 animate-pulse" />
                                        ))}
                                    </div>
                                ) : orders.length ? (
                                    <div className="space-y-4">
                                        {orders.map((order) => (
                                            <Link to={`/orders/${order.id}`} key={order.id} className="block border border-stone-200 bg-white p-4 sm:p-5 hover:border-stone-300 transition-colors">
                                                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                                    <div className="flex gap-4 min-w-0">
                                                        <div className="w-14 h-14 bg-stone-100 border border-stone-200 flex items-center justify-center text-stone-500 shrink-0">
                                                            {order.status === 'COMPLETED' ? <CheckCircle2 size={22} /> : <Truck size={22} />}
                                                        </div>
                                                        <div className="min-w-0">
                                                            <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                                                            <h4 className="mt-1 font-serif text-xl text-stone-950">{order.itemSummary}</h4>
                                                            <p className="mt-1 text-sm text-stone-500">{deliveryLabel(order)}</p>
                                                            <p className="mt-1 text-xs text-stone-400">Deliver to {order.recipientName}</p>
                                                            <p className="mt-1 text-xs text-stone-400">Payment: {paymentLabel(order.paymentMethod)}</p>
                                                        </div>
                                                    </div>
                                                    <div className="sm:text-right">
                                                        <span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${statusClass(order.status)}`}>
                                                            {statusLabel(order.status)}
                                                        </span>
                                                        <p className="mt-2 font-semibold text-stone-950">{currency(order.totalAmount)}</p>
                                                    </div>
                                                </div>

                                                <div className="mt-4 border-t border-stone-100 pt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
                                                    {(order.items || []).map((item) => (
                                                        <div key={`${order.id}-${item.productId}`} className="flex gap-3 min-w-0">
                                                            <img src={item.imageUrl} alt={item.productName} className="h-14 w-12 object-cover bg-stone-100 border border-stone-200" />
                                                            <div className="min-w-0">
                                                                <p className="text-sm font-medium text-stone-900 truncate">{item.productName}</p>
                                                                <p className="text-xs text-stone-500">{item.floristName}</p>
                                                                <p className="text-xs text-stone-400">Qty {item.quantity} · {currency(item.lineTotal)}</p>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            </Link>
                                        ))}
                                    </div>
                                ) : (
                                    <div className="border border-dashed border-stone-300 bg-stone-50 px-6 py-10 text-center">
                                        <PackageCheck className="mx-auto mb-3 text-stone-400" size={26} />
                                        <h4 className="font-serif text-xl text-stone-900">No orders yet</h4>
                                        <p className="mt-1 text-sm text-stone-500">Your checkout orders and florist status updates will appear here.</p>
                                        <Link to="/dashboard" className="mt-5 inline-flex h-10 items-center justify-center bg-stone-900 px-5 text-sm font-semibold text-white hover:bg-stone-800">
                                            Browse flowers
                                        </Link>
                                    </div>
                                )}
                            </section>
                        )}

                        {/* Recent Orders (Moved down) */}
                        <section className="hidden">
                            <div className="flex items-center justify-between mb-6 border-b border-stone-200 pb-4">
                                <h3 className="text-2xl font-serif text-stone-900">Order History</h3>
                                <Link to="/dashboard" className="text-xs uppercase tracking-widest text-stone-500 hover:text-stone-900 transition-colors">
                                    Continue Shopping
                                </Link>
                            </div>
                            <div className="space-y-4">
                                {[1].map((i) => (
                                    <div key={i} className="flex items-center justify-between p-4 border border-stone-100 hover:border-stone-300 transition-colors bg-white">
                                        <div className="flex items-center gap-4">
                                            <div className="w-12 h-12 bg-stone-100 flex items-center justify-center text-stone-400">
                                                <Leaf size={20} />
                                            </div>
                                            <div>
                                                <p className="font-serif text-lg text-stone-900">Autumn Collection #{1000 + i}</p>
                                                <p className="text-xs text-stone-500 uppercase tracking-wide">Delivered • Oct {10 + i}, 2026</p>
                                            </div>
                                        </div>
                                        <button className="text-xs font-medium text-stone-900 underline underline-offset-4 decoration-stone-300 hover:decoration-stone-900">View</button>
                                    </div>
                                ))}
                            </div>
                        </section>

                        {/* Petal Points Explainer (Replacing the specific component with cleaner UI) */}
                        {!isArtisan && (
                            <section>
                                <h3 className="text-2xl font-serif text-stone-900 mb-6 border-b border-stone-200 pb-4">Petal Rewards</h3>
                                <div className="bg-stone-900 text-stone-100 p-8 md:p-10 relative overflow-hidden">
                                    <div className="absolute -top-10 -right-10 w-40 h-40 rounded-full border border-stone-700/50"></div>
                                    <div className="relative z-10 grid grid-cols-1 md:grid-cols-3 gap-8 text-center md:text-left">
                                        <div>
                                            <Gift className="w-8 h-8 mb-4 mx-auto md:mx-0 text-stone-300" strokeWidth={1.5} />
                                            <h4 className="font-serif text-xl mb-2">Earn Points</h4>
                                            <p className="text-xs text-stone-400 leading-relaxed">Shop naturally. Earn 10 points for every €100 spent on our sustainable collections.</p>
                                        </div>
                                        <div>
                                            <Shield className="w-8 h-8 mb-4 mx-auto md:mx-0 text-stone-300" strokeWidth={1.5} />
                                            <h4 className="font-serif text-xl mb-2">Unlock Tiers</h4>
                                            <p className="text-xs text-stone-400 leading-relaxed">Access exclusive tiers: Seedling, Bloom, and Perennial for special perks.</p>
                                        </div>
                                        <div>
                                            <Heart className="w-8 h-8 mb-4 mx-auto md:mx-0 text-stone-300" strokeWidth={1.5} />
                                            <h4 className="font-serif text-xl mb-2">Redeem</h4>
                                            <p className="text-xs text-stone-400 leading-relaxed">Use points for discounts on future orders or gift them to a friend.</p>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        )}
                    </div>

                    {/* ─── RIGHT: MEMBERSHIP CARD ─── */}
                    <div className="lg:col-span-1">
                        <div className="sticky top-32 space-y-8">
                            <div className="bg-white p-8 border border-stone-200 shadow-sm text-center">
                                <p className="text-xs uppercase tracking-widest text-stone-500 mb-2">Current Status</p>
                                <h2 className="text-3xl font-serif text-stone-900 mb-4">{isArtisan ? 'Artisan Studio' : 'Seedling Member'}</h2>
                                <div className="w-full bg-stone-100 h-1 mb-4">
                                    <div className="bg-stone-900 h-1 w-3/4"></div>
                                </div>
                                <div className="flex justify-between text-[10px] uppercase tracking-wider text-stone-400 mb-6">
                                    <span>0 pts</span>
                                    <span>500 pts</span>
                                </div>
                                <p className="text-4xlfont-serif text-stone-900 mb-1">380</p>
                                <p className="text-sm text-stone-500 mb-6">Petal Points Available</p>
                                <button className="w-full py-3 border border-stone-900 text-stone-900 text-sm font-medium hover:bg-stone-900 hover:text-white transition-colors">
                                    View Rewards
                                </button>
                            </div>

                            <div className="bg-stone-50 p-6 border border-stone-200">
                                <h4 className="font-serif text-lg text-stone-900 mb-4">Account Settings</h4>
                                <ul className="space-y-3 text-sm">
                                    <li><button className="text-stone-600 hover:text-stone-900 transition-colors w-full text-left">Payment Methods</button></li>
                                    <li><a href="#saved-addresses" className="text-stone-600 hover:text-stone-900 transition-colors w-full text-left block">Saved Addresses</a></li>
                                    <li><button className="text-stone-600 hover:text-stone-900 transition-colors w-full text-left">Notification Preferences</button></li>
                                    <li><button className="text-stone-600 hover:text-stone-900 transition-colors w-full text-left">Privacy & Security</button></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

            </main>
        </div>
    );
}
