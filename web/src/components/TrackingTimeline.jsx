import { PackageSearch } from 'lucide-react';
import TrackingTimelineItem from './TrackingTimelineItem';

export default function TrackingTimeline({ events = [] }) {
    if (!events.length) {
        return (
            <div className="border border-dashed border-stone-300 bg-white/80 p-8 text-center">
                <PackageSearch className="mx-auto text-stone-400" size={30} strokeWidth={1.5} />
                <h3 className="mt-4 font-serif text-2xl text-stone-950">No tracking updates yet</h3>
                <p className="mt-2 text-sm leading-relaxed text-stone-500">
                    Tracking information will appear once the florist or courier updates the order.
                </p>
            </div>
        );
    }

    return (
        <ol className="border border-stone-200 bg-white/85 p-5">
            {events.map((event, index) => (
                <TrackingTimelineItem
                    key={event.id || `${event.status}-${event.timestamp}`}
                    event={event}
                    latest={index === 0}
                    last={index === events.length - 1}
                />
            ))}
        </ol>
    );
}
