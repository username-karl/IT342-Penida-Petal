import { useEffect, useState } from 'react';
import { Check, CheckCircle2, Send, Truck } from 'lucide-react';
import Stepper, { Step } from './Stepper';

const workflowSteps = [
    { value: 'PENDING', label: 'Pending', helper: 'Automatic' },
    { value: 'ACCEPTED', label: 'Accepted', helper: 'Florist confirmed' },
    { value: 'ARRANGING', label: 'Arranging', helper: 'Bouquet in progress' },
    { value: 'READY_FOR_PICKUP', label: 'Ready for Pickup', helper: 'Awaiting courier' },
    { value: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', helper: 'With rider' },
    { value: 'DELIVERED', label: 'Delivered', helper: 'Completed' },
];

const actionLabels = {
    ACCEPTED: 'Accept Order',
    ARRANGING: 'Mark as Arranging',
    READY_FOR_PICKUP: 'Mark as Ready for Pickup',
    OUT_FOR_DELIVERY: 'Mark as Out for Delivery',
    DELIVERED: 'Mark as Delivered',
};

export function normalizeDeliveryStatus(status) {
    if (status === 'PREPARING') return 'ARRANGING';
    if (status === 'SHIPPED') return 'OUT_FOR_DELIVERY';
    if (status === 'COMPLETED') return 'DELIVERED';
    return status || 'PENDING';
}

export function getNextDeliveryStatus(status) {
    const current = normalizeDeliveryStatus(status);
    const index = workflowSteps.findIndex((step) => step.value === current);
    if (index < 0 || index >= workflowSteps.length - 1) return null;
    return workflowSteps[index + 1].value;
}

export function getDefaultTrackingMessage(status) {
    const messages = {
        PENDING: 'Your order has been received and is waiting for florist confirmation.',
        ACCEPTED: 'The florist has accepted your order.',
        ARRANGING: 'The florist is preparing your bouquet.',
        READY_FOR_PICKUP: 'Your bouquet is ready for courier pickup.',
        OUT_FOR_DELIVERY: 'Your bouquet is on the way to the recipient.',
        DELIVERED: 'Your bouquet has been successfully delivered to the recipient.',
        CANCELLED: 'This order has been cancelled.',
    };
    return messages[normalizeDeliveryStatus(status)] || 'Tracking information has been updated.';
}

function localDateTimeValue() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
}

function automaticTrackingNumber(order) {
    return order?.shipping?.trackingNumber || `PETAL-${String(order?.id || '').padStart(4, '0')}`;
}

function editableCourierName(order) {
    const courierName = order?.shipping?.courierName || '';
    return courierName === 'Petal Local Delivery' ? '' : courierName;
}

function formatDateTime(value) {
    if (!value) return 'No timestamp yet';
    return new Date(value).toLocaleString('en-PH', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
    });
}

export default function UpdateShippingStatusForm({ order, onSubmit }) {
    const currentStatus = normalizeDeliveryStatus(order?.status);
    const currentIndex = workflowSteps.findIndex((step) => step.value === currentStatus);
    const nextStatus = getNextDeliveryStatus(currentStatus);
    const currentStep = currentIndex >= 0 ? currentIndex + 1 : 1;
    const nextStep = workflowSteps.find((step) => step.value === nextStatus);
    const currentStepMeta = workflowSteps.find((step) => step.value === currentStatus) || workflowSteps[0];
    const needsCourier = nextStatus === 'OUT_FOR_DELIVERY';
    const trackingNumber = automaticTrackingNumber(order);
    const latestEvent = order?.shipping?.events?.[0];
    const [form, setForm] = useState({
        courierName: editableCourierName(order),
        estimatedDeliveryDate: order?.shipping?.estimatedDeliveryDate || order?.deliveryDate || '',
        timestamp: localDateTimeValue(),
    });
    const [saving, setSaving] = useState(false);
    const [success, setSuccess] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        setForm((current) => ({
            ...current,
            courierName: editableCourierName(order) || current.courierName || '',
            estimatedDeliveryDate: order?.shipping?.estimatedDeliveryDate || order?.deliveryDate || current.estimatedDeliveryDate || '',
            timestamp: localDateTimeValue(),
        }));
        setSuccess('');
        setError('');
    }, [order?.id, order?.status, order?.deliveryDate, order?.shipping?.courierName, order?.shipping?.estimatedDeliveryDate]);

    const update = (field, value) => setForm((current) => ({ ...current, [field]: value }));

    const submit = async (event) => {
        event.preventDefault();
        if (!nextStatus) return;
        setSaving(true);
        setSuccess('');
        setError('');
        try {
            await onSubmit({
                ...form,
                courierName: needsCourier ? form.courierName : order?.shipping?.courierName || 'Petal Local Delivery',
                trackingNumber,
                deliveryStatus: nextStatus,
                trackingMessage: '',
                timestamp: `${form.timestamp}:00`,
                estimatedDeliveryDate: form.estimatedDeliveryDate || null,
            });
            setSuccess('Shipping update saved.');
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to update shipping status');
        } finally {
            setSaving(false);
        }
    };

    return (
        <form onSubmit={submit} className="border border-stone-200 bg-white p-5">
            <h3 className="font-serif text-2xl text-stone-950">Update Shipping Status</h3>

            <div className="mt-5 border-y border-stone-100 py-5">
                <Stepper
                    currentStep={currentStep}
                    hideFooter
                    disableStepIndicators
                    stepCircleContainerClassName="shadow-none"
                    stepContainerClassName="px-4 py-4"
                    contentClassName="text-sm"
                    renderStepIndicator={({ step, currentStep: activeStep }) => {
                        const complete = step < activeStep;
                        const active = step === activeStep;
                        return (
                            <div className="flex shrink-0 flex-col items-center">
                                <span className={`flex h-8 w-8 items-center justify-center rounded-full border text-xs font-semibold ${active ? 'border-stone-950 bg-stone-950 text-white' : complete ? 'border-green-700 bg-green-700 text-white' : 'border-stone-200 bg-stone-50 text-stone-400'}`}>
                                    {complete ? <Check size={15} /> : step}
                                </span>
                            </div>
                        );
                    }}
                >
                    {workflowSteps.map((step) => (
                        <Step key={step.value}>
                            <p className="text-xs uppercase tracking-[0.18em] text-stone-400">Current step</p>
                            <div className="mt-2 flex items-start justify-between gap-4">
                                <div>
                                    <p className="font-serif text-xl text-stone-950">{currentStepMeta.label}</p>
                                    <p className="mt-1 text-sm text-stone-500">{currentStepMeta.helper}</p>
                                </div>
                                <Truck className="mt-1 text-stone-400" size={19} strokeWidth={1.7} />
                            </div>
                            {nextStep ? (
                                <p className="mt-4 text-xs leading-relaxed text-stone-500">
                                    Next action: <span className="font-semibold text-stone-900">{nextStep.label}</span>. Pending is automatic and locked.
                                </p>
                            ) : (
                                <div className="mt-5 border border-green-200 bg-green-50 p-4">
                                    <div className="flex items-start gap-3">
                                        <CheckCircle2 className="mt-0.5 text-green-800" size={20} strokeWidth={1.8} />
                                        <div>
                                            <p className="text-sm font-semibold text-green-950">Order delivered</p>
                                            <p className="mt-1 text-xs leading-relaxed text-green-800">
                                                No further delivery updates are needed. Latest update: {formatDateTime(latestEvent?.timestamp)}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            )}
                            <div className="mt-5 space-y-4">
                                {needsCourier && (
                                    <label className="block">
                                        <span className="text-xs uppercase tracking-wider text-stone-500">Courier name</span>
                                        <input required value={form.courierName} onChange={(event) => update('courierName', event.target.value)} placeholder="Petal Cebu Rider" className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 text-sm outline-none focus:border-stone-900" />
                                    </label>
                                )}
                                {nextStatus && (
                                    <>
                                        <div className="block">
                                            <span className="text-xs uppercase tracking-wider text-stone-500">Tracking number</span>
                                            <div className="mt-2 flex min-h-11 items-center border border-stone-200 bg-stone-100 px-3 text-sm font-medium text-stone-600">
                                                {trackingNumber}
                                            </div>
                                            <span className="mt-2 block text-xs leading-relaxed text-stone-500">Generated automatically from the order number.</span>
                                        </div>
                                        <label className="block">
                                            <span className="text-xs uppercase tracking-wider text-stone-500">Estimated delivery</span>
                                            <input type="date" value={form.estimatedDeliveryDate || ''} onChange={(event) => update('estimatedDeliveryDate', event.target.value)} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 text-sm outline-none focus:border-stone-900" />
                                        </label>
                                        <label className="block">
                                            <span className="text-xs uppercase tracking-wider text-stone-500">Date/time</span>
                                            <input required type="datetime-local" value={form.timestamp} onChange={(event) => update('timestamp', event.target.value)} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 text-sm outline-none focus:border-stone-900" />
                                        </label>
                                    </>
                                )}
                            </div>
                            {success && <p className="mt-4 border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">{success}</p>}
                            {error && <p className="mt-4 border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
                            {nextStatus && (
                                <button disabled={saving} className="mt-5 inline-flex min-h-11 w-full items-center justify-center gap-2 bg-stone-950 px-4 text-sm font-semibold text-white hover:bg-stone-800 disabled:cursor-not-allowed disabled:bg-stone-200 disabled:text-stone-500">
                                    <Send size={16} /> {saving ? 'Saving...' : actionLabels[nextStatus]}
                                </button>
                            )}
                        </Step>
                    ))}
                </Stepper>
            </div>
        </form>
    );
}
