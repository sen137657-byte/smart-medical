package com.smartmedical.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生端问诊列表展示 VO（带患者姓名）
 */
@Data
public class ConsultationDoctorVO {

    private Long id;
    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String content;

    /** 状态码（WAITING / REPLIED） */
    private String status;

    /** 中文状态文本（待回复 / 已回复） */
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}