package com.recruitment.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "resume_index")
public class ResumeDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String parsedContent;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String selfEvaluation;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String aiAnalysis;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String education; // 教育经历（学校+专业+学历）

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String experience; // 工作经历（公司+职位+描述）

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String skills; // 技能标签

    @Field(type = FieldType.Double)
    private BigDecimal aiScore;

    @Field(type = FieldType.Keyword)
    private String fileUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
