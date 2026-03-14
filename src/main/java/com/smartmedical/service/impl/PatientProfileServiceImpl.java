package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.PatientProfile;
import com.smartmedical.mapper.PatientProfileMapper;
import com.smartmedical.service.PatientProfileService;
import org.springframework.stereotype.Service;

@Service
public class PatientProfileServiceImpl extends ServiceImpl<PatientProfileMapper, PatientProfile>
        implements PatientProfileService {
}
