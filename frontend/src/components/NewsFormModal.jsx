import React, {useCallback, useEffect, useState} from "react";
import AsyncSelect from "react-select/async";
import {Button, Form} from "react-bootstrap";
import "./styles/AddNewsModal.css";
import {api} from "../services/api";
import {validateNewsContent, validateNewsTitle, validateTag} from "../utils/validation";
import {buildApiUrl} from "../config/constants";
import {showError} from "../utils/notifications";

function NewsFormModal({isOpen, onClose, onSave, initialData = null, isEdit = false}) {
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
        selectedTags: initialData?.tags?.map(tag => ({value: tag.id, label: tag.name})) || []
    });

    const [title, setTitle] = useState(getInitialState().title);
    const [content, setContent] = useState(getInitialState().content);
    const [selectedTags, setSelectedTags] = useState(getInitialState().selectedTags);
    const [errors, setErrors] = useState({});
    const [submitError, setSubmitError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoadingTags, setIsLoadingTags] = useState(false);

    useEffect(() => {
        if (isOpen) {
            const {title, content, selectedTags} = getInitialState();
            setTitle(title);
            setContent(content);
            setSelectedTags(selectedTags);
            setErrors({});
            setSubmitError(null);
        }
    }, [initialData, isOpen]);

    const loadTags = useCallback(async (inputValue) => {
        setIsLoadingTags(true);
        try {
            const data = await api.get(
                `${buildApiUrl("TAGS")}?search=${inputValue || ""}&page=0&size=20&sort=name,asc`
            );
            return data.content.map(tag => ({value: tag.id, label: tag.name}));
        } catch (err) {
            showError("Failed to load tags");
            return [];
        } finally {
            setIsLoadingTags(false);
        }
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;

        const newErrors = {};
        const titleError = validateNewsTitle(title);
        const contentError = validateNewsContent(content);

        if (titleError) newErrors.title = titleError;
        if (contentError) newErrors.content = contentError;

        selectedTags.forEach((tag, index) => {
            const label = tag?.label ?? "";
            const tagError = validateTag(label);
            if (tagError) newErrors[`tag-${index}`] = tagError;
        });

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsSubmitting(true);
        try {
            await onSave({
                title,
                content,
                tagIds: selectedTags.map(tag => tag.value),
            });
            onClose();
        } catch (err) {
            setSubmitError(err.message || "Failed to save news. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <h3>{isEdit ? "Edit News" : "Add News"}</h3>
                <Form onSubmit={handleSubmit}>
                    <Form.Group controlId="title" className="form-group">
                        <Form.Control
                            type="text"
                            placeholder="Title"
                            value={title}
                            onChange={e => {
                                const value = e.target.value;
                                setTitle(value);
                                setErrors(prev => ({...prev, title: validateNewsTitle(value)}));
                            }}
                            isInvalid={!!errors.title}
                            required
                            autoFocus
                        />
                        <Form.Control.Feedback type="invalid">{errors.title}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group controlId="content" className="form-group">
                        <Form.Control
                            as="textarea"
                            placeholder="Content"
                            value={content}
                            onChange={e => {
                                const value = e.target.value;
                                setContent(value);
                                setErrors(prev => ({...prev, content: validateNewsContent(value)}));
                            }}
                            isInvalid={!!errors.content}
                            required
                            rows={4}
                        />
                        <Form.Control.Feedback type="invalid">{errors.content}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group controlId="tags" className="form-group">
                        <Form.Label>Select tags:</Form.Label>
                        <AsyncSelect
                            isMulti
                            cacheOptions
                            defaultOptions
                            loadOptions={loadTags}
                            value={selectedTags}
                            onChange={(newTags) => {
                                const tagsArray = newTags || [];
                                setSelectedTags(tagsArray);

                                const newErrors = {};
                                tagsArray.forEach((tag, index) => {
                                    const label = tag?.label ?? "";
                                    const tagError = validateTag(label);
                                    if (tagError) newErrors[`tag-${index}`] = tagError;
                                });
                                setErrors(prev => ({...prev, ...newErrors}));
                            }}
                            placeholder={isLoadingTags ? "Loading tags..." : "Search and select tags..."}
                            isLoading={isLoadingTags}
                        />
                        {selectedTags.map((tag, index) =>
                            errors[`tag-${index}`] ? (
                                <p key={index} className="error-text">{errors[`tag-${index}`]}</p>
                            ) : null
                        )}
                    </Form.Group>

                    {submitError && <div className="alert alert-danger">{submitError}</div>}

                    <div className="modal-actions">
                        <Button type="button" variant="secondary" onClick={onClose} disabled={isSubmitting}>
                            Cancel
                        </Button>
                        <Button type="submit" variant="primary" disabled={isSubmitting}>
                            {isSubmitting ? "Saving..." : (isEdit ? "Update" : "Save")}
                        </Button>
                    </div>
                </Form>
            </div>
        </div>
    );
}

export default NewsFormModal;
