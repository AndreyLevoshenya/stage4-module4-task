const getAuthToken = () => localStorage.getItem("token");

const parseError = async (response) => {
    try {
        const data = await response.json();
        // Backend returns { errorCode, errorMessage, httpStatus, timestamp }
        const backendMessage = data?.errorMessage || data?.message;
        return {
            status: response.status,
            errorCode: data?.errorCode,
            errorMessage: backendMessage || `Request failed with status ${response.status}`,
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

    const fetchOptions = { ...options, headers };

    const response = await fetch(url, fetchOptions);

    if (!response.ok) {
        if (response.status === 401) {
            // Unauthenticated -> redirect to login
            localStorage.removeItem("token");
            // Use hard redirect to break out of protected route
            window.location.href = "/login";
            // Also throw to reject awaiting callers
        }

        const err = await parseError(response);
        const error = new Error(err.errorMessage || "Request failed");
        error.status = err.status;
        error.errorCode = err.errorCode;
        error.httpStatus = err.httpStatus;
        throw error;
    }

    // Try to parse JSON, fallback to text
    const contentType = response.headers.get("Content-Type") || "";
    if (contentType.includes("application/json")) {
        return response.json();
    }
    // Fallback: try parse JSON even if header is missing
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
    get: (url) => apiFetch(url, { method: "GET" }),
    post: (url, body) => apiFetch(url, { method: "POST", body: JSON.stringify(body) }),
    patch: (url, body) => apiFetch(url, { method: "PATCH", body: JSON.stringify(body) }),
    delete: (url) => apiFetch(url, { method: "DELETE" }),
};


