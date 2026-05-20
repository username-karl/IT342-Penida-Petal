export default function TrackingTimelineItem({ event, latest, last }) {
    return (
        <li className="relative flex gap-4">
            <div className="flex flex-col items-center">
                <span className={`mt-1 h-3.5 w-3.5 rounded-full border-2 ${latest ? 'border-stone-950 bg-stone-950' : 'border-stone-300 bg-white'}`} />
                {!last && <span className="mt-1 h-full min-h-14 w-px bg-stone-200" />}
            </div>
            <div className="pb-7">
                <h4 className={`text-sm font-semibold ${latest ? 'text-stone-950' : 'text-stone-600'}`}>{event.status}</h4>
                <p className={`mt-1 text-sm leading-relaxed ${latest ? 'text-stone-700' : 'text-stone-500'}`}>{event.description}</p>
                <time className="mt-2 block text-xs text-stone-400">
                    {new Date(event.timestamp).toLocaleString('en-PH', {
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric',
                        hour: 'numeric',
                        minute: '2-digit',
                    })}
                </time>
            </div>
        </li>
    );
}
