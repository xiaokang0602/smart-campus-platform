package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_score_analysis")
public class AiScoreAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examRecordId;

    private Long studentId;

    private Integer analysisScope;

    private String subject;

    private Long analystId;

    private String analysisContent;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
