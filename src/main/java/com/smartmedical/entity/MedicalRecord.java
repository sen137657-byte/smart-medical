package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;
    private Long patientId;
    private Long doctorId;

    private String chiefComplaint;
    private String presentIllness;
    private String diagnosis;
    private String treatmentAdvice;

    private String status;

    private LocalDateTime recordTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}