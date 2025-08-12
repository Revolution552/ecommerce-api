"use client";

import React from 'react';

interface MessageDisplayProps {
    type: 'success' | 'error' | 'info'; // Added 'info' to the type definition
    message: string | null;
    extraContent?: React.ReactNode; // Optional for additional content like auth token or suggestions
}

const MessageDisplay: React.FC<MessageDisplayProps> = ({ type, message, extraContent }) => {
    if (!message) return null;

    const baseClasses = "mt-6 p-4 border-l-4 rounded-lg shadow-sm";
    const successClasses = "bg-green-100 border-green-500 text-green-700";
    const errorClasses = "bg-red-100 border-red-500 text-red-700";
    const infoClasses = "bg-blue-100 border-blue-500 text-blue-700"; // Added info classes

    let currentClasses = '';
    let headerText = '';

    switch (type) {
        case 'success':
            currentClasses = successClasses;
            headerText = 'Success:';
            break;
        case 'error':
            currentClasses = errorClasses;
            headerText = 'Error:';
            break;
        case 'info': // Handle 'info' type
            currentClasses = infoClasses;
            headerText = 'Info:';
            break;
        default:
            currentClasses = infoClasses; // Default to info if type is not recognized
            headerText = 'Info:';
    }

    return (
        <div className={`${baseClasses} ${currentClasses}`}>
            <p className="font-semibold">{headerText}</p>
            <p>{message}</p>
            {extraContent && <div className="mt-4">{extraContent}</div>}
        </div>
    );
};

export default MessageDisplay;
