package com.school.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结构
 */
@Data
public class PageResult<T> implements Serializable {

    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> records;

    public static <T> PageResult<T> of(Page<?> page, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = page.getTotal();
        r.pageNum = page.getCurrent();
        r.pageSize = page.getSize();
        r.records = records;
        return r;
    }

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.pageNum = pageNum;
        r.pageSize = pageSize;
        r.records = records;
        return r;
    }
}
