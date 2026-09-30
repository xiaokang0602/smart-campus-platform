package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private String subject;

    private Integer questionType;

    private String grade;

    private String title;

    private String options;

    private String answer;

    private String analysis;

    private Integer difficulty;

    private String knowledgePoint;

    private Long createTeacher;

    private Integer source;

    private String aiBatchNo;

    private Integer reviewStatus;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
