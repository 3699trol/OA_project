package com.recruitment.common.core.model;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象
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
}
