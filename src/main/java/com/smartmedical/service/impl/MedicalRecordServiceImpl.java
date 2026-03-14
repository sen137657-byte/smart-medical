package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmedical.common.MedicalRecordStatus;
import com.smartmedical.common.RoleConst;
import com.smartmedical.dto.MedicalRecordCreateReq;
import com.smartmedical.dto.MedicalRecordUpdateReq;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.entity.MedicalRecord;
import com.smartmedical.entity.PatientProfile;
import com.smartmedical.mapper.*;
import com.smartmedical.service.MedicalRecordService;
import com.smartmedical.vo.MedicalRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PatientProfileMapper patientProfileMapper;

    @Override
    @Transactional
    public Long createByDoctor(Long doctorUserId, MedicalRecordCreateReq req) {
        if (req.getAppointmentId() == null) {
            throw new RuntimeException("appointmentId不能为空");
        }

        // 1) doctorUserId -> doctorProfile
        DoctorProfile doctorProfile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, doctorUserId)
                        .last("limit 1")
        );
        if (doctorProfile == null) throw new RuntimeException("未绑定医生档案");

        // 2) appointment
        var appt = appointmentMapper.selectById(req.getAppointmentId());
        if (appt == null) throw new RuntimeException("预约不存在");

        // 3) 归属校验：appointment.doctor_id == doctor_profile.id
        if (!doctorProfile.getId().equals(appt.getDoctorId())) {
            throw new RuntimeException("无权为该预约创建病历");
        }

        // 4) 一个预约只能一份病历
        MedicalRecord existed = medicalRecordMapper.selectOne(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getAppointmentId, req.getAppointmentId())
                        .last("limit 1")
        );
        if (existed != null) {
            throw new RuntimeException("该预约已创建病历");
        }

        // 5) 插入病历（doctor_id / patient_id 存 profileId）
        MedicalRecord r = new MedicalRecord();
        r.setAppointmentId(req.getAppointmentId());
        r.setDoctorId(appt.getDoctorId());      // profileId
        r.setPatientId(appt.getPatientId());    // profileId
        r.setChiefComplaint(req.getChiefComplaint());
        r.setPresentIllness(req.getPresentIllness());
        r.setDiagnosis(req.getDiagnosis());
        r.setTreatmentAdvice(req.getTreatmentAdvice());
        r.setStatus(MedicalRecordStatus.DRAFT);

        //  解决 create_time cannot be null
        LocalDateTime now = LocalDateTime.now();
        r.setRecordTime(now);
        r.setCreateTime(now);
        r.setUpdateTime(now);

        medicalRecordMapper.insert(r);
        return r.getId();
    }

    @Override
    @Transactional
    public void updateByDoctor(Long doctorUserId, Long id, MedicalRecordUpdateReq req) {
        DoctorProfile doctorProfile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, doctorUserId)
                        .last("limit 1")
        );
        if (doctorProfile == null) throw new RuntimeException("未绑定医生档案");

        MedicalRecord r = medicalRecordMapper.selectById(id);
        if (r == null) throw new RuntimeException("病历不存在");

        // doctor_id 存 profileId，所以这里用 profileId 校验
        if (!doctorProfile.getId().equals(r.getDoctorId())) {
            throw new RuntimeException("无权修改该病历");
        }
        if (!MedicalRecordStatus.DRAFT.equals(r.getStatus())) {
            throw new RuntimeException("病历已归档，不允许修改");
        }

        r.setChiefComplaint(req.getChiefComplaint());
        r.setPresentIllness(req.getPresentIllness());
        r.setDiagnosis(req.getDiagnosis());
        r.setTreatmentAdvice(req.getTreatmentAdvice());
        r.setUpdateTime(LocalDateTime.now());

        medicalRecordMapper.updateById(r);
    }

    @Override
    @Transactional
    public void archiveByDoctor(Long doctorUserId, Long id) {
        DoctorProfile doctorProfile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, doctorUserId)
                        .last("limit 1")
        );
        if (doctorProfile == null) throw new RuntimeException("未绑定医生档案");

        MedicalRecord r = medicalRecordMapper.selectById(id);
        if (r == null) throw new RuntimeException("病历不存在");

        if (!doctorProfile.getId().equals(r.getDoctorId())) {
            throw new RuntimeException("无权归档该病历");
        }
        if (!MedicalRecordStatus.DRAFT.equals(r.getStatus())) {
            return;
        }

        r.setStatus(MedicalRecordStatus.ARCHIVED);
        r.setUpdateTime(LocalDateTime.now());
        medicalRecordMapper.updateById(r);
    }

    @Override
    public List<MedicalRecordVO> listForDoctor(Long doctorUserId) {
        DoctorProfile doctorProfile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, doctorUserId)
                        .last("limit 1")
        );
        if (doctorProfile == null) throw new RuntimeException("未绑定医生档案");

        List<MedicalRecord> list = medicalRecordMapper.selectList(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getDoctorId, doctorProfile.getId()) // ✅用 profileId
                        .orderByDesc(MedicalRecord::getRecordTime)
        );
        return toVO(list);
    }

    @Override
    public List<MedicalRecordVO> listForPatient(Long patientUserId) {
        PatientProfile patientProfile = patientProfileMapper.selectOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, patientUserId)
                        .last("limit 1")
        );
        if (patientProfile == null) throw new RuntimeException("未绑定患者档案");

        List<MedicalRecord> list = medicalRecordMapper.selectList(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientProfile.getId()) // ✅用 profileId
                        .orderByDesc(MedicalRecord::getRecordTime)
        );
        return toVO(list);
    }

    @Override
    public MedicalRecordVO detail(Long loginUserId, String role, Long id) {
        MedicalRecord r = medicalRecordMapper.selectById(id);
        if (r == null) throw new RuntimeException("病历不存在");

        if (RoleConst.DOCTOR.equals(role)) {
            DoctorProfile doctorProfile = doctorProfileMapper.selectOne(
                    new LambdaQueryWrapper<DoctorProfile>()
                            .eq(DoctorProfile::getUserId, loginUserId)
                            .last("limit 1")
            );
            if (doctorProfile == null) throw new RuntimeException("未绑定医生档案");
            if (!doctorProfile.getId().equals(r.getDoctorId())) throw new RuntimeException("无权查看该病历");
        }

        if (RoleConst.PATIENT.equals(role)) {
            PatientProfile patientProfile = patientProfileMapper.selectOne(
                    new LambdaQueryWrapper<PatientProfile>()
                            .eq(PatientProfile::getUserId, loginUserId)
                            .last("limit 1")
            );
            if (patientProfile == null) throw new RuntimeException("未绑定患者档案");
            if (!patientProfile.getId().equals(r.getPatientId())) throw new RuntimeException("无权查看该病历");
        }

        return toVO(List.of(r)).get(0);
    }

    private List<MedicalRecordVO> toVO(List<MedicalRecord> list) {
        if (list == null || list.isEmpty()) return List.of();

        // doctor_id / patient_id 现在是 profileId，所以这里按 profileId 查 profile 表
        List<Long> doctorProfileIds = list.stream().map(MedicalRecord::getDoctorId).distinct().toList();
        List<Long> patientProfileIds = list.stream().map(MedicalRecord::getPatientId).distinct().toList();

        Map<Long, String> doctorNameMap = doctorProfileMapper.selectList(
                new LambdaQueryWrapper<DoctorProfile>().in(DoctorProfile::getId, doctorProfileIds)
        ).stream().collect(Collectors.toMap(DoctorProfile::getId, DoctorProfile::getName, (a, b) -> a));

        Map<Long, String> patientNameMap = patientProfileMapper.selectList(
                new LambdaQueryWrapper<PatientProfile>().in(PatientProfile::getId, patientProfileIds)
        ).stream().collect(Collectors.toMap(PatientProfile::getId, PatientProfile::getName, (a, b) -> a));

        return list.stream().map(r -> {
            MedicalRecordVO vo = new MedicalRecordVO();
            vo.setId(r.getId());
            vo.setAppointmentId(r.getAppointmentId());

            vo.setDoctorId(r.getDoctorId());
            vo.setDoctorName(doctorNameMap.getOrDefault(r.getDoctorId(), "医生"));

            vo.setPatientId(r.getPatientId());
            vo.setPatientName(patientNameMap.getOrDefault(r.getPatientId(), "患者"));

            vo.setChiefComplaint(r.getChiefComplaint());
            vo.setPresentIllness(r.getPresentIllness());
            vo.setDiagnosis(r.getDiagnosis());
            vo.setTreatmentAdvice(r.getTreatmentAdvice());

            vo.setStatus(r.getStatus());
            vo.setStatusText(MedicalRecordStatus.textOf(r.getStatus()));
            vo.setRecordTime(r.getRecordTime());
            return vo;
        }).toList();
    }
}