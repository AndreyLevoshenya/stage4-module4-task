import {ERROR_CODES} from "../constants/errorCodes";

export const handleError = (error, actions = {}) => {
    const {errorCode, status} = error;

    if (errorCode === ERROR_CODES.AUTHENTICATION_FAILED || status === 401) {
        if (actions.onAuthError) {
            actions.onAuthError(error);
        }
        return;
    }

    if (errorCode === ERROR_CODES.NEWS_DOES_NOT_EXIST ||
        errorCode === ERROR_CODES.AUTHOR_DOES_NOT_EXIST ||
        errorCode === ERROR_CODES.RESOURCE_NOT_FOUND ||
        status === 404) {
        if (actions.onNotFound) {
            actions.onNotFound(error);
        }
        return;
    }

    if (errorCode === ERROR_CODES.VALIDATION_EXCEPTION) {
        if (actions.onValidationError) {
            actions.onValidationError(error);
        }
        return;
    }

    if (errorCode === ERROR_CODES.ENTITY_ALREADY_EXISTS) {
        if (actions.onConflictError) {
            actions.onConflictError(error);
        }
        return;
    }

    if (actions.onGenericError) {
        actions.onGenericError(error);
    }
};

export const isErrorType = (error, errorCode) => {
    return error.errorCode === errorCode;
};

export const isAuthError = (error) => {
    return error.errorCode === ERROR_CODES.AUTHENTICATION_FAILED || error.status === 401;
};

export const isNotFoundError = (error) => {
    return error.errorCode === ERROR_CODES.NEWS_DOES_NOT_EXIST ||
        error.errorCode === ERROR_CODES.AUTHOR_DOES_NOT_EXIST ||
        error.errorCode === ERROR_CODES.RESOURCE_NOT_FOUND ||
        error.status === 404;
};
