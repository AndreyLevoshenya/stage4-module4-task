import {getErrorMessage} from "../constants/errorCodes";
import {CONFIG} from "../config/constants";

const getAuthToken = () => localStorage.getItem("token");

const parseError = async (response) => {
    try {
        const data = await response.json();
        const backendMessage = data?.errorMessage || data?.message;
        const userFriendlyMessage = getErrorMessage(data?.errorCode, backendMessage);

        return {
            status: response.status,
            errorCode: data?.errorCode,
            errorMessage: userFriendlyMessage,
            httpStatus: data?.httpStatus,
        };
    } catch {
        return {
            status: response.status,
            errorMessage: `Request failed with status ${response.status}`,
        };
    }
};

export async function apiFetch(url, options = {}) {
    const headers = new Headers(options.headers || {});

    if (!headers.has("Content-Type") && options.body) {
        headers.set("Content-Type", "application/json");
    }

    const token = getAuthToken();
    if (token && !headers.has("Authorization")) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    const fetchOptions = {...options, headers};

    const response = await fetch(url, fetchOptions);

    if (!response.ok) {
        if (response.status === 401) {
            localStorage.removeItem("token");
            window.location.href = CONFIG.ROUTES.LOGIN;
        }

        const err = await parseError(response);
        const error = new Error(err.errorMessage || "Request failed");
        error.status = err.status;
        error.errorCode = err.errorCode;
        error.httpStatus = err.httpStatus;
        throw error;
    }

    const contentType = response.headers.get("Content-Type") || "";
    if (contentType.includes("application/json")) {
        return response.json();
    }
    const text = await response.text();
    if (!text || text.trim() === "") {
        return null;
    }
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

export const api = {
    get: (url) => apiFetch(url, {method: "GET"}),
    post: (url, body) => apiFetch(url, {method: "POST", body: JSON.stringify(body)}),
    patch: (url, body) => apiFetch(url, {method: "PATCH", body: JSON.stringify(body)}),
    delete: (url) => apiFetch(url, {method: "DELETE"}),
};
