import {api} from "./api";
import {buildApiUrl} from "../config/constants";

const API_URL = buildApiUrl("NEWS");

export const fetchNews = async ({page, size, sortField, sort, search}) => {
    const params = new URLSearchParams({
        page,
        size,
        sort: `${sortField},${sort}`,
    });
    if (search) params.append("search", search);

    return api.get(`${API_URL}?${params.toString()}`);
};

export const addNews = async (newsData) => {
    return api.post(API_URL, newsData);
};

export const editNews = async (id, newsData) => {
    return api.patch(`${API_URL}/${id}`, newsData);
};

export const deleteNews = async (id) => {
    return api.delete(`${API_URL}/${id}`);
};
