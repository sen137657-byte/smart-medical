package com.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmedical.entity.PatientProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientProfileMapper extends BaseMapper<PatientProfile> {
}
