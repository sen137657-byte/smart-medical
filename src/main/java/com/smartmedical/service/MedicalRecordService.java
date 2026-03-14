package com.smartmedical.service;

import com.smartmedical.dto.MedicalRecordCreateReq;
import com.smartmedical.dto.MedicalRecordUpdateReq;
import com.smartmedical.vo.MedicalRecordVO;

import java.util.List;

public interface MedicalRecordService {
    Long createByDoctor(Long doctorUserId, MedicalRecordCreateReq req);
    void updateByDoctor(Long doctorUserId, Long id, MedicalRecordUpdateReq req);
    void archiveByDoctor(Long doctorUserId, Long id);

    List<MedicalRecordVO> listForDoctor(Long doctorUserId);
    List<MedicalRecordVO> listForPatient(Long patientUserId);

    MedicalRecordVO detail(Long loginUserId, String role, Long id);
}