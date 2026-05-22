import { Check, Circle } from 'lucide-react';

const TRACKING_STEPS = [
    {
        statuses: ['PENDING'],
        label: 'Order Placed',
        description: 'Your order has been received and is waiting for florist confirmation.',
    },
    {
        statuses: ['ACCEPTED', 'ARRANGING', 'PREPARING', 'READY_FOR_PICKUP'],
        label: 'Florist Preparing',
        description: 'The florist is preparing your bouquet for delivery.',
    },
    {
        statuses: ['OUT_FOR_DELIVERY', 'SHIPPED'],
        label: 'Out for Delivery',
        description: 'Your bouquet is on the way to the recipient.',
    },
    {
        statuses: ['DELIVERED', 'COMPLETED'],
        label: 'Delivered',
        description: 'Your bouquet has been delivered to the recipient.',
    },
];

function normalizeStatus(status) {
    const normalized = (status || 'PENDING').trim().toUpperCase().replaceAll(' ', '_');
    if (normalized === 'PREPARING') return 'ARRANGING';
    if (normalized === 'SHIPPED') return 'OUT_FOR_DELIVERY';
    if (normalized === 'COMPLETED') return 'DELIVERED';
    if (normalized === 'OUT_FOR_DELIVERY') return 'OUT_FOR_DELIVERY';
    if (normalized === 'READY_FOR_PICKUP') return 'READY_FOR_PICKUP';
    return normalized;
}

function currentStepIndex(status) {
    const normalized = normalizeStatus(status);
    return TRACKING_STEPS.findIndex((step) => step.statuses.includes(normalized));
}

function formatTimestamp(value) {
    if (!value) return '';
    return new Date(value).toLocaleString('en-PH', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
    });
}

function eventForStep(events, step) {
    return events.find((event) => step.statuses.includes(normalizeStatus(event.status)));
}

export default function OrderTrackingStepper({ status, events = [] }) {
    const activeIndex = currentStepIndex(status);
    const hasKnownStatus = activeIndex >= 0;

    return (
        <div className="border border-stone-200 bg-white/85 p-5">
            {!hasKnownStatus && (
                <div className="mb-5 border border-dashed border-stone-300 bg-stone-50 p-4 text-sm text-stone-500">
                    Tracking is using the latest order status while Petal confirms the next delivery update.
                </div>
            )}
            <ol className="space-y-0">
                {TRACKING_STEPS.map((step, index) => {
                    const isComplete = hasKnownStatus && index < activeIndex;
                    const isCurrent = hasKnownStatus && index === activeIndex;
                    const isFuture = !hasKnownStatus || index > activeIndex;
                    const event = eventForStep(events, step);
                    const description = event?.description || step.description;
                    const timestamp = event?.timestamp ? formatTimestamp(event.timestamp) : '';

                    return (
                        <li key={step.label} className="relative flex gap-4 pb-7 last:pb-0">
                            {index < TRACKING_STEPS.length - 1 && (
                                <span
                                    className={`absolute left-[15px] top-8 h-[calc(100%-2rem)] w-px ${isComplete ? 'bg-stone-900' : 'bg-stone-200'}`}
                                    aria-hidden="true"
                                />
                            )}
                            <span
                                className={`relative z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border ${
                                    isComplete
                                        ? 'border-stone-900 bg-stone-900 text-white'
                                        : isCurrent
                                            ? 'border-stone-900 bg-white text-stone-950 shadow-[0_0_0_4px_rgba(28,25,23,0.08)]'
                                            : 'border-stone-200 bg-stone-50 text-stone-300'
                                }`}
                                aria-hidden="true"
                            >
                                {isComplete ? <Check size={16} /> : <Circle size={10} fill={isCurrent ? 'currentColor' : 'none'} />}
                            </span>
                            <div className="min-w-0 pt-1">
                                <div className="flex flex-wrap items-center gap-2">
                                    <h3 className={`text-sm font-semibold ${isFuture ? 'text-stone-400' : 'text-stone-950'}`}>{step.label}</h3>
                                    {isCurrent && (
                                        <span className="border border-stone-900 bg-stone-900 px-2 py-0.5 text-[11px] font-medium uppercase tracking-[0.14em] text-white">
                                            Current
                                        </span>
                                    )}
                                </div>
                                <p className={`mt-1 text-sm leading-relaxed ${isFuture ? 'text-stone-400' : 'text-stone-500'}`}>{description}</p>
                                {timestamp && !isFuture && (
                                    <time className="mt-2 block text-xs text-stone-400">
                                        {timestamp}
                                    </time>
                                )}
                            </div>
                        </li>
                    );
                })}
            </ol>
        </div>
    );
}
