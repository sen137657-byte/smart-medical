package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Appointment;
import com.smartmedical.mapper.AppointmentMapper;
import com.smartmedical.service.AppointmentService;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl
        extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {
}