package com.recruitment.search.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    @GetMapping("/job")
    public Result<PageResult<?>> searchJob(@RequestParam(name = "keyword") String keyword,
                                            @RequestParam(name = "page", defaultValue = "1") long page,
                                            @RequestParam(name = "size", defaultValue = "10") long size) {
        // TODO: ES职位搜索
        return Result.success(PageResult.empty(page, size));
    }

    @GetMapping("/resume")
    public Result<PageResult<?>> searchResume(@RequestParam(name = "keyword") String keyword,
                                               @RequestParam(name = "page", defaultValue = "1") long page,
                                               @RequestParam(name = "size", defaultValue = "10") long size) {
        // TODO: ES简历搜索
        return Result.success(PageResult.empty(page, size));
    }

    @PostMapping("/rebuild-index")
    public Result<?> rebuildIndex() {
        // TODO: 重建索引
        return Result.success();
    }
}
