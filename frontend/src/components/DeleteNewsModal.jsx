import React, { useEffect, useCallback } from "react";
import "./styles/DeleteNewsModal.css";

function DeleteNewsModal({ isOpen, onClose, onDelete, newsTitle }) {
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
                <h3>Delete News</h3>
                <p>
                    Are you sure you want to delete the news: <strong>{newsTitle}</strong>?
                </p>

                <div className="modal-actions">
                    <button type="button" onClick={onClose}>
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={onDelete}
                        className="delete-btn"
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DeleteNewsModal;
