package com.recruitment.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.entity.ResumeEducation;
import com.recruitment.resume.entity.ResumeExperience;
import com.recruitment.resume.mapper.ResumeEducationMapper;
import com.recruitment.resume.mapper.ResumeExperienceMapper;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历服务实现
 */
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeMapper resumeMapper;
    private final ResumeEducationMapper resumeEducationMapper;
    private final ResumeExperienceMapper resumeExperienceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getMyResume(Long userId) {
        Resume resume = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, userId)
                        .eq(Resume::getDeleted, 0));

        if (resume == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();

        // 从 parsedContent JSON 中解析完整简历数据（姓名、技能、自我评价等基础字段）
        if (resume.getParsedContent() != null && !resume.getParsedContent().isEmpty()) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(
                        resume.getParsedContent(),
                        new TypeReference<Map<String, Object>>() {});
                result.putAll(parsed);
            } catch (JsonProcessingException e) {
                // 解析失败时返回空
            }
        }

        // 教育经历、工作经历以子表为准，覆盖 JSON 中的旧值
        result.put("educations", loadEducations(resume.getId()));
        result.put("experiences", loadExperiences(resume.getId()));

        // 补充字段
        result.put("id", resume.getId());
        result.put("selfEvaluation", resume.getSelfEvaluation());
        result.put("aiScore", resume.getAiScore());
        result.put("aiAnalysis", resume.getAiAnalysis());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMyResume(Long userId, Map<String, Object> data) {
        Resume resume = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, userId)
                        .eq(Resume::getDeleted, 0));

        boolean isNew = (resume == null);
        if (isNew) {
            resume = new Resume();
            resume.setUserId(userId);
            resume.setDeleted(0);
            resume.setCreateTime(LocalDateTime.now());
        }

        // 将完整简历数据序列化为 JSON 存储到 parsedContent
        try {
            String json = objectMapper.writeValueAsString(data);
            resume.setParsedContent(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("简历数据序列化失败: " + e.getMessage());
        }

        // 保存自我评价
        if (data.containsKey("evaluation")) {
            resume.setSelfEvaluation(String.valueOf(data.get("evaluation")));
        } else if (data.containsKey("summary")) {
            resume.setSelfEvaluation(String.valueOf(data.get("summary")));
        }

        resume.setUpdateTime(LocalDateTime.now());

        if (isNew) {
            resumeMapper.insert(resume);
        } else {
            resumeMapper.updateById(resume);
        }

        // 同步教育经历、工作经历到子表
        Long resumeId = resume.getId();
        saveEducations(resumeId, data.get("educations"));
        saveExperiences(resumeId, data.get("experiences"));
    }

    /**
     * 保存教育经历子表：先清空旧数据，再全量插入
     */
    @SuppressWarnings("unchecked")
    private void saveEducations(Long resumeId, Object educationsObj) {
        resumeEducationMapper.delete(
                new LambdaQueryWrapper<ResumeEducation>().eq(ResumeEducation::getResumeId, resumeId));

        if (!(educationsObj instanceof List<?> list)) {
            return;
        }

        int order = 0;
        for (Object itemObj : list) {
            if (!(itemObj instanceof Map)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) itemObj;
            ResumeEducation edu = new ResumeEducation();
            edu.setResumeId(resumeId);
            edu.setSchool(asString(item.get("school")));
            edu.setMajor(asString(item.get("major")));
            edu.setDegree(asString(item.get("degree")));

            LocalDate[] range = parseTimeRange(asString(item.get("time")));
            edu.setStartDate(range[0]);
            edu.setEndDate(range[1]);
            edu.setIsHighest(0);
            edu.setSortOrder(order++);
            edu.setCreateTime(LocalDateTime.now());
            edu.setUpdateTime(LocalDateTime.now());
            resumeEducationMapper.insert(edu);
        }
    }

    /**
     * 保存工作经历子表：先清空旧数据，再全量插入
     */
    @SuppressWarnings("unchecked")
    private void saveExperiences(Long resumeId, Object experiencesObj) {
        resumeExperienceMapper.delete(
                new LambdaQueryWrapper<ResumeExperience>().eq(ResumeExperience::getResumeId, resumeId));

        if (!(experiencesObj instanceof List<?> list)) {
            return;
        }

        int order = 0;
        for (Object itemObj : list) {
            if (!(itemObj instanceof Map)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) itemObj;
            ResumeExperience exp = new ResumeExperience();
            exp.setResumeId(resumeId);
            exp.setCompany(asString(item.get("company")));
            exp.setPosition(asString(item.get("role")));

            LocalDate[] range = parseTimeRange(asString(item.get("time")));
            exp.setStartDate(range[0]);
            exp.setEndDate(range[1]);

            // details 是多行工作内容，用换行拼接后存入 description
            Object details = item.get("details");
            if (details instanceof List<?> detailList) {
                List<String> lines = new ArrayList<>();
                for (Object d : detailList) {
                    if (d != null) {
                        lines.add(String.valueOf(d));
                    }
                }
                exp.setDescription(String.join("\n", lines));
            } else {
                exp.setDescription(asString(item.get("description")));
            }

            exp.setSortOrder(order++);
            exp.setCreateTime(LocalDateTime.now());
            exp.setUpdateTime(LocalDateTime.now());
            resumeExperienceMapper.insert(exp);
        }
    }

    /**
     * 读取教育经历，转换为前端使用的结构 {school, major, degree, time}
     */
    private List<Map<String, Object>> loadEducations(Long resumeId) {
        List<ResumeEducation> list = resumeEducationMapper.selectList(
                new LambdaQueryWrapper<ResumeEducation>()
                        .eq(ResumeEducation::getResumeId, resumeId)
                        .orderByAsc(ResumeEducation::getSortOrder));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ResumeEducation edu : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("school", edu.getSchool());
            map.put("major", edu.getMajor());
            map.put("degree", edu.getDegree());
            map.put("time", formatTimeRange(edu.getStartDate(), edu.getEndDate()));
            result.add(map);
        }
        return result;
    }

    /**
     * 读取工作经历，转换为前端使用的结构 {company, role, time, details}
     */
    private List<Map<String, Object>> loadExperiences(Long resumeId) {
        List<ResumeExperience> list = resumeExperienceMapper.selectList(
                new LambdaQueryWrapper<ResumeExperience>()
                        .eq(ResumeExperience::getResumeId, resumeId)
                        .orderByAsc(ResumeExperience::getSortOrder));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ResumeExperience exp : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("company", exp.getCompany());
            map.put("role", exp.getPosition());
            map.put("time", formatTimeRange(exp.getStartDate(), exp.getEndDate()));

            List<String> details = new ArrayList<>();
            if (exp.getDescription() != null && !exp.getDescription().isEmpty()) {
                for (String line : exp.getDescription().split("\n")) {
                    if (!line.trim().isEmpty()) {
                        details.add(line);
                    }
                }
            }
            map.put("details", details);
            result.add(map);
        }
        return result;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 解析形如 "2022.09 - 2026.06" / "2022.09 - 至今" 的时间区间为起止日期。
     * @return LocalDate[2]，[0] 为开始日期，[1] 为结束日期（“至今”或缺省为 null）
     */
    private LocalDate[] parseTimeRange(String time) {
        LocalDate[] range = new LocalDate[]{null, null};
        if (time == null || time.trim().isEmpty()) {
            return range;
        }
        String[] parts = time.split("-");
        if (parts.length > 0) {
            range[0] = parseYearMonth(parts[0]);
        }
        if (parts.length > 1) {
            range[1] = parseYearMonth(parts[1]);
        }
        return range;
    }

    /**
     * 解析形如 "2022.09" / "2022" 的年月字符串为该月 1 号；"至今" 或非法值返回 null。
     */
    private LocalDate parseYearMonth(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty() || "至今".equals(s)) {
            return null;
        }
        try {
            String[] parts = s.split("\\.");
            int year = Integer.parseInt(parts[0].trim());
            int month = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
            if (month < 1 || month > 12) {
                month = 1;
            }
            return LocalDate.of(year, month, 1);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将起止日期格式化为 "2022.09 - 2026.06"；结束日期为空时展示为 "至今"。
     */
    private String formatTimeRange(LocalDate start, LocalDate end) {
        String startStr = formatYearMonth(start);
        String endStr = end == null ? "至今" : formatYearMonth(end);
        if (!startStr.isEmpty()) {
            return startStr + " - " + endStr;
        }
        return "至今".equals(endStr) ? "" : endStr;
    }

    private String formatYearMonth(LocalDate date) {
        if (date == null) {
            return "";
        }
        return String.format("%d.%02d", date.getYear(), date.getMonthValue());
    }
}
