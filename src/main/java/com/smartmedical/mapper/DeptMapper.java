package com.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmedical.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室数据访问层 Mapper
 * 负责操作数据库 dept 表
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}
