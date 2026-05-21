import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Camera, Check, Copy, Leaf } from 'lucide-react';
import TrackingTimeline from '../components/TrackingTimeline';
import { ordersAPI } from '../services/api';

function formatDate(value) {
    if (!value) return 'To be confirmed';
    return new Date(`${value}T00:00:00`).toLocaleDateString('en-PH', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
    });
}

function mediaUrl(value) {
    if (!value) return '';
    if (value.startsWith('http://') || value.startsWith('https://') || value.startsWith('/images/')) return value;
    if (value.startsWith('/uploads/')) return `http://localhost:8080${value}`;
    return value;
}

function OrderPhoto({ title, imageUrl }) {
    const [imageError, setImageError] = useState(false);
    if (!imageUrl) return null;
    return (
        <section className="border border-stone-200 bg-white/80">
            <div className="flex items-center gap-3 border-b border-stone-100 p-5">
                <Camera size={19} className="text-stone-500" />
                <h2 className="font-serif text-2xl text-stone-950">{title}</h2>
            </div>
            <div className="p-5">
                {imageError ? (
                    <div className="border border-dashed border-stone-300 bg-stone-50 p-6 text-sm text-stone-500">Unable to load this photo right now.</div>
                ) : (
                    <img src={mediaUrl(imageUrl)} alt={title} onError={() => setImageError(true)} className="max-h-[420px] w-full border border-stone-200 bg-stone-100 object-cover" />
                )}
            </div>
        </section>
    );
}

export default function ShippingInformation() {
    const { id } = useParams();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        const loadOrder = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await ordersAPI.getBuyerOrder(id);
                setOrder(response.data.data);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load shipping information');
            } finally {
                setLoading(false);
            }
        };

        loadOrder();
    }, [id]);

    const shipping = order?.shipping;

    const copyTrackingNumber = async () => {
        if (!shipping?.trackingNumber) return;
        await navigator.clipboard.writeText(shipping.trackingNumber);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 1600);
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900">
            <header className="sticky top-0 z-40 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="mx-auto flex h-20 max-w-4xl items-center justify-between px-6">
                    <Link to={`/orders/${id}`} className="inline-flex min-h-11 items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-950" aria-label="Back to order details">
                        <ArrowLeft size={18} />
                        Back
                    </Link>
                    <h1 className="font-serif text-xl tracking-tight">Shipping Information</h1>
                    <Link to="/dashboard" className="inline-flex min-h-11 items-center gap-2 font-serif text-lg tracking-tight text-stone-950" aria-label="Petal home">
                        <Leaf size={17} strokeWidth={1.5} />
                        Petal
                    </Link>
                </div>
            </header>

            <main className="mx-auto max-w-4xl px-6 py-10">
                {loading ? (
                    <div className="space-y-4">
                        <div className="h-36 animate-pulse bg-stone-100" />
                        <div className="h-72 animate-pulse bg-stone-100" />
                    </div>
                ) : error ? (
                    <div className="border border-red-200 bg-red-50 p-5 text-sm text-red-700">{error}</div>
                ) : (
                    <div className="space-y-5">
                        <section className="border border-stone-200 bg-white/80 p-6">
                            <p className="text-xs uppercase tracking-[0.18em] text-stone-400">Courier</p>
                            <h2 className="mt-2 font-serif text-4xl text-stone-950">{shipping?.courierName || 'Petal Local Delivery'}</h2>
                            <div className="mt-5 border border-stone-100 bg-stone-50 p-4">
                                <p className="text-xs uppercase tracking-wider text-stone-400">Tracking number</p>
                                <div className="mt-2 flex items-center justify-between gap-3">
                                    <p className="break-all text-sm font-semibold text-stone-900">{shipping?.trackingNumber}</p>
                                    <button onClick={copyTrackingNumber} className="inline-flex min-h-10 items-center gap-2 border border-stone-200 bg-white px-3 text-sm font-medium text-stone-800 hover:border-stone-900">
                                        {copied ? <Check size={15} /> : <Copy size={15} />}
                                        {copied ? 'Copied' : 'Copy'}
                                    </button>
                                </div>
                            </div>
                            <div className="mt-4 flex items-center justify-between gap-4 text-sm">
                                <span className="text-stone-500">Estimated delivery</span>
                                <span className="font-semibold text-stone-950">{formatDate(shipping?.estimatedDeliveryDate)}</span>
                            </div>
                        </section>

                        <section>
                            <h2 className="mb-4 font-serif text-2xl text-stone-950">Logistics Tracking</h2>
                            <TrackingTimeline events={shipping?.events || []} />
                        </section>

                        <OrderPhoto title="Bouquet preparation" imageUrl={order?.fulfillmentImageUrl || shipping?.fulfillmentImageUrl} />
                        <OrderPhoto title="Proof of Delivery" imageUrl={order?.proofImageUrl || shipping?.proofImageUrl} />
                    </div>
                )}
            </main>
        </div>
    );
}
