package com.recruitment.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        // 从 parsedContent JSON 中解析完整简历数据
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

        // 补充字段
        result.put("id", resume.getId());
        result.put("selfEvaluation", resume.getSelfEvaluation());
        result.put("aiScore", resume.getAiScore());
        result.put("aiAnalysis", resume.getAiAnalysis());

        return result;
    }

    @Override
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
    }
}
