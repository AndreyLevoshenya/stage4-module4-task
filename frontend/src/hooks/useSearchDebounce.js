import { useState, useEffect } from "react";
import { CONFIG } from "../config/constants";

export const useSearchDebounce = (searchInput, setSearch, setPage) => {
    const [searchLoading, setSearchLoading] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => {
            if (searchInput !== searchInput) {
                setSearchLoading(true);
                setSearch(searchInput);
                setPage(CONFIG.UI.PAGINATION.DEFAULT_PAGE);
            }
        }, CONFIG.UI.DEBOUNCE_DELAY);

        return () => clearTimeout(timer);
    }, [searchInput, setSearch, setPage]);

    return { searchLoading, setSearchLoading };
};
