import React, { useState, useEffect, useCallback } from "react";
import AsyncSelect from "react-select/async";
import "./styles/AddNewsModal.css";
import { api } from "../services/api";

function NewsFormModal({ isOpen, onClose, onSave, initialData = null, isEdit = false }) {
    const handleKeyDown = useCallback((e) => {
        if (e.key === "Escape") onClose();
    }, [onClose]);

    useEffect(() => {
        if (isOpen) {
            document.addEventListener("keydown", handleKeyDown);
            return () => document.removeEventListener("keydown", handleKeyDown);
        }
    }, [isOpen, handleKeyDown]);

    const getInitialState = () => ({
        title: initialData?.title || "",
        content: initialData?.content || "",
        selectedTags: initialData?.tags?.map(tag => ({ value: tag.id, label: tag.name })) || []
    });

    const [title, setTitle] = useState(getInitialState().title);
    const [content, setContent] = useState(getInitialState().content);
    const [selectedTags, setSelectedTags] = useState(getInitialState().selectedTags);
    const [errors, setErrors] = useState({});
    const [submitError, setSubmitError] = useState(null);

    useEffect(() => {
        if (isOpen) {
            const { title, content, selectedTags } = getInitialState();
            setTitle(title);
            setContent(content);
            setSelectedTags(selectedTags);
            setErrors({});
            setSubmitError(null);
        }
    }, [initialData, isOpen]);

    const validateTitle = (value) => {
        if (!value.trim()) return "Title is required.";
        if (value.length < 6 || value.length > 30) return "Title must be between 6 and 30 characters.";
        return "";
    };

    const validateContent = (value) => {
        if (!value.trim()) return "Content is required.";
        if (value.length < 12 || value.length > 1000) return "Content must be between 12 and 1000 characters.";
        return "";
    };

    const validateTag = (tag) => {
        if (tag.label.length < 3 || tag.label.length > 15) {
            return `Tag "${tag.label}" must be between 3 and 15 characters.`;
        }
        return "";
    };

    const loadTags = useCallback(async (inputValue) => {
        try {
            const data = await api.get(
                `http://localhost:8080/api/v1/tags?search=${inputValue || ""}&page=0&size=20&sort=name,asc`
            );
            return data.content.map(tag => ({ value: tag.id, label: tag.name }));
        } catch (err) {
            console.error(err);
            return [];
        }
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const newErrors = {};
        const titleError = validateTitle(title);
        const contentError = validateContent(content);

        if (titleError) newErrors.title = titleError;
        if (contentError) newErrors.content = contentError;

        selectedTags.forEach((tag, index) => {
            const tagError = validateTag(tag);
            if (tagError) newErrors[`tag-${index}`] = tagError;
        });

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        try {
            await onSave({
                title,
                content,
                tagIds: selectedTags.map(tag => tag.value),
            });
            onClose();
            window.location.reload();
        } catch (err) {
            console.error(err);
            setSubmitError(err.message || "Failed to save news. Please try again.");
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <h3>{isEdit ? "Edit News" : "Add News"}</h3>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Title"
                        value={title}
                        onChange={e => {
                            const value = e.target.value;
                            setTitle(value);
                            setErrors(prev => ({ ...prev, title: validateTitle(value) }));
                        }}
                        required
                        autoFocus
                    />
                    {errors.title && <p className="error-text">{errors.title}</p>}

                    <textarea
                        placeholder="Content"
                        value={content}
                        onChange={e => {
                            const value = e.target.value;
                            setContent(value);
                            setErrors(prev => ({ ...prev, content: validateContent(value) }));
                        }}
                        required
                    />
                    {errors.content && <p className="error-text">{errors.content}</p>}

                    <div className="tags-section">
                        <label>Select tags:</label>
                        <AsyncSelect
                            isMulti
                            cacheOptions
                            defaultOptions
                            loadOptions={loadTags}
                            value={selectedTags}
                            onChange={(newTags) => {
                                setSelectedTags(newTags);
                                const newErrors = {};
                                newTags.forEach((tag, index) => {
                                    const tagError = validateTag(tag);
                                    if (tagError) newErrors[`tag-${index}`] = tagError;
                                });
                                setErrors(prev => ({ ...prev, ...newErrors }));
                            }}
                            placeholder="Search and select tags..."
                        />
                        {selectedTags.map((tag, index) =>
                            errors[`tag-${index}`] ? (
                                <p key={index} className="error-text">{errors[`tag-${index}`]}</p>
                            ) : null
                        )}
                    </div>

                    {submitError && <p className="error-text">{submitError}</p>}

                    <div className="modal-actions">
                        <button type="button" onClick={onClose} className="btn btn-ghost">Cancel</button>
                        <button type="submit" className="btn btn-primary">{isEdit ? "Update" : "Save"}</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default NewsFormModal;


