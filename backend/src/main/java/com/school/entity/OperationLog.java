package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private Long operatorId;

    private Integer operatorRole;

    private String module;

    private String action;

    private String targetType;

    private Long targetId;

    private String changeDiff;

    private String ip;

    private String userAgent;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
