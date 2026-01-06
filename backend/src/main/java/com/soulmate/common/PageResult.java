package com.soulmate.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 总页数
     */
    private int pages;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageResult<>(list, total, page, size, pages);
    }
}

