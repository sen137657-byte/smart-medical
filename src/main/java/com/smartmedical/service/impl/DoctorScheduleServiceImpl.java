package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.DoctorSchedule;
import com.smartmedical.mapper.DoctorScheduleMapper;
import com.smartmedical.service.DoctorScheduleService;
import org.springframework.stereotype.Service;

@Service
public class DoctorScheduleServiceImpl
        extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule>
        implements DoctorScheduleService {
}