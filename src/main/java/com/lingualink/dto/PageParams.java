package com.lingualink.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageParams {
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private Sort.Direction sortDir = Sort.Direction.ASC;

    public PageParams() {
    }

    public PageParams(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageParams(int page, int size, String sortBy, Sort.Direction sortDir) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(sortDir, sortBy));
    }

    public Pageable toPageable(String defaultSortBy) {
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : defaultSortBy;
        return PageRequest.of(page, size, Sort.by(sortDir, sortField));
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page); // Ensure non-negative
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.max(1, Math.min(size, 100)); // Clamp between 1 and 100
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Sort.Direction getSortDir() {
        return sortDir;
    }

    public void setSortDir(Sort.Direction sortDir) {
        this.sortDir = sortDir;
    }

    public void setSortDir(String direction) {
        if (direction != null) {
            this.sortDir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        }
    }
}

