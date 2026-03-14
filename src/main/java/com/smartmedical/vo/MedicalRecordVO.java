package com.smartmedical.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalRecordVO {

    private Long id;
    private Long appointmentId;

    // profileId
    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;

    private String chiefComplaint;
    private String presentIllness;
    private String diagnosis;
    private String treatmentAdvice;

    /** 状态码（DRAFT / ARCHIVED） */
    private String status;

    /** 中文状态文本（草稿 / 已归档） */
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
}