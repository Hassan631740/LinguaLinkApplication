package com.lingualink.util;

import com.lingualink.dto.PageParams;
import org.springframework.data.domain.Sort;

public class PaginationUtil {
    
    public static PageParams parsePageParams(Integer page, Integer size, String sortBy, String sortDir) {
        PageParams pageParams = new PageParams();
        
        if (page != null) {
            pageParams.setPage(page);
        }
        
        if (size != null) {
            pageParams.setSize(size);
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            pageParams.setSortBy(sortBy);
        }
        
        if (sortDir != null && !sortDir.isEmpty()) {
            pageParams.setSortDir(sortDir);
        }
        
        return pageParams;
    }
}

