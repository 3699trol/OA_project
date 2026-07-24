package com.recruitment.job.vo;

import com.recruitment.job.entity.Job;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 职位推荐视图对象：在职位信息基础上附带技能重叠度匹配分与命中的技能标签。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobRecommendVO extends Job {

    /** 技能重叠度匹配分（0~100），越高越匹配 */
    private Double matchScore;

    /** 命中的技能标签（用户技能与职位技能的交集） */
    private List<String> matchedSkills;
}
