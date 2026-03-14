package com.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmedical.entity.DoctorProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生Mapper
 */
@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {
}
