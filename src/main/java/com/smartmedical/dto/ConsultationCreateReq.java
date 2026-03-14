package com.smartmedical.dto;

import java.time.LocalDateTime;

/**
 * 患者发起问诊请求 DTO
 */
public class ConsultationCreateReq {

    /**
     * 选择的医生ID（doctor_profile.id）
     */
    private Long doctorId;

    /**
     * 病情描述（文字）
     */
    private String content;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}