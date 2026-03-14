package com.smartmedical.dto;

import lombok.Data;

@Data
public class MedicalRecordCreateReq {
    private Long appointmentId; // 必填（绑定预约）
    private String chiefComplaint;
    private String presentIllness;
    private String diagnosis;
    private String treatmentAdvice;
}