import {CONFIG} from '../config/constants';

const validateField = (value, fieldName, minLength, maxLength) => {
    if (!value.trim()) return `${fieldName} cannot be blank`;
    if (value.length < minLength || value.length > maxLength) {
        return `${fieldName} must be between ${minLength} and ${maxLength} characters`;
    }
    return "";
};

export const validateUsername = (value) => {
    return validateField(value, "Username", CONFIG.VALIDATION.USERNAME.MIN_LENGTH, CONFIG.VALIDATION.USERNAME.MAX_LENGTH);
};

export const validatePassword = (value) => {
    return validateField(value, "Password", CONFIG.VALIDATION.PASSWORD.MIN_LENGTH, CONFIG.VALIDATION.PASSWORD.MAX_LENGTH);
};

export const validateFirstname = (value) => {
    return validateField(value, "Firstname", CONFIG.VALIDATION.NAME.MIN_LENGTH, CONFIG.VALIDATION.NAME.MAX_LENGTH);
};

export const validateLastname = (value) => {
    return validateField(value, "Lastname", CONFIG.VALIDATION.NAME.MIN_LENGTH, CONFIG.VALIDATION.NAME.MAX_LENGTH);
};

export const validateComment = (value) => {
    return validateField(value, "Comment", CONFIG.VALIDATION.COMMENT.MIN_LENGTH, CONFIG.VALIDATION.COMMENT.MAX_LENGTH);
};

export const validateNewsTitle = (value) => {
    return validateField(value, "Title", CONFIG.VALIDATION.NEWS_TITLE.MIN_LENGTH, CONFIG.VALIDATION.NEWS_TITLE.MAX_LENGTH);
};

export const validateNewsContent = (value) => {
    return validateField(value, "Content", CONFIG.VALIDATION.NEWS_CONTENT.MIN_LENGTH, CONFIG.VALIDATION.NEWS_CONTENT.MAX_LENGTH);
};

export const validateTag = (value) => {
    return validateField(value, "Tag", CONFIG.VALIDATION.TAG.MIN_LENGTH, CONFIG.VALIDATION.TAG.MAX_LENGTH);
};


