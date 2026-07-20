package com.recruitment.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.file.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录 Mapper
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
}
