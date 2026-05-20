import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import {
    AlertCircle, Bell, CalendarDays, CheckCircle2, Gift, Heart, Leaf, MapPin, PackageCheck, Plus, Shield, Star, Trash2, Truck, Edit2, X
} from 'lucide-react';
import { addressesAPI, ordersAPI, savedDatesAPI } from '../services/api';

function currency(value) {
    const number = Number(value || 0);
    return `PHP ${number.toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function statusLabel(status) {
    const labels = {
        PENDING: 'Order Placed',
        ACCEPTED: 'Accepted',
        ARRANGING: 'Being Arranged',
        PREPARING: 'Being Arranged',
        READY_FOR_PICKUP: 'Ready for Pickup',
        OUT_FOR_DELIVERY: 'Out for Delivery',
        DELIVERED: 'Delivered',
        COMPLETED: 'Delivered',
        CANCELLED: 'Cancelled',
    };
    return labels[status] || status;
}

function statusClass(status) {
    if (status === 'COMPLETED' || status === 'DELIVERED' || status === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (status === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (status === 'ACCEPTED' || status === 'ARRANGING' || status === 'PREPARING') return 'bg-amber-50 text-amber-800 border-amber-200';
    if (status === 'OUT_FOR_DELIVERY') return 'bg-stone-900 text-white border-stone-900';
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

function formatSavedDate(value) {
    if (!value) return 'Date pending';
    const date = new Date(`${value}T00:00:00`);
    return date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' });
}

function normalizeSavedDate(savedDate) {
    return {
        id: savedDate.id || savedDate.dateId,
        label: savedDate.label || 'Important date',
        eventDate: savedDate.eventDate,
        recurring: savedDate.recurring ?? savedDate.isRecurring ?? false,
    };
}

const RECENT_ORDER_LIMIT = 3;
const INACTIVE_ORDER_STATUSES = ['DELIVERED', 'COMPLETED', 'CANCELLED'];

function isActiveOrder(order) {
    return !INACTIVE_ORDER_STATUSES.includes(order?.status);
}

function sortActiveOrders(a, b) {
    if (!a.deliveryDate && !b.deliveryDate) return Number(b.id || 0) - Number(a.id || 0);
    if (!a.deliveryDate) return 1;
    if (!b.deliveryDate) return -1;
    return new Date(`${a.deliveryDate}T00:00:00`) - new Date(`${b.deliveryDate}T00:00:00`);
}

function primaryOrderItem(order) {
    return order?.items?.[0] || null;
}

export default function Profile() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [isEditing, setIsEditing] = useState(false);
    const [showRecipientBook, setShowRecipientBook] = useState(false);
    const [showRecipientModal, setShowRecipientModal] = useState(false);
    const [orders, setOrders] = useState([]);
    const [ordersLoading, setOrdersLoading] = useState(true);
    const [ordersError, setOrdersError] = useState('');
    const [addresses, setAddresses] = useState([]);
    const [addressesLoading, setAddressesLoading] = useState(true);
    const [addressesError, setAddressesError] = useState('');
    const [savedDates, setSavedDates] = useState([]);
    const [savedDatesLoading, setSavedDatesLoading] = useState(true);
    const [savedDatesError, setSavedDatesError] = useState('');
    const [savedDatesSuccess, setSavedDatesSuccess] = useState('');
    const [showSavedDateModal, setShowSavedDateModal] = useState(false);
    const [savedDateSaving, setSavedDateSaving] = useState(false);
    const [savedDateFormErrors, setSavedDateFormErrors] = useState({});
    const [savedDateForm, setSavedDateForm] = useState({
        label: '',
        eventDate: '',
        isRecurring: true,
    });
    const [addressForm, setAddressForm] = useState({
        label: 'Home',
        recipientName: '',
        phoneNumber: '',
        addressLine: '',
        defaultAddress: false,
    });

    const displayName = user?.name || 'Guest';
    const email = user?.email || 'guest@example.com';
    const initials = displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';
    const recentOrders = orders.slice(0, RECENT_ORDER_LIMIT);
    const currentGift = orders.filter(isActiveOrder).sort(sortActiveOrders)[0] || null;
    const reminderCountText = savedDatesLoading
        ? 'Loading reminders'
        : savedDates.length === 1
            ? '1 saved reminder'
            : savedDates.length > 1
                ? `${savedDates.length} saved reminders`
                : 'No reminders yet';

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

    useEffect(() => {
        const loadSavedDates = async () => {
            setSavedDatesLoading(true);
            setSavedDatesError('');
            try {
                const response = await savedDatesAPI.getSavedDates();
                setSavedDates((response.data.data || []).map(normalizeSavedDate));
            } catch (err) {
                setSavedDatesError(err.response?.data?.message || err.message || 'Unable to load important dates');
            } finally {
                setSavedDatesLoading(false);
            }
        };

        if (user && !isArtisan) {
            loadSavedDates();
        } else {
            setSavedDatesLoading(false);
        }
    }, [isArtisan, user]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const updateSavedDateForm = (field, value) => {
        setSavedDateForm((current) => ({ ...current, [field]: value }));
        setSavedDateFormErrors((current) => ({ ...current, [field]: '' }));
    };

    const resetSavedDateForm = () => {
        setSavedDateForm({
            label: '',
            eventDate: '',
            isRecurring: true,
        });
        setSavedDateFormErrors({});
    };

    const closeSavedDateModal = () => {
        setShowSavedDateModal(false);
        resetSavedDateForm();
    };

    const validateSavedDateForm = () => {
        const errors = {};
        if (!savedDateForm.label.trim()) {
            errors.label = 'Add a label for this date.';
        }
        if (!savedDateForm.eventDate) {
            errors.eventDate = 'Choose the event date.';
        }
        return errors;
    };

    const saveImportantDate = async (event) => {
        event.preventDefault();
        const errors = validateSavedDateForm();
        setSavedDateFormErrors(errors);
        setSavedDatesError('');
        setSavedDatesSuccess('');

        if (Object.keys(errors).length > 0) {
            return;
        }

        setSavedDateSaving(true);
        try {
            const response = await savedDatesAPI.createSavedDate({
                label: savedDateForm.label.trim(),
                eventDate: savedDateForm.eventDate,
                isRecurring: Boolean(savedDateForm.isRecurring),
            });
            const savedDate = normalizeSavedDate(response.data.data);
            setSavedDates((current) => [savedDate, ...current]);
            setSavedDatesSuccess(`${savedDate.label} has been saved.`);
            closeSavedDateModal();
        } catch (err) {
            const message = err.response?.data?.message || err.message || 'Unable to save important date';
            setSavedDatesError(message);
        } finally {
            setSavedDateSaving(false);
        }
    };

    const updateAddressForm = (field, value) => {
        setAddressForm((current) => ({ ...current, [field]: value }));
    };

    const resetAddressForm = () => {
        setAddressForm({
            label: 'Home',
            recipientName: '',
            phoneNumber: '',
            addressLine: '',
            defaultAddress: false,
        });
    };

    const closeRecipientModal = () => {
        setShowRecipientModal(false);
        resetAddressForm();
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
            resetAddressForm();
            setShowRecipientModal(false);
            setShowRecipientBook(true);
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

            <main className="pt-28 pb-20 max-w-5xl mx-auto px-5 sm:px-6">

                {/* ─── HEADER ─── */}
                <header className="mb-10 text-center">
                    <div className="w-20 h-20 mx-auto bg-stone-100 rounded-full flex items-center justify-center text-2xl font-serif italic text-stone-800 mb-5 border border-stone-200">
                        {initials}
                    </div>
                    <h1 className="text-3xl md:text-4xl font-serif text-stone-900 mb-2">
                        {displayName}
                    </h1>
                    <p className="text-sm text-stone-500">{email}</p>
                </header>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* ─── LEFT: ACCOUNT DETAILS ─── */}
                    <div className="lg:col-span-2 space-y-8">

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
                        {false && !isArtisan && (
                            <section id="saved-addresses">
                                <div className="mb-6 flex flex-col gap-3 border-b border-stone-200 pb-4 sm:flex-row sm:items-end sm:justify-between">
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

                        {!isArtisan && (
                            <section>
                                <div className="mb-4 flex items-center justify-between border-b border-stone-200 pb-4">
                                    <div>
                                        <h3 className="text-2xl font-serif text-stone-900">Current Gift</h3>
                                        <p className="mt-1 text-sm text-stone-500">Your next active bouquet delivery.</p>
                                    </div>
                                </div>

                                {ordersLoading ? (
                                    <div className="h-28 bg-stone-100 animate-pulse" />
                                ) : currentGift ? (
                                    <Link to={`/orders/${currentGift.id}`} className="group block border border-stone-200 bg-white p-4 transition-colors hover:border-stone-400 hover:bg-[#FFFDF9] sm:p-5">
                                        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                            <div className="flex gap-4 min-w-0">
                                                {primaryOrderItem(currentGift)?.imageUrl ? (
                                                    <img
                                                        src={primaryOrderItem(currentGift).imageUrl}
                                                        alt={primaryOrderItem(currentGift).productName || currentGift.itemSummary}
                                                        className="h-16 w-14 shrink-0 border border-stone-200 bg-stone-100 object-cover"
                                                    />
                                                ) : (
                                                    <div className="h-16 w-14 bg-[#F7F1EA] border border-stone-200 flex items-center justify-center text-stone-500 shrink-0">
                                                        <Truck size={21} strokeWidth={1.5} />
                                                    </div>
                                                )}
                                                <div className="min-w-0">
                                                    <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{currentGift.orderNumber}</p>
                                                    <h4 className="mt-1 font-serif text-xl text-stone-950">{primaryOrderItem(currentGift)?.productName || currentGift.itemSummary}</h4>
                                                    <p className="mt-1 text-sm text-stone-500">{deliveryLabel(currentGift)}</p>
                                                    <p className="mt-1 text-xs text-stone-400">Deliver to {currentGift.recipientName}</p>
                                                </div>
                                            </div>
                                            <div className="flex items-center justify-between gap-4 sm:block sm:text-right">
                                                <span className={`inline-flex border px-2.5 py-1 text-xs font-medium leading-none ${statusClass(currentGift.status)}`}>
                                                    {statusLabel(currentGift.status)}
                                                </span>
                                                <p className="text-xs font-medium uppercase tracking-widest text-stone-500 transition-colors group-hover:text-stone-900 sm:mt-4">View details</p>
                                            </div>
                                        </div>
                                    </Link>
                                ) : (
                                    <div className="border border-dashed border-stone-300 bg-stone-50 px-5 py-5">
                                        <div className="flex items-start gap-3">
                                            <PackageCheck className="mt-0.5 shrink-0 text-stone-400" size={20} strokeWidth={1.5} />
                                            <div>
                                                <h4 className="font-serif text-lg text-stone-900">No active gift right now</h4>
                                                <p className="mt-1 text-sm leading-relaxed text-stone-500">When a florist is preparing or delivering a bouquet, it will appear here.</p>
                                            </div>
                                        </div>
                                    </div>
                                )}
                            </section>
                        )}

                        {!isArtisan && (
                            <section>
                                <div className="flex flex-col gap-3 mb-5 border-b border-stone-200 pb-4 sm:flex-row sm:items-end sm:justify-between">
                                    <div>
                                        <h3 className="text-2xl font-serif text-stone-900">Recent Gifts</h3>
                                        <p className="mt-1 text-sm text-stone-500">
                                            {orders.length
                                                ? `Showing latest ${Math.min(orders.length, RECENT_ORDER_LIMIT)} of ${orders.length} gift${orders.length === 1 ? '' : 's'}`
                                                : 'Your latest gifts will appear here after checkout.'}
                                        </p>
                                    </div>
                                    <Link to="/purchase-history" className="inline-flex min-h-11 items-center justify-center border border-stone-900 px-4 text-xs font-medium uppercase tracking-widest text-stone-900 transition-colors hover:bg-stone-900 hover:text-white">
                                        View Purchase History
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
                                ) : recentOrders.length ? (
                                    <div className="space-y-3">
                                        {recentOrders.map((order) => (
                                            <Link to={`/orders/${order.id}`} key={order.id} className="group block border border-stone-200 bg-white p-4 transition-colors hover:border-stone-400 hover:bg-[#FFFDF9] sm:p-5">
                                                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                                    <div className="flex gap-4 min-w-0">
                                                        <div className="w-14 h-14 bg-[#F7F1EA] border border-stone-200 flex items-center justify-center text-stone-500 shrink-0">
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
                                                    <div className="flex items-center justify-between gap-4 sm:block sm:text-right">
                                                        <span className={`inline-flex border px-2.5 py-1 text-xs font-medium leading-none ${statusClass(order.status)}`}>
                                                            {statusLabel(order.status)}
                                                        </span>
                                                        <p className="font-semibold tabular-nums text-stone-950 sm:mt-2">{currency(order.totalAmount)}</p>
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
                                                <div className="mt-4 flex justify-end border-t border-stone-100 pt-3">
                                                    <span className="text-xs font-medium uppercase tracking-widest text-stone-500 transition-colors group-hover:text-stone-900">View details</span>
                                                </div>
                                            </Link>
                                        ))}
                                        {orders.length > RECENT_ORDER_LIMIT && (
                                            <Link to="/purchase-history" className="flex min-h-12 items-center justify-center border border-dashed border-stone-300 bg-stone-50 px-4 text-sm font-medium text-stone-700 transition-colors hover:border-stone-900 hover:bg-white hover:text-stone-950">
                                                View all {orders.length} gifts
                                            </Link>
                                        )}
                                    </div>
                                ) : (
                                    <div className="border border-dashed border-stone-300 bg-stone-50 px-6 py-10 text-center">
                                        <PackageCheck className="mx-auto mb-3 text-stone-400" size={26} />
                                        <h4 className="font-serif text-xl text-stone-900">No gifts yet</h4>
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
                        {false && !isArtisan && (
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
                        <div className="sticky top-28 space-y-5">
                            <div id="saved-addresses" className="bg-white p-5 border border-stone-200 shadow-sm">
                                <div className="flex items-start justify-between gap-4 border-b border-stone-100 pb-4">
                                    <div>
                                        <p className="text-xs uppercase tracking-widest text-stone-500">Saved Addresses</p>
                                        <h2 className="mt-1 text-2xl font-serif text-stone-900">Recipient Book</h2>
                                    </div>
                                    <span className="border border-stone-200 bg-[#FDFCF8] px-2 py-1 text-[10px] uppercase tracking-wider text-stone-500">{addresses.length} saved</span>
                                </div>

                                {addressesError && (
                                    <div className="mt-4 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 flex items-start gap-2">
                                        <AlertCircle size={16} className="mt-0.5 shrink-0" /> {addressesError}
                                    </div>
                                )}

                                {addressesLoading ? (
                                    <div className="mt-4 h-24 bg-stone-100 animate-pulse" />
                                ) : (
                                    <div className="mt-4 space-y-3">
                                        {(showRecipientBook ? addresses : addresses.slice(0, 1)).map((address) => (
                                            <div key={address.id} className="border border-stone-200 bg-[#FDFCF8] p-4 transition-colors hover:border-stone-300">
                                                <div className="flex items-start gap-3">
                                                    <div className="h-9 w-9 border border-stone-200 bg-white flex items-center justify-center text-stone-500 shrink-0">
                                                        <MapPin size={16} strokeWidth={1.5} />
                                                    </div>
                                                    <div className="min-w-0 flex-1">
                                                        <div className="flex flex-wrap items-center gap-2">
                                                            <h4 className="font-serif text-lg leading-tight text-stone-950">{address.label}</h4>
                                                            {address.defaultAddress && (
                                                                <span className="border border-green-200 bg-green-50 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide text-green-800">Default</span>
                                                            )}
                                                        </div>
                                                        <p className="mt-1 text-sm font-medium text-stone-800">{address.recipientName}</p>
                                                        <p className="mt-1 text-xs leading-relaxed text-stone-500">{address.addressLine}</p>
                                                        {address.phoneNumber && <p className="mt-1 text-xs text-stone-400">{address.phoneNumber}</p>}
                                                    </div>
                                                </div>
                                                {showRecipientBook && (
                                                    <div className="mt-3 flex flex-wrap gap-2 border-t border-stone-100 pt-3">
                                                        {!address.defaultAddress && (
                                                            <button type="button" onClick={() => makeDefaultAddress(address)} className="min-h-9 px-3 border border-stone-200 text-xs font-medium text-stone-600 hover:border-stone-900 hover:text-stone-900 inline-flex items-center gap-2">
                                                                <Star size={14} /> Default
                                                            </button>
                                                        )}
                                                        <button type="button" onClick={() => removeAddress(address.id)} className="min-h-9 px-3 border border-stone-200 text-xs font-medium text-stone-600 hover:border-red-200 hover:bg-red-50 hover:text-red-700 inline-flex items-center gap-2">
                                                            <Trash2 size={14} /> Remove
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        ))}

                                        {addresses.length === 0 && (
                                            <div className="border border-dashed border-stone-300 bg-stone-50 px-4 py-6 text-center">
                                                <MapPin className="mx-auto mb-3 text-stone-400" size={24} />
                                                <h4 className="font-serif text-lg text-stone-900">No saved addresses yet</h4>
                                                <p className="mt-1 text-sm text-stone-500">Save frequent recipients for faster checkout.</p>
                                            </div>
                                        )}
                                    </div>
                                )}

                                <button
                                    type="button"
                                    onClick={() => setShowRecipientBook((current) => !current)}
                                    className="mt-4 min-h-11 w-full border border-stone-900 px-4 text-sm font-medium text-stone-900 hover:bg-stone-900 hover:text-white transition-colors inline-flex items-center justify-center gap-2"
                                >
                                    {showRecipientBook ? 'Hide recipient manager' : 'Manage recipients'}
                                </button>

                                <button
                                    type="button"
                                    onClick={() => setShowRecipientModal(true)}
                                    className="mt-3 min-h-11 w-full bg-stone-900 px-4 text-sm font-medium text-white hover:bg-stone-800 transition-colors inline-flex items-center justify-center gap-2"
                                >
                                    <Plus size={15} /> {addresses.length ? 'Add recipient' : 'Add first recipient'}
                                </button>
                            </div>

                            {!isArtisan && (
                                <div className="bg-white p-5 border border-stone-200 shadow-sm">
                                    <div className="flex items-start gap-3">
                                        <div className="h-10 w-10 border border-stone-200 bg-[#FDFCF8] flex items-center justify-center text-stone-500 shrink-0">
                                            <CalendarDays size={18} strokeWidth={1.5} />
                                        </div>
                                        <div>
                                            <h4 className="font-serif text-lg text-stone-900">Forget-Me-Not</h4>
                                            <p className="mt-1 text-sm leading-relaxed text-stone-500">Never miss birthdays, anniversaries, and special moments.</p>
                                        </div>
                                    </div>

                                    {savedDatesSuccess && (
                                        <div className="mt-4 flex items-start gap-2 border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
                                            <CheckCircle2 size={16} className="mt-0.5 shrink-0" /> {savedDatesSuccess}
                                        </div>
                                    )}

                                    {savedDatesError && (
                                        <div className="mt-4 flex items-start gap-2 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                                            <AlertCircle size={16} className="mt-0.5 shrink-0" /> {savedDatesError}
                                        </div>
                                    )}

                                    <div className="mt-4 flex items-center justify-between gap-4 border-t border-stone-100 pt-4">
                                        <p className="text-sm font-medium text-stone-700">{reminderCountText}</p>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setSavedDatesError('');
                                                setSavedDatesSuccess('');
                                                setShowSavedDateModal(true);
                                            }}
                                            className="inline-flex min-h-10 items-center justify-center gap-2 border border-stone-900 px-3 text-xs font-medium uppercase tracking-widest text-stone-900 transition-colors hover:bg-stone-900 hover:text-white"
                                        >
                                            <Bell size={14} /> Add reminder
                                        </button>
                                    </div>
                                </div>
                            )}

                            <div className="bg-stone-50 p-5 border border-stone-200">
                                <h4 className="font-serif text-lg text-stone-900 mb-3">Account Settings</h4>
                                <ul className="divide-y divide-stone-200 text-sm">
                                    <li><button className="w-full py-3 text-left text-stone-600 hover:text-stone-900 transition-colors">Payment Methods</button></li>
                                    <li><a href="#saved-addresses" className="w-full py-3 text-left text-stone-600 hover:text-stone-900 transition-colors block">Saved Addresses</a></li>
                                    <li><button className="w-full py-3 text-left text-stone-600 hover:text-stone-900 transition-colors">Notification Preferences</button></li>
                                    <li><button className="w-full py-3 text-left text-stone-600 hover:text-stone-900 transition-colors">Privacy & Security</button></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

            </main>

            {showRecipientModal && (
                <div className="fixed inset-0 z-[80] flex items-end justify-center bg-stone-950/45 px-4 py-4 backdrop-blur-sm sm:items-center" role="dialog" aria-modal="true" aria-labelledby="recipient-modal-title">
                    <div className="w-full max-w-lg border border-stone-200 bg-[#FDFCF8] shadow-2xl">
                        <div className="flex items-start justify-between gap-4 border-b border-stone-200 px-5 py-4">
                            <div>
                                <p className="text-xs uppercase tracking-widest text-stone-500">Recipient Book</p>
                                <h2 id="recipient-modal-title" className="mt-1 font-serif text-2xl text-stone-900">Add New Recipient</h2>
                            </div>
                            <button
                                type="button"
                                onClick={closeRecipientModal}
                                className="flex h-11 w-11 items-center justify-center border border-stone-200 text-stone-600 transition-colors hover:border-stone-900 hover:text-stone-900"
                                aria-label="Close recipient form"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        <form onSubmit={saveAddress} className="px-5 py-5">
                            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                <label className="block">
                                    <span className="text-xs uppercase tracking-wider text-stone-500 font-medium">Label</span>
                                    <input required value={addressForm.label} onChange={(event) => updateAddressForm('label', event.target.value)} className="mt-2 min-h-11 w-full bg-white border border-stone-200 px-3 text-stone-900 outline-none focus:border-stone-900" />
                                </label>
                                <label className="block">
                                    <span className="text-xs uppercase tracking-wider text-stone-500 font-medium">Recipient</span>
                                    <input required value={addressForm.recipientName} onChange={(event) => updateAddressForm('recipientName', event.target.value)} className="mt-2 min-h-11 w-full bg-white border border-stone-200 px-3 text-stone-900 outline-none focus:border-stone-900" />
                                </label>
                                <label className="block sm:col-span-2">
                                    <span className="text-xs uppercase tracking-wider text-stone-500 font-medium">Phone Optional</span>
                                    <input value={addressForm.phoneNumber} onChange={(event) => updateAddressForm('phoneNumber', event.target.value)} className="mt-2 min-h-11 w-full bg-white border border-stone-200 px-3 text-stone-900 outline-none focus:border-stone-900" />
                                </label>
                                <label className="block sm:col-span-2">
                                    <span className="text-xs uppercase tracking-wider text-stone-500 font-medium">Delivery Address</span>
                                    <textarea required rows={4} value={addressForm.addressLine} onChange={(event) => updateAddressForm('addressLine', event.target.value)} className="mt-2 w-full bg-white border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 resize-none" />
                                </label>
                            </div>

                            <label className="mt-4 flex min-h-10 items-center gap-3 text-sm text-stone-700">
                                <input type="checkbox" checked={addressForm.defaultAddress} onChange={(event) => updateAddressForm('defaultAddress', event.target.checked)} className="h-4 w-4 accent-stone-900" />
                                Make default recipient
                            </label>

                            <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                                <button
                                    type="button"
                                    onClick={closeRecipientModal}
                                    className="min-h-11 border border-stone-200 px-5 text-sm font-medium text-stone-700 transition-colors hover:border-stone-900 hover:text-stone-900"
                                >
                                    Cancel
                                </button>
                                <button className="min-h-11 bg-stone-900 px-5 text-sm font-medium text-white transition-colors hover:bg-stone-800 inline-flex items-center justify-center gap-2">
                                    <Plus size={15} /> Save Recipient
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showSavedDateModal && (
                <div className="fixed inset-0 z-[80] flex items-end justify-center bg-stone-950/45 px-4 py-4 backdrop-blur-sm sm:items-center" role="dialog" aria-modal="true" aria-labelledby="saved-date-modal-title">
                    <div className="w-full max-w-lg border border-stone-200 bg-[#FDFCF8] shadow-2xl">
                        <div className="flex items-start justify-between gap-4 border-b border-stone-200 px-5 py-4">
                            <div>
                                <p className="text-xs uppercase tracking-widest text-stone-500">Forget-Me-Not</p>
                                <h2 id="saved-date-modal-title" className="mt-1 font-serif text-2xl text-stone-900">Add Important Date</h2>
                            </div>
                            <button
                                type="button"
                                onClick={closeSavedDateModal}
                                className="flex h-11 w-11 items-center justify-center border border-stone-200 text-stone-600 transition-colors hover:border-stone-900 hover:text-stone-900"
                                aria-label="Close important date form"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        <form onSubmit={saveImportantDate} className="px-5 py-5">
                            {savedDatesError && (
                                <div className="mb-4 flex items-start gap-2 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                                    <AlertCircle size={16} className="mt-0.5 shrink-0" /> {savedDatesError}
                                </div>
                            )}

                            <div className="grid grid-cols-1 gap-4">
                                <label className="block">
                                    <span className="text-xs uppercase tracking-wider text-stone-500 font-medium">Label</span>
                                    <input
                                        value={savedDateForm.label}
                                        onChange={(event) => updateSavedDateForm('label', event.target.value)}
                                        placeholder="Mom's birthday"
                                        className={`mt-2 min-h-11 w-full border bg-white px-3 text-stone-900 outline-none focus:border-stone-900 ${savedDateFormErrors.label ? 'border-red-300' : 'border-stone-200'}`}
                                    />
                                    {savedDateFormErrors.label && <p className="mt-1 text-xs text-red-700">{savedDateFormErrors.label}</p>}
                                </label>

                                <label className="block">
                                    <span className="text-xs uppercase tracking-wider text-stone-500 font-medium">Event Date</span>
                                    <input
                                        type="date"
                                        value={savedDateForm.eventDate}
                                        onChange={(event) => updateSavedDateForm('eventDate', event.target.value)}
                                        className={`mt-2 min-h-11 w-full border bg-white px-3 text-stone-900 outline-none focus:border-stone-900 ${savedDateFormErrors.eventDate ? 'border-red-300' : 'border-stone-200'}`}
                                    />
                                    {savedDateFormErrors.eventDate && <p className="mt-1 text-xs text-red-700">{savedDateFormErrors.eventDate}</p>}
                                </label>

                                <label className="flex min-h-14 items-center justify-between gap-4 border border-stone-200 bg-white px-4 py-3">
                                    <span>
                                        <span className="block text-sm font-medium text-stone-900">Repeat every year</span>
                                        <span className="mt-1 block text-xs leading-relaxed text-stone-500">Use this for birthdays, anniversaries, and recurring moments.</span>
                                    </span>
                                    <input
                                        type="checkbox"
                                        checked={savedDateForm.isRecurring}
                                        onChange={(event) => updateSavedDateForm('isRecurring', event.target.checked)}
                                        className="h-4 w-4 shrink-0 accent-stone-900"
                                    />
                                </label>
                            </div>

                            <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                                <button
                                    type="button"
                                    onClick={closeSavedDateModal}
                                    className="min-h-11 border border-stone-200 px-5 text-sm font-medium text-stone-700 transition-colors hover:border-stone-900 hover:text-stone-900"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={savedDateSaving}
                                    className="inline-flex min-h-11 items-center justify-center gap-2 bg-stone-900 px-5 text-sm font-medium text-white transition-colors hover:bg-stone-800 disabled:cursor-not-allowed disabled:opacity-60"
                                >
                                    <Plus size={15} /> {savedDateSaving ? 'Saving...' : 'Save Date'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
