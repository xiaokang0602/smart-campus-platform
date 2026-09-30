package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("timetable")
public class Timetable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;

    private Integer week;

    private Integer period;

    private String subject;

    private Long teacherId;

    private String room;

    @TableLogic
    private Integer deleted;
}
