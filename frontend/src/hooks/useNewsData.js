import { useState, useEffect, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchNews } from "../services/NewsService";
import { CONFIG } from "../config/constants";

export const useNewsData = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    
    const [news, setNews] = useState([]);
    const [error, setError] = useState("");
    const [notFound, setNotFound] = useState(false);
    const [loading, setLoading] = useState(true);
    const [searchLoading, setSearchLoading] = useState(false);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [page, setPage] = useState(Number(searchParams.get("page") || CONFIG.UI.PAGINATION.DEFAULT_PAGE));
    const [size, setSize] = useState(Number(searchParams.get("size") || CONFIG.UI.PAGINATION.DEFAULT_SIZE));
    const [sortField, setSortField] = useState(searchParams.get("sortField") || "createDate");
    const [sort, setSort] = useState(searchParams.get("sort") || "DESC");
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [searchInput, setSearchInput] = useState(searchParams.get("search") || "");

    const loadNews = useCallback(async () => {
        setError("");
        if (page === CONFIG.UI.PAGINATION.DEFAULT_PAGE && size === CONFIG.UI.PAGINATION.DEFAULT_SIZE && sortField === "createDate" && sort === "DESC" && !search) {
            setLoading(true);
        } else {
            setSearchLoading(true);
        }
        
        try {
            const data = await fetchNews({ page, size, sortField, sort, search });
            setNews(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch (err) {
            if (err.status === 404 || err.errorCode === "000001") {
                setNotFound(true);
            } else {
                setError(err.message);
            }
        } finally {
            setLoading(false);
            setSearchLoading(false);
        }
    }, [page, size, sortField, sort, search]);

    useEffect(() => {
        loadNews();
    }, [loadNews]);

    useEffect(() => {
        const params = { page, size, sortField, sort };
        if (search) params.search = search;
        setSearchParams(params);
    }, [page, size, sortField, sort, search, setSearchParams]);

    return {
        news,
        error,
        notFound,
        loading,
        searchLoading,
        totalElements,
        totalPages,
        page,
        size,
        sortField,
        sort,
        search,
        searchInput,
        setPage,
        setSize,
        setSortField,
        setSort,
        setSearch,
        setSearchInput,
        loadNews
    };
};
