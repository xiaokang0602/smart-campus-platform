package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student_award")
public class StudentAward {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String awardName;

    private LocalDate awardTime;

    private String awardLevel;

    private Long createBy;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
