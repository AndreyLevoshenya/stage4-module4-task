const API_URL = "http://localhost:8080/api/v1/news";

const getToken = () => localStorage.getItem("token");

export const fetchNews = async ({ page, size, sortField, sort, search }) => {
    const token = getToken();
    const params = new URLSearchParams({
        page,
        size,
        sort: `${sortField},${sort}`,
    });
    if (search) params.append("search", search);

    const response = await fetch(`${API_URL}?${params.toString()}`, {
        headers: { Authorization: `Bearer ${token}` },
    });

    if (!response.ok) throw new Error("Failed to load news");
    return response.json();
};

export const addNews = async (newsData) => {
    const token = getToken();
    const response = await fetch(API_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(newsData),
    });

    if (!response.ok) {
        const errData = await response.json();
        throw new Error(errData.message || "Failed to add news");
    }
    return response.json();
};

export const editNews = async (id, newsData) => {
    const token = getToken();
    const response = await fetch(`${API_URL}/${id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(newsData),
    });

    if (!response.ok) {
        const errData = await response.json();
        throw new Error(errData.message || "Failed to update news");
    }
    return response.json();
};

export const deleteNews = async (id) => {
    const token = getToken();
    const response = await fetch(`${API_URL}/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
    });

    if (!response.ok) throw new Error("Failed to delete news");
};
