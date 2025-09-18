import React, { useState } from "react";
import { Button } from "react-bootstrap";
import { Pencil, Trash } from "lucide-react";
import NewsFormModal from "./NewsFormModal";
import ConfirmModal from "./ConfirmModal";
import { editNews, deleteNews } from "../services/NewsService";
import "./styles/NewsActionsPanel.css"

function NewsActionsPanel({ newsItem, onAfterEdit, onAfterDelete }) {
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const openEdit = (e) => { e.stopPropagation(); setShowEditModal(true); };
    const openDelete = (e) => { e.stopPropagation(); setShowDeleteModal(true); };

    const handleSaveEdited = async (newsData) => {
        setLoading(true);
        setError(null);
        try {
            const updated = await editNews(newsItem.id, newsData);
            setShowEditModal(false);
            if (onAfterEdit) onAfterEdit(updated);
        } catch (err) {
            setError(err.message || "Failed to update news");
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmDelete = async () => {
        setLoading(true);
        setError(null);
        try {
            await deleteNews(newsItem.id);
            setShowDeleteModal(false);
            if (onAfterDelete) onAfterDelete(newsItem);
        } catch (err) {
            setError(err.message || "Failed to delete news");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <div className="news-actions">
                <Button className="news-actions-button" onClick={openEdit} aria-label="Edit" title="Edit" >
                    <Pencil size={16} />
                </Button>
                <Button className="news-actions-button" variant="danger" onClick={openDelete} aria-label="Delete" title="Delete" >
                    <Trash size={16} />
                </Button>
            </div>

            {showEditModal && (
                <NewsFormModal
                    isOpen={showEditModal}
                    onClose={() => setShowEditModal(false)}
                    onSave={handleSaveEdited}
                    initialData={{ ...newsItem, tags: newsItem.tagDtoResponseList }}
                    isEdit
                />
            )}

            {showDeleteModal && (
                <ConfirmModal
                    isOpen={showDeleteModal}
                    onClose={() => setShowDeleteModal(false)}
                    onConfirm={handleConfirmDelete}
                    title="Delete"
                    message={`Are you sure you want to delete: "${newsItem.title}"?`}
                    confirmText="Delete"
                    cancelText="Cancel"
                    confirmVariant="danger"
                />
            )}

            {error && <div className="news-actions-error">{error}</div>}
        </div>
    );
}

export default NewsActionsPanel;
