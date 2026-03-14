package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 科室实体类
 * 对应数据库 dept 表
 */
@Data
@TableName("dept")
public class Dept {

    @TableId(type = IdType.AUTO)
    private Long id;//科室编号（主键）

    private String name;//科室名称

    private String description;//科室描述信息
}
