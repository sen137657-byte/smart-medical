package com.smartmedical.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建预约请求 DTO
 */
@Data
public class AppointmentCreateReq {

    private Long doctorId;

    private LocalDateTime appointmentTime;
}