package com.recruitment.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.search.document.JobDocument;
import com.recruitment.search.document.ResumeDocument;
import com.recruitment.search.service.SearchService;
import com.recruitment.search.vo.JobSearchVO;
import com.recruitment.search.vo.ResumeSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ObjectProvider<ElasticsearchOperations> elasticsearchOperationsProvider;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;

    @Override
    public PageResult<JobSearchVO> searchJobs(String keyword, long page, long size) {
        ElasticsearchOperations operations = getOperations();
        if (operations == null || !StringUtils.hasText(keyword)) {
            return PageResult.empty(page, size);
        }
        String normalizedKeyword = normalizeKeyword(keyword);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(normalizedKeyword)
                        .fields("jobName^3", "description^2", "requirements", "category", "city", "department")))
                .withPageable(PageRequest.of(toPageIndex(page), toPageSize(size)))
                .build();
        query.setHighlightQuery(highlight(JobDocument.class, "jobName", "description", "requirements"));

        SearchHits<JobDocument> hits = operations.search(query, JobDocument.class);
        List<JobSearchVO> records = hits.getSearchHits().stream()
                .map(this::toJobSearchVO)
                .toList();
        return new PageResult<>(records, hits.getTotalHits(), page, size);
    }

    @Override
    public PageResult<ResumeSearchVO> searchResumes(String keyword, long page, long size) {
        ElasticsearchOperations operations = getOperations();
        if (operations == null || !StringUtils.hasText(keyword)) {
            return PageResult.empty(page, size);
        }
        String normalizedKeyword = normalizeKeyword(keyword);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(normalizedKeyword)
                        .fields("parsedContent^3", "selfEvaluation^2", "aiAnalysis")))
                .withPageable(PageRequest.of(toPageIndex(page), toPageSize(size)))
                .build();
        query.setHighlightQuery(highlight(ResumeDocument.class, "parsedContent", "selfEvaluation", "aiAnalysis"));

        SearchHits<ResumeDocument> hits = operations.search(query, ResumeDocument.class);
        List<ResumeSearchVO> records = hits.getSearchHits().stream()
                .map(this::toResumeSearchVO)
                .toList();
        return new PageResult<>(records, hits.getTotalHits(), page, size);
    }

    @Override
    public void rebuildIndex() {
        ElasticsearchOperations operations = getOperations();
        if (operations == null) {
            log.warn("Skipped Elasticsearch index rebuild because ElasticsearchOperations is unavailable");
            return;
        }

        recreateIndex(operations.indexOps(JobDocument.class));
        recreateIndex(operations.indexOps(ResumeDocument.class));

        jobMapper.selectList(new LambdaQueryWrapper<Job>().eq(Job::getDeleted, 0))
                .forEach(job -> operations.save(toJobDocument(job)));
        resumeMapper.selectList(new LambdaQueryWrapper<Resume>().eq(Resume::getDeleted, 0))
                .forEach(resume -> operations.save(toResumeDocument(resume)));
    }

    @Override
    public void syncJob(Job job) {
        ElasticsearchOperations operations = getOperations();
        if (operations == null || job == null || job.getId() == null) {
            return;
        }
        if (Integer.valueOf(1).equals(job.getDeleted())) {
            deleteJob(job.getId());
            return;
        }
        operations.save(toJobDocument(job));
    }

    @Override
    public void deleteJob(Long jobId) {
        ElasticsearchOperations operations = getOperations();
        if (operations != null && jobId != null) {
            operations.delete(String.valueOf(jobId), JobDocument.class);
        }
    }

    @Override
    public void syncResume(Resume resume) {
        ElasticsearchOperations operations = getOperations();
        if (operations == null || resume == null || resume.getId() == null) {
            return;
        }
        if (Integer.valueOf(1).equals(resume.getDeleted())) {
            deleteResume(resume.getId());
            return;
        }
        operations.save(toResumeDocument(resume));
    }

    @Override
    public void deleteResume(Long resumeId) {
        ElasticsearchOperations operations = getOperations();
        if (operations != null && resumeId != null) {
            operations.delete(String.valueOf(resumeId), ResumeDocument.class);
        }
    }

    private ElasticsearchOperations getOperations() {
        return elasticsearchOperationsProvider.getIfAvailable();
    }

    private void recreateIndex(IndexOperations indexOperations) {
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping());
    }

    private HighlightQuery highlight(Class<?> type, String... fields) {
        List<HighlightField> highlightFields = Arrays.stream(fields)
                .map(HighlightField::new)
                .toList();
        return new HighlightQuery(new Highlight(highlightFields), type);
    }

    private int toPageIndex(long page) {
        return (int) Math.max(0, page - 1);
    }

    private int toPageSize(long size) {
        return (int) Math.max(1, Math.min(size, 100));
    }

    private String normalizeKeyword(String keyword) {
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private JobDocument toJobDocument(Job job) {
        JobDocument document = new JobDocument();
        BeanUtils.copyProperties(job, document);
        return document;
    }

    private ResumeDocument toResumeDocument(Resume resume) {
        ResumeDocument document = new ResumeDocument();
        BeanUtils.copyProperties(resume, document);
        return document;
    }

    private JobSearchVO toJobSearchVO(SearchHit<JobDocument> hit) {
        JobSearchVO vo = new JobSearchVO();
        BeanUtils.copyProperties(hit.getContent(), vo);
        vo.setHighlights(hit.getHighlightFields());
        return vo;
    }

    private ResumeSearchVO toResumeSearchVO(SearchHit<ResumeDocument> hit) {
        ResumeSearchVO vo = new ResumeSearchVO();
        BeanUtils.copyProperties(hit.getContent(), vo);
        vo.setHighlights(hit.getHighlightFields());
        return vo;
    }
}
