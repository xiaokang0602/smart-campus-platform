package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("security_question")
public class SecurityQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String question;

    private String answerHash;

    @TableLogic
    private Integer deleted;
}
