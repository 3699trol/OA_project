package com.recruitment.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
