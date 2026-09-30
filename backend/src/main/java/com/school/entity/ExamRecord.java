package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long studentId;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private BigDecimal totalScore;

    private Integer status;

    private Integer cheatSwitchCount;

    private Integer maxAllowedSwitch;

    private Integer reopenCount;

    private Long lastReopenBy;

    private LocalDateTime lastReopenTime;

    @TableLogic
    private Integer deleted;
}
