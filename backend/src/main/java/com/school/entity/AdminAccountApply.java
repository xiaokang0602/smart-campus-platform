package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_account_apply")
public class AdminAccountApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applySchoolId;

    private Long applicantId;

    private String newUsername;

    private String newRealName;

    private String menuPerms;

    private String applyReason;

    private Integer auditStatus;

    private Long auditUser;

    private String auditComment;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
