package com.recruitment.search.service;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.job.entity.Job;
import com.recruitment.resume.entity.Resume;
import com.recruitment.search.vo.JobSearchVO;
import com.recruitment.search.vo.ResumeSearchVO;

public interface SearchService {

    PageResult<JobSearchVO> searchJobs(String keyword, long page, long size);

    PageResult<ResumeSearchVO> searchResumes(String keyword, long page, long size);

    void rebuildIndex();

    void syncJob(Job job);

    void deleteJob(Long jobId);

    void syncResume(Resume resume);

    void deleteResume(Long resumeId);
}
