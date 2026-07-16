package com.recruitment.search.controller;

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
    public Result<?> searchJob(@RequestParam String keyword) {
        // TODO: ES职位搜索
        return Result.success();
    }

    @GetMapping("/resume")
    public Result<?> searchResume(@RequestParam String keyword) {
        // TODO: ES简历搜索
        return Result.success();
    }

    @PostMapping("/rebuild-index")
    public Result<?> rebuildIndex() {
        // TODO: 重建索引
        return Result.success();
    }
}
