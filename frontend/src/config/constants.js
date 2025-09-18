export const CONFIG = {
    API: {
        BASE_URL: process.env.REACT_APP_BACKEND_URL || "http://localhost:8080",
        ENDPOINTS: {
            AUTH: "/api/v1/auth",
            NEWS: "/api/v1/news",
            TAGS: "/api/v1/tags",
            AUTHORS: "/api/v1/authors",
            COMMENTS: "/api/v1/comments"
        }
    },
    UI: {
        DEBOUNCE_DELAY: 500,
        PAGINATION: {
            DEFAULT_PAGE: 0,
            DEFAULT_SIZE: 10,
            MAX_SIZE: 50
        }
    },
    VALIDATION: {
        USERNAME: {
            MIN_LENGTH: 3,
            MAX_LENGTH: 30
        },
        PASSWORD: {
            MIN_LENGTH: 4,
            MAX_LENGTH: 30
        },
        NAME: {
            MIN_LENGTH: 3,
            MAX_LENGTH: 32
        },
        COMMENT: {
            MIN_LENGTH: 5,
            MAX_LENGTH: 255
        },
        NEWS_TITLE: {
            MIN_LENGTH: 5,
            MAX_LENGTH: 30
        },
        NEWS_CONTENT: {
            MIN_LENGTH: 5,
            MAX_LENGTH: 255
        },
        TAG: {
            MIN_LENGTH: 3,
            MAX_LENGTH: 15
        }
    },
    ROUTES: {
        HOME: "/",
        LOGIN: "/login",
        REGISTER: "/register",
        NEWS: "/news",
        ABOUT: "/about",
        OAUTH_CALLBACK: "/oauth2/callback",
        NOT_FOUND: "/not-found"
    }
};

export const buildApiUrl = (endpoint, path = "") => {
    return `${CONFIG.API.BASE_URL}${CONFIG.API.ENDPOINTS[endpoint]}${path}`;
};
