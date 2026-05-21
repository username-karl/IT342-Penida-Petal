import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Camera, ClipboardList, Image as ImageIcon, Leaf, Upload } from 'lucide-react';
import OrderStatusBadge from '../components/OrderStatusBadge';
import ShippingInfoCard from '../components/ShippingInfoCard';
import TrackingTimeline from '../components/TrackingTimeline';
import UpdateShippingStatusForm from '../components/UpdateShippingStatusForm';
import { ordersAPI } from '../services/api';

const paymentLabels = { COD: 'COD', GCASH: 'GCash', MAYA: 'Maya', CARD: 'Card' };
const ALLOWED_PHOTO_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
const MAX_PHOTO_BYTES = 5 * 1024 * 1024;

function currency(value) {
    return `PHP ${Number(value || 0).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function deliveryLabel(order) {
    if (!order?.deliveryDate) return order?.timeSlot || 'No delivery window';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} / ${order.timeSlot}`;
}

function normalizeStatus(status) {
    if (status === 'PREPARING') return 'ARRANGING';
    if (status === 'COMPLETED') return 'DELIVERED';
    return status || 'PENDING';
}

function canUploadFulfillment(status) {
    return ['ARRANGING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(normalizeStatus(status));
}

function canUploadProof(status) {
    return normalizeStatus(status) === 'DELIVERED';
}

function mediaUrl(value) {
    if (!value) return '';
    if (value.startsWith('http://') || value.startsWith('https://') || value.startsWith('/images/')) return value;
    if (value.startsWith('/uploads/')) return `http://localhost:8080${value}`;
    return value;
}

function OrderPhotoUpload({ title, helper, uploadedUrl, disabled, disabledText, onUpload }) {
    const [file, setFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState('');
    const [saving, setSaving] = useState(false);
    const [success, setSuccess] = useState('');
    const [error, setError] = useState('');

    const selectFile = (event) => {
        const nextFile = event.target.files?.[0] || null;
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        setSuccess('');
        setError('');

        if (!nextFile) {
            setFile(null);
            setPreviewUrl('');
            return;
        }
        if (!ALLOWED_PHOTO_TYPES.includes(nextFile.type)) {
            setFile(null);
            setPreviewUrl('');
            event.target.value = '';
            setError('Choose a JPG, PNG, or WEBP image.');
            return;
        }
        if (nextFile.size > MAX_PHOTO_BYTES) {
            setFile(null);
            setPreviewUrl('');
            event.target.value = '';
            setError('Photo must be 5MB or smaller.');
            return;
        }

        setFile(nextFile);
        setPreviewUrl(URL.createObjectURL(nextFile));
    };

    const submit = async (event) => {
        event.preventDefault();
        if (!file || disabled) return;
        setSaving(true);
        setSuccess('');
        setError('');
        try {
            await onUpload(file);
            setSuccess('Photo uploaded.');
            setFile(null);
            if (previewUrl) URL.revokeObjectURL(previewUrl);
            setPreviewUrl('');
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to upload photo');
        } finally {
            setSaving(false);
        }
    };

    useEffect(() => () => {
        if (previewUrl) URL.revokeObjectURL(previewUrl);
    }, [previewUrl]);

    return (
        <form onSubmit={submit} className="border border-stone-200 bg-white p-5">
            <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center border border-stone-200 bg-[#FDFCF8] text-stone-500">
                    <Camera size={18} strokeWidth={1.5} />
                </div>
                <div>
                    <h3 className="font-serif text-xl text-stone-950">{title}</h3>
                    <p className="mt-1 text-sm leading-relaxed text-stone-500">{disabled ? disabledText : helper}</p>
                </div>
            </div>

            {uploadedUrl && (
                <div className="mt-4 overflow-hidden border border-stone-200 bg-stone-50">
                    <img src={mediaUrl(uploadedUrl)} alt={title} className="h-40 w-full object-cover" />
                    <p className="border-t border-stone-200 px-3 py-2 text-xs font-medium uppercase tracking-widest text-stone-500">Uploaded</p>
                </div>
            )}

            {!disabled && (
                <div className="mt-4 space-y-3">
                    <label className="flex min-h-11 cursor-pointer items-center justify-center gap-2 border border-stone-200 bg-stone-50 px-3 text-sm font-medium text-stone-700 transition-colors hover:border-stone-900 hover:text-stone-950">
                        <ImageIcon size={16} />
                        {file ? file.name : 'Choose photo'}
                        <input type="file" accept="image/png,image/jpeg,image/jpg,image/webp" onChange={selectFile} className="sr-only" />
                    </label>
                    {previewUrl && (
                        <img src={previewUrl} alt="Selected preview" className="h-28 w-full border border-stone-200 object-cover" />
                    )}
                    <button disabled={!file || saving} className="inline-flex min-h-11 w-full items-center justify-center gap-2 bg-stone-950 px-4 text-sm font-semibold text-white hover:bg-stone-800 disabled:cursor-not-allowed disabled:bg-stone-200 disabled:text-stone-500">
                        <Upload size={16} /> {saving ? 'Uploading...' : uploadedUrl ? 'Replace photo' : 'Upload photo'}
                    </button>
                </div>
            )}

            {success && <p className="mt-3 border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">{success}</p>}
            {error && <p className="mt-3 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        </form>
    );
}

export default function SellerOrderDetail() {
    const { id } = useParams();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadOrder = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await ordersAPI.getSellerOrder(id);
                setOrder(response.data.data);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load seller order');
            } finally {
                setLoading(false);
            }
        };

        loadOrder();
    }, [id]);

    const updateShipping = async (data) => {
        setError('');
        const response = await ordersAPI.updateSellerShipping(order.id, data);
        setOrder(response.data.data);
    };

    const uploadFulfillmentPhoto = async (file) => {
        const response = await ordersAPI.uploadFulfillmentPhoto(order.id, file);
        setOrder(response.data.data);
    };

    const uploadProofPhoto = async (file) => {
        const response = await ordersAPI.uploadProofPhoto(order.id, file);
        setOrder(response.data.data);
    };

    return (
        <div className="min-h-screen bg-[#F7F3EC] text-stone-900">
            <nav className="border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="max-w-6xl mx-auto h-16 px-6 flex items-center justify-between">
                    <Link to="/seller-centre" className="text-xl font-serif tracking-tight inline-flex items-center gap-2">
                        <Leaf size={18} strokeWidth={1.5} /> Seller Centre
                    </Link>
                    <Link to="/seller-centre?tab=orders" className="inline-flex items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-900">
                        <ArrowLeft size={16} /> Back to orders
                    </Link>
                </div>
            </nav>

            <main className="max-w-6xl mx-auto px-6 py-10">
                {loading ? (
                    <div className="h-96 bg-stone-100 animate-pulse border border-stone-200" />
                ) : error ? (
                    <div className="border border-red-200 bg-red-50 p-5 text-sm text-red-700">{error}</div>
                ) : order && (
                    <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-[minmax(0,1fr)_380px]">
                        <div className="order-2 space-y-6 lg:order-1">
                            <section className="self-start bg-white border border-stone-200">
                            <div className="p-6 border-b border-stone-100">
                                <p className="text-xs uppercase tracking-[0.2em] text-stone-400">{order.orderNumber}</p>
                                <h1 className="mt-2 font-serif text-4xl">Fulfillment Details</h1>
                                <p className="mt-2 text-sm text-stone-500">{deliveryLabel(order)}</p>
                            </div>
                            <div className="divide-y divide-stone-100">
                                {(order.items || []).map((item) => (
                                    <div key={`${order.id}-${item.productId}`} className="p-5 flex items-center justify-between gap-4">
                                        <div>
                                            <h2 className="font-serif text-2xl">{item.productName}</h2>
                                            <p className="mt-1 text-sm text-stone-500">Qty {item.quantity} / {currency(item.unitPrice)} each</p>
                                        </div>
                                        <p className="font-semibold whitespace-nowrap">{currency(item.lineTotal)}</p>
                                    </div>
                                ))}
                            </div>
                            </section>
                            <section>
                                <h2 className="mb-4 font-serif text-2xl text-stone-950">Logistics Tracking</h2>
                                <TrackingTimeline events={order.shipping?.events || []} />
                            </section>
                        </div>

                        <aside className="order-1 space-y-4 lg:order-2">
                            <div className="bg-white border border-stone-200 p-5">
                                <div className="flex items-center justify-between">
                                    <OrderStatusBadge status={order.status} compact />
                                    <ClipboardList size={20} />
                                </div>
                                <dl className="mt-5 space-y-3 text-sm">
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Buyer</dt><dd className="font-medium text-right">{order.buyerName}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Recipient</dt><dd className="font-medium text-right">{order.recipientName}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Address</dt><dd className="font-medium text-right">{order.recipientAddress}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Payment</dt><dd className="font-medium">{paymentLabels[order.paymentMethod] || order.paymentMethod}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Seller total</dt><dd className="font-semibold">{currency(order.sellerSubtotal)}</dd></div>
                                </dl>
                                {order.cardMessage && (
                                    <div className="mt-5 border-t border-stone-100 pt-4">
                                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">Card Message</p>
                                        <p className="mt-2 text-sm text-stone-600 leading-relaxed">{order.cardMessage}</p>
                                    </div>
                                )}
                            </div>
                            <UpdateShippingStatusForm order={order} onSubmit={updateShipping} />
                            <OrderPhotoUpload
                                title="Bouquet preparation"
                                helper="Upload a florist-side photo once the bouquet is being arranged or packaged."
                                uploadedUrl={order.fulfillmentImageUrl}
                                disabled={!canUploadFulfillment(order.status)}
                                disabledText="Available once the order is marked as Arranging."
                                onUpload={uploadFulfillmentPhoto}
                            />
                            <OrderPhotoUpload
                                title="Proof of Delivery"
                                helper="Upload delivery proof after the order is marked Delivered."
                                uploadedUrl={order.proofImageUrl}
                                disabled={!canUploadProof(order.status)}
                                disabledText="Available after the order is marked Delivered."
                                onUpload={uploadProofPhoto}
                            />
                            <ShippingInfoCard orderId={order.id} shipping={order.shipping} />
                        </aside>
                    </div>
                )}
            </main>
        </div>
    );
}
