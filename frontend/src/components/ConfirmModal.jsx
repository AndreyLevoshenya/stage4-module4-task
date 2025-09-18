import React, {useCallback, useEffect} from "react";
import "./styles/ConfirmModal.css";
import {Button} from "react-bootstrap";

function ConfirmModal({
                          isOpen,
                          onClose,
                          onConfirm,
                          title = "Confirm action",
                          message = "Are you sure you want to proceed?",
                          confirmText = "Confirm",
                          cancelText = "Cancel"
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

                <div className="confirm-actions-panel">
                    <Button type="button" variant="secondary" onClick={onClose}>
                        {cancelText}
                    </Button>
                    <Button type="button" variant="danger" onClick={onConfirm}>
                        {confirmText}
                    </Button>
                </div>
            </div>
        </div>
    );
}

export default ConfirmModal;
