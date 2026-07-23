package com.recruitment.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.application.entity.JobApplication;
import com.recruitment.application.mapper.JobApplicationMapper;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ObjectProvider<ElasticsearchOperations> elasticsearchOperationsProvider;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<JobSearchVO> searchJobs(String keyword, long page, long size) {
        ElasticsearchOperations operations = getOperations();
        if (operations == null || !StringUtils.hasText(keyword)) {
            return PageResult.empty(page, size);
        }
        ensureIndex(operations, operations.indexOps(JobDocument.class), JobDocument.class);
        String normalizedKeyword = normalizeKeyword(keyword);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(normalizedKeyword)
                        .fields("jobName^3", "education^2", "experience^2", "requirements^2", "skills^2", "description", "category", "city")))
                .withPageable(PageRequest.of(toPageIndex(page), toPageSize(size)))
                .build();
        query.setHighlightQuery(highlight(JobDocument.class, "jobName", "description", "requirements", "skills"));

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
        ensureIndex(operations, operations.indexOps(ResumeDocument.class), ResumeDocument.class);
        String normalizedKeyword = normalizeKeyword(keyword);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(normalizedKeyword)
                        .fields("education", "experience", "skills")))
                .withPageable(PageRequest.of(toPageIndex(page), toPageSize(size)))
                .build();
        query.setHighlightQuery(highlight(ResumeDocument.class, "education", "experience", "skills"));

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

    /**
     * 确保索引存在，不存在则自动创建并从 MySQL 同步数据
     * 如果索引已存在但 mapping 过时，则删除重建
     */
    private <T> void ensureIndex(ElasticsearchOperations operations, IndexOperations indexOperations, Class<T> documentClass) {
        boolean needsRebuild = false;
        if (!indexOperations.exists()) {
            log.info("Elasticsearch index not found, creating and syncing from MySQL...");
            needsRebuild = true;
        } else {
            // 检查 mapping 是否包含新字段（education, experience, skills）
            try {
                var mapping = indexOperations.getMapping();
                if (documentClass == ResumeDocument.class) {
                    // ES mapping 结构为 {"properties": {"education": {...}, ...}}
                    // 需要检查 properties 下是否包含 education 字段
                    if (mapping != null) {
                        Object properties = mapping.get("properties");
                        if (properties instanceof Map<?, ?> props) {
                            if (!props.containsKey("education")) {
                                log.info("Resume index mapping is outdated (missing education field), rebuilding...");
                                needsRebuild = true;
                            }
                        } else {
                            // properties 不是 Map 类型，说明 mapping 结构异常，需要重建
                            log.info("Resume index mapping structure is unexpected, rebuilding...");
                            needsRebuild = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to check index mapping: {}", e.getMessage(), e);
                // 检查失败时，为了安全起见，重建索引
                needsRebuild = true;
            }
        }

        if (needsRebuild) {
            if (indexOperations.exists()) {
                indexOperations.delete();
            }
            indexOperations.create();
            indexOperations.putMapping(indexOperations.createMapping());
            // 根据文档类型从 MySQL 同步数据
            if (documentClass == JobDocument.class) {
                long count = jobMapper.selectCount(new LambdaQueryWrapper<Job>().eq(Job::getDeleted, 0));
                jobMapper.selectList(new LambdaQueryWrapper<Job>().eq(Job::getDeleted, 0))
                        .forEach(job -> operations.save(toJobDocument(job)));
                log.info("Synced {} jobs to job_index", count);
            } else if (documentClass == ResumeDocument.class) {
                long count = resumeMapper.selectCount(new LambdaQueryWrapper<Resume>().eq(Resume::getDeleted, 0));
                resumeMapper.selectList(new LambdaQueryWrapper<Resume>().eq(Resume::getDeleted, 0))
                        .forEach(resume -> operations.save(toResumeDocument(resume)));
                log.info("Synced {} resumes to resume_index", count);
            }
        }
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
        // 从 parsedContent JSON 中提取教育经历、工作经历、技能标签
        extractResumeFields(resume, document);
        return document;
    }

    @SuppressWarnings("unchecked")
    private void extractResumeFields(Resume resume, ResumeDocument document) {
        if (resume.getParsedContent() == null || resume.getParsedContent().isEmpty()) {
            return;
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    resume.getParsedContent(),
                    new TypeReference<Map<String, Object>>() {});

            // 教育经历：拼接学校、专业、学历
            if (parsed.get("educations") instanceof List<?> educations) {
                StringBuilder sb = new StringBuilder();
                for (Object edu : educations) {
                    if (edu instanceof Map<?, ?> map) {
                        if (sb.length() > 0) sb.append(" ");
                        if (map.get("school") != null) sb.append(map.get("school"));
                        if (map.get("major") != null) sb.append(" ").append(map.get("major"));
                        if (map.get("degree") != null) sb.append(" ").append(map.get("degree"));
                    }
                }
                document.setEducation(sb.toString());
            }

            // 工作经历：拼接公司、职位、描述
            if (parsed.get("experiences") instanceof List<?> experiences) {
                StringBuilder sb = new StringBuilder();
                for (Object exp : experiences) {
                    if (exp instanceof Map<?, ?> map) {
                        if (sb.length() > 0) sb.append(" ");
                        if (map.get("company") != null) sb.append(map.get("company"));
                        if (map.get("role") != null) sb.append(" ").append(map.get("role"));
                        if (map.get("description") != null) sb.append(" ").append(map.get("description"));
                    }
                }
                document.setExperience(sb.toString());
            }

            // 技能标签：拼接所有技能
            if (parsed.get("skills") instanceof List<?> skills) {
                document.setSkills(String.join(" ", skills.stream()
                        .filter(s -> s != null)
                        .map(Object::toString)
                        .toList()));
            }
        } catch (Exception e) {
            log.warn("Failed to extract resume fields from parsedContent: {}", e.getMessage());
        }
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
        // 查询候选人姓名
        ResumeDocument doc = hit.getContent();
        if (doc.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(doc.getUserId());
            if (user != null) {
                vo.setCandidateName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
            // 查询关联的投递记录ID
            JobApplication app = jobApplicationMapper.selectOne(
                    new LambdaQueryWrapper<JobApplication>()
                            .eq(JobApplication::getUserId, doc.getUserId())
                            .eq(JobApplication::getDeleted, 0)
                            .orderByDesc(JobApplication::getCreateTime)
                            .last("LIMIT 1"));
            if (app != null) {
                vo.setApplicationId(app.getId());
            }
        }
        return vo;
    }
}
