import { ERROR_CODES } from "../constants/errorCodes";

// Handle specific error codes with custom actions
export const handleError = (error, actions = {}) => {
    const { errorCode, status } = error;
    
    // Authentication errors
    if (errorCode === ERROR_CODES.AUTHENTICATION_FAILED || status === 401) {
        if (actions.onAuthError) {
            actions.onAuthError(error);
        }
        return;
    }
    
    // Not found errors
    if (errorCode === ERROR_CODES.NEWS_DOES_NOT_EXIST || 
        errorCode === ERROR_CODES.AUTHOR_DOES_NOT_EXIST ||
        errorCode === ERROR_CODES.RESOURCE_NOT_FOUND ||
        status === 404) {
        if (actions.onNotFound) {
            actions.onNotFound(error);
        }
        return;
    }
    
    // Validation errors
    if (errorCode === ERROR_CODES.VALIDATION_EXCEPTION) {
        if (actions.onValidationError) {
            actions.onValidationError(error);
        }
        return;
    }
    
    // Entity already exists
    if (errorCode === ERROR_CODES.ENTITY_ALREADY_EXISTS) {
        if (actions.onConflictError) {
            actions.onConflictError(error);
        }
        return;
    }
    
    // Generic error handling
    if (actions.onGenericError) {
        actions.onGenericError(error);
    }
};

// Check if error is a specific type
export const isErrorType = (error, errorCode) => {
    return error.errorCode === errorCode;
};

// Check if error is authentication related
export const isAuthError = (error) => {
    return error.errorCode === ERROR_CODES.AUTHENTICATION_FAILED || error.status === 401;
};

// Check if error is not found related
export const isNotFoundError = (error) => {
    return error.errorCode === ERROR_CODES.NEWS_DOES_NOT_EXIST ||
           error.errorCode === ERROR_CODES.AUTHOR_DOES_NOT_EXIST ||
           error.errorCode === ERROR_CODES.RESOURCE_NOT_FOUND ||
           error.status === 404;
};


