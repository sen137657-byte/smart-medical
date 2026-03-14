package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Consultation;
import com.smartmedical.mapper.ConsultationMapper;
import com.smartmedical.service.ConsultationService;
import org.springframework.stereotype.Service;

/**
 * 在线问诊业务实现
 */
@Service
public class ConsultationServiceImpl
        extends ServiceImpl<ConsultationMapper, Consultation>
        implements ConsultationService {
}