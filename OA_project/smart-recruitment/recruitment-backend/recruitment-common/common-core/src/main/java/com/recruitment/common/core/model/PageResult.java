package com.recruitment.common.core.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果对象
 * 统一约定：所有 /list 接口均返回 Result<PageResult<T>>，
 * 查询参数统一使用 page（从1开始）与 size
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long pageNum, long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public long getTotalPages() {
        return (total + pageSize - 1) / pageSize;
    }

    /**
     * 构造一个空分页结果，供尚未实现业务逻辑的接口占位返回
     */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
    }
}
