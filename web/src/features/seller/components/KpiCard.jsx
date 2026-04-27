import React from 'react';
import { ArrowRight } from 'lucide-react';

export default function KpiCard({ title, value, onClick }) {
    return (
        <div
            className="bg-white p-8 border border-stone-200 rounded-sm hover:border-stone-400 transition-colors cursor-pointer group"
            onClick={onClick}
        >
            <h3 className="font-serif text-xl mb-4 text-stone-900 flex justify-between items-center">
                {title}
                <ArrowRight size={16} className="text-stone-400 group-hover:text-stone-900 group-hover:translate-x-1 transition-all" />
            </h3>
            <p className="text-4xl font-light text-stone-600">{value}</p>
        </div>
    );
}
