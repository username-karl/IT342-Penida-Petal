import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Gift } from 'lucide-react';

export function currency(value) {
    const number = Number(value || 0);
    return `PHP ${number.toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function statusLabel(status) {
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

export function statusClass(status) {
    if (status === 'COMPLETED' || status === 'DELIVERED' || status === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (status === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (status === 'ACCEPTED' || status === 'ARRANGING' || status === 'PREPARING') return 'bg-amber-50 text-amber-800 border-amber-200';
    if (status === 'OUT_FOR_DELIVERY') return 'bg-stone-900 text-white border-stone-900';
    return 'bg-rose-50 text-rose-800 border-rose-200';
}

export function paymentLabel(value) {
    const labels = {
        COD: 'Cash on Delivery',
        GCASH: 'GCash',
        MAYA: 'Maya',
        CARD: 'Card',
    };
    return labels[value] || value;
}

export function deliveryLabel(order) {
    if (!order.deliveryDate) return order.timeSlot || 'Delivery pending';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} / ${order.timeSlot}`;
}

function ProductThumbnail({ src, alt, className = 'h-16 w-14' }) {
    const [failed, setFailed] = useState(false);

    if (!src || failed) {
        return (
            <div className={`${className} flex shrink-0 items-center justify-center border border-stone-200 bg-[#F7F1EA] text-stone-500`}>
                <Gift size={20} strokeWidth={1.5} />
            </div>
        );
    }

    return (
        <img
            src={src}
            alt={alt}
            loading="lazy"
            onError={() => setFailed(true)}
            className={`${className} shrink-0 border border-stone-200 bg-stone-100 object-cover object-center`}
        />
    );
}

export default function PurchaseHistoryCard({ order, compact = false }) {
    const orderItems = order.items || [];
    const primaryItem = orderItems[0] || null;
    const additionalItems = orderItems.slice(1);
    const displayName = primaryItem?.productName || order.itemSummary;

    return (
        <Link to={`/orders/${order.id}`} className="group block border border-stone-200 bg-white p-4 transition-colors hover:border-stone-300 hover:bg-[#FFFDF9] sm:p-5" aria-label={`View details for ${order.orderNumber}`}>
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div className="flex min-w-0 gap-4">
                    <ProductThumbnail src={primaryItem?.imageUrl} alt={displayName} />
                    <div className="min-w-0">
                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                        <h4 className="mt-1 font-serif text-xl leading-tight text-stone-950">{displayName}</h4>
                        <dl className="mt-2 grid gap-1 text-xs text-stone-500 sm:grid-cols-2 sm:gap-x-5">
                            <div className="min-w-0">
                                <dt className="sr-only">Recipient</dt>
                                <dd className="truncate">To {order.recipientName}</dd>
                            </div>
                            <div className="min-w-0">
                                <dt className="sr-only">Delivery</dt>
                                <dd className="truncate">{deliveryLabel(order)}</dd>
                            </div>
                            <div className="min-w-0">
                                <dt className="sr-only">Payment</dt>
                                <dd className="truncate">Payment: {paymentLabel(order.paymentMethod)}</dd>
                            </div>
                            {primaryItem && (
                                <div className="min-w-0 sm:col-span-2">
                                    <dt className="sr-only">Primary gift item</dt>
                                    <dd className="truncate">{primaryItem.floristName} / Qty {primaryItem.quantity} / {currency(primaryItem.lineTotal)}</dd>
                                </div>
                            )}
                        </dl>
                    </div>
                </div>
                <div className="flex items-center justify-between gap-4 sm:block sm:text-right">
                    <span className={`inline-flex border px-2.5 py-1 text-xs font-medium leading-none ${statusClass(order.status)}`}>
                        {statusLabel(order.status)}
                    </span>
                    <p className="font-semibold tabular-nums text-stone-950 sm:mt-2">{currency(order.totalAmount)}</p>
                </div>
            </div>

            {!compact && additionalItems.length > 0 && (
                <div className="mt-4 grid grid-cols-1 gap-3 border-t border-stone-100 pt-4 sm:grid-cols-2">
                    {additionalItems.map((item) => (
                        <div key={`${order.id}-${item.productId}`} className="flex min-w-0 gap-3">
                            <ProductThumbnail src={item.imageUrl} alt={item.productName} className="h-14 w-12" />
                            <div className="min-w-0">
                                <p className="truncate text-sm font-medium text-stone-900">{item.productName}</p>
                                <p className="text-xs text-stone-500">{item.floristName}</p>
                                <p className="text-xs text-stone-400">Qty {item.quantity} / {currency(item.lineTotal)}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <div className="mt-4 flex justify-end border-t border-stone-100 pt-3">
                <span className="text-xs font-medium uppercase tracking-widest text-stone-500 transition-colors group-hover:text-stone-900">View details</span>
            </div>
        </Link>
    );
}
