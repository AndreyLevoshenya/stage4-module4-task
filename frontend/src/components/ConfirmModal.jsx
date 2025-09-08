import React, { useEffect, useCallback } from "react";
import "./styles/ConfirmModal.css";

function ConfirmModal({
    isOpen,
    onClose,
    onConfirm,
    title = "Confirm action",
    message = "Are you sure you want to proceed?",
    confirmText = "Confirm",
    cancelText = "Cancel",
    confirmVariant = "danger" // "danger" | "primary"
}) {
    const handleKeyDown = useCallback((e) => {
        if (e.key === "Escape") onClose();
    }, [onClose]);

    useEffect(() => {
        if (isOpen) {
            document.addEventListener("keydown", handleKeyDown);
            return () => document.removeEventListener("keydown", handleKeyDown);
        }
    }, [isOpen, handleKeyDown]);

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <h3>{title}</h3>
                <p>{message}</p>

                <div className="modal-actions">
                    <button type="button" onClick={onClose} className="btn btn-ghost">
                        {cancelText}
                    </button>
                    <button
                        type="button"
                        onClick={onConfirm}
                        className={`btn ${confirmVariant === "danger" ? "btn-danger" : "btn-primary"}`}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ConfirmModal;


