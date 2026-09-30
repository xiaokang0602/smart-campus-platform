package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_change_apply")
public class TeacherChangeApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applyClassId;

    private String subject;

    private Long oldTeacherId;

    private Long newTeacherId;

    private Long applyUser;

    private String applyReason;

    private Integer auditStatus;

    private String auditComment;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
