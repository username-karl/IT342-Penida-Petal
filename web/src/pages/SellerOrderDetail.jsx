import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, ClipboardList, Leaf } from 'lucide-react';
import OrderStatusBadge from '../components/OrderStatusBadge';
import ShippingInfoCard from '../components/ShippingInfoCard';
import TrackingTimeline from '../components/TrackingTimeline';
import UpdateShippingStatusForm from '../components/UpdateShippingStatusForm';
import { ordersAPI } from '../services/api';

const paymentLabels = { COD: 'COD', GCASH: 'GCash', MAYA: 'Maya', CARD: 'Card' };
function currency(value) {
    return `PHP ${Number(value || 0).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function deliveryLabel(order) {
    if (!order?.deliveryDate) return order?.timeSlot || 'No delivery window';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} / ${order.timeSlot}`;
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
                            <ShippingInfoCard orderId={order.id} shipping={order.shipping} />
                        </aside>
                    </div>
                )}
            </main>
        </div>
    );
}
