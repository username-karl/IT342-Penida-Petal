import { ChevronRight, Copy, Truck } from 'lucide-react';
import { Link } from 'react-router-dom';

function formatDateTime(value) {
    if (!value) return 'No updates yet';
    return new Date(value).toLocaleString('en-PH', {
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
    });
}

export default function ShippingInfoCard({ orderId, shipping, to }) {
    const latestEvent = shipping?.events?.[0];
    const content = (
        <div className="border border-stone-200 bg-white/85 p-5 transition-colors hover:border-stone-900">
            <div className="flex items-center gap-4">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center border border-stone-900 bg-stone-900 text-white">
                    <Truck size={19} strokeWidth={1.7} />
                </div>
                <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-3">
                        <h3 className="font-serif text-xl text-stone-950">Shipping Information</h3>
                        {to && (
                            <span className="inline-flex min-h-10 items-center gap-1 text-xs font-semibold uppercase tracking-[0.16em] text-stone-900">
                                View <ChevronRight size={16} />
                            </span>
                        )}
                    </div>
                    <p className="mt-1 text-sm font-medium text-stone-800">{shipping?.latestStatus || 'Pending'}</p>
                    <div className="mt-4 grid grid-cols-1 gap-1 border-t border-stone-100 pt-3 text-xs text-stone-500 sm:grid-cols-2">
                        <span>{shipping?.courierName || 'Petal Local Delivery'}</span>
                        <span className="inline-flex items-center gap-1 sm:justify-end">
                            <Copy size={12} /> {shipping?.trackingNumber || `PETAL-${orderId}`}
                        </span>
                        <span className="sm:col-span-2">Latest update: {formatDateTime(latestEvent?.timestamp)}</span>
                    </div>
                </div>
            </div>
        </div>
    );

    return to ? <Link to={to} className="block">{content}</Link> : content;
}
