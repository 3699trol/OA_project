package com.recruitment.search.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.search.service.SearchService;
import com.recruitment.search.vo.JobSearchVO;
import com.recruitment.search.vo.ResumeSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/job")
    public Result<PageResult<JobSearchVO>> searchJob(@RequestParam(name = "keyword") String keyword,
                                                     @RequestParam(name = "page", defaultValue = "1") long page,
                                                     @RequestParam(name = "size", defaultValue = "10") long size) {
        return Result.success(searchService.searchJobs(keyword, page, size));
    }

    @GetMapping("/resume")
    public Result<PageResult<ResumeSearchVO>> searchResume(@RequestParam(name = "keyword") String keyword,
                                                           @RequestParam(name = "page", defaultValue = "1") long page,
                                                           @RequestParam(name = "size", defaultValue = "10") long size) {
        return Result.success(searchService.searchResumes(keyword, page, size));
    }

    @PostMapping("/rebuild-index")
    public Result<String> rebuildIndex() {
        searchService.rebuildIndex();
        return Result.success("ES索引重建完成");
    }
}
