package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("teacher_job")
public class TeacherJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private Long classId;

    private String subject;

    private Integer isHeadTeacher;

    @TableLogic
    private Integer deleted;
}
