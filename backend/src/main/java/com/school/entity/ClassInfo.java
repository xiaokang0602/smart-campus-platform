package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("class")
public class ClassInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String className;

    private String grade;

    private Long headTeacherId;

    private Long schoolId;

    @TableLogic
    private Integer deleted;
}
