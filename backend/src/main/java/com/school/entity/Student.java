package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("student")
public class Student {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long classId;

    private String studentNo;

    private String duty;

    private String gender;

    private LocalDate birthday;

    private String archiveNote;

    @TableLogic
    private Integer deleted;
}
