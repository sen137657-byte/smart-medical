package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.vo.ConsultationReplyVO;
import com.smartmedical.entity.ConsultationReply;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.mapper.ConsultationReplyMapper;
import com.smartmedical.service.ConsultationReplyService;
import com.smartmedical.service.DoctorProfileService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsultationReplyServiceImpl
        extends ServiceImpl<ConsultationReplyMapper, ConsultationReply>
        implements ConsultationReplyService {

    private final DoctorProfileService doctorProfileService;

    public ConsultationReplyServiceImpl(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    @Override
    public List<ConsultationReplyVO> listReplyVO(Long consultationId) {

        // 1) 查回复记录
        List<ConsultationReply> replies = this.list(
                new LambdaQueryWrapper<ConsultationReply>()
                        .eq(ConsultationReply::getConsultationId, consultationId)
                        .orderByAsc(ConsultationReply::getCreateTime)
        );

        if (replies == null || replies.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) 批量查医生姓名（避免循环查库）
        Set<Long> doctorIds = replies.stream()
                .map(ConsultationReply::getDoctorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> doctorNameMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<DoctorProfile> doctors = doctorProfileService.listByIds(doctorIds);
            for (DoctorProfile d : doctors) {
                doctorNameMap.put(d.getId(), d.getName());
            }
        }

        // 3) 组装 VO：字段对齐前端 doctorName / content / replyTime
        List<ConsultationReplyVO> voList = new ArrayList<>();
        for (ConsultationReply r : replies) {
            ConsultationReplyVO vo = new ConsultationReplyVO();
            vo.setId(r.getId());
            vo.setConsultationId(r.getConsultationId());
            vo.setDoctorId(r.getDoctorId());
            vo.setDoctorName(doctorNameMap.getOrDefault(r.getDoctorId(), "未知医生"));
            vo.setContent(r.getReplyContent());
            vo.setReplyTime(r.getCreateTime());
            voList.add(vo);
        }

        return voList;
    }
}