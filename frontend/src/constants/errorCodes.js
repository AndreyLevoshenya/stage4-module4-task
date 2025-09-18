export const ERROR_CODES = {
    NEWS_DOES_NOT_EXIST: "000001",
    AUTHOR_DOES_NOT_EXIST: "000002",
    TAG_DOES_NOT_EXIST: "000003",
    COMMENT_DOES_NOT_EXIST: "000004",
    VALIDATION_EXCEPTION: "000005",
    API_VERSION_NOT_SUPPORTED: "000006",
    RESOURCE_NOT_FOUND: "000007",
    USER_DOES_NOT_EXIST: "000008",
    METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION: "000009",
    ENTITY_ALREADY_EXISTS: "000010",
    AUTHENTICATION_FAILED: "000011"
};

export const ERROR_MESSAGES = {
    [ERROR_CODES.NEWS_DOES_NOT_EXIST]: "News not found",
    [ERROR_CODES.AUTHOR_DOES_NOT_EXIST]: "Author not found",
    [ERROR_CODES.TAG_DOES_NOT_EXIST]: "Tag not found",
    [ERROR_CODES.COMMENT_DOES_NOT_EXIST]: "Comment not found",
    [ERROR_CODES.VALIDATION_EXCEPTION]: "Validation error",
    [ERROR_CODES.API_VERSION_NOT_SUPPORTED]: "API version not supported",
    [ERROR_CODES.RESOURCE_NOT_FOUND]: "Resource not found",
    [ERROR_CODES.USER_DOES_NOT_EXIST]: "User not found",
    [ERROR_CODES.METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION]: "Invalid ID format",
    [ERROR_CODES.ENTITY_ALREADY_EXISTS]: "This item already exists",
    [ERROR_CODES.AUTHENTICATION_FAILED]: "Authentication failed"
};

export const getErrorMessage = (errorCode, fallbackMessage) => {
    return ERROR_MESSAGES[errorCode] || fallbackMessage || "An error occurred";
};
