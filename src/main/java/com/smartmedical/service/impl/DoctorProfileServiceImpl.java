package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.mapper.DoctorProfileMapper;
import com.smartmedical.service.DoctorProfileService;
import org.springframework.stereotype.Service;

/**
 * 医生Service实现类
 */
@Service
public class DoctorProfileServiceImpl
        extends ServiceImpl<DoctorProfileMapper, DoctorProfile>
        implements DoctorProfileService {
}
