import React from 'react';
import { ArrowRight } from 'lucide-react';

export default function PrimaryButton({ children, loading, loadingText = 'Loading...', type = 'button', className = '' }) {
    return (
        <button
            type={type}
            disabled={loading}
            className={`w-full bg-stone-900 text-white h-12 rounded-sm font-medium hover:bg-stone-800 transition-all disabled:opacity-70 flex items-center justify-center gap-2 group ${className}`}
        >
            {loading ? loadingText : children}
            {!loading && <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />}
        </button>
    );
}
