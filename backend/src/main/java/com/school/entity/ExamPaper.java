package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_paper")
public class ExamPaper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private String paperName;

    private String subject;

    private String grade;

    private Integer totalScore;

    private Integer examTime;

    private Long createTeacher;

    private Integer source;

    private Integer status;

    private String classIds;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
