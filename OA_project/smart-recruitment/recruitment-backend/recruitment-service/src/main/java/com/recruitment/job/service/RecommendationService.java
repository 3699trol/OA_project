package com.recruitment.job.service;

import com.recruitment.job.vo.JobRecommendVO;

import java.util.List;

/**
 * 基于技能标签重叠度的职位推荐服务。
 */
public interface RecommendationService {

    /**
     * 根据当前登录用户的简历技能标签，与在招职位技能标签计算重叠度，返回匹配度最高的若干职位。
     * 用户尚无简历或技能为空时，回退为最新在招职位。
     *
     * @param userId 当前登录用户 ID
     * @param limit  返回数量上限
     */
    List<JobRecommendVO> recommendJobs(Long userId, int limit);
}
