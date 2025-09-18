import React from "react";
import {Button, Form} from "react-bootstrap";
import "./styles/PaginationComponent.css"

function PaginationComponent({page, size, totalPages, onPageChange, onSizeChange}) {
    const renderPages = () => {
        const pages = [];
        const neighbors = 2;

        if (page > 0) pages.push(<Button key="first" size="sm" onClick={() => onPageChange(0)}> &lt;&lt; </Button>);
        if (page > 0) pages.push(<Button key="prev" size="sm" onClick={() => onPageChange(page - 1)}> &lt; </Button>);

        for (let p = Math.max(0, page - neighbors); p <= Math.min(totalPages - 1, page + neighbors); p++) {
            pages.push(
                <Button
                    key={p}
                    size="sm"
                    variant={p === page ? "primary" : "light"}
                    onClick={() => onPageChange(p)}
                >
                    {p + 1}
                </Button>
            );
        }

        if (page < totalPages - 1) pages.push(<Button key="next" size="sm"
                                                      onClick={() => onPageChange(page + 1)}> &gt; </Button>);
        if (page < totalPages - 1) pages.push(<Button key="last" size="sm"
                                                      onClick={() => onPageChange(totalPages - 1)}> &gt;&gt; </Button>);

        return pages;
    };

    return (
        <div className="pagination-wrapper">
            <div className="pagination-buttons">{renderPages()}</div>
            <Form.Select
                size="sm"
                value={size}
                onChange={(e) => onSizeChange(Number(e.target.value))}
                className="pagination-size"
            >
                <option value={10}>10 / page</option>
                <option value={20}>20 / page</option>
                <option value={50}>50 / page</option>
            </Form.Select>
        </div>
    );
}

export default PaginationComponent;
