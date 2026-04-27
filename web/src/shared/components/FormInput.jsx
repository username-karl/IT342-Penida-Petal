import React from 'react';

export default function FormInput({ label, type = 'text', value, onChange, placeholder, required = false, extraLabel }) {
    return (
        <div className="space-y-2">
            <div className="flex justify-between items-center">
                <label className="text-xs uppercase tracking-wider text-stone-500 font-medium">{label}</label>
                {extraLabel && extraLabel}
            </div>
            <input
                type={type}
                required={required}
                value={value}
                onChange={onChange}
                className="w-full bg-white border border-stone-200 rounded-sm px-4 py-3 text-stone-900 placeholder:text-stone-300 focus:outline-none focus:border-stone-900 transition-colors"
                placeholder={placeholder}
            />
        </div>
    );
}
