package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("class_rep")
public class ClassRep {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long teacherId;

    private String subject;

    private Long classId;

    private Integer auditStatus;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
