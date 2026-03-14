package com.smartmedical.dto;

import lombok.Data;

@Data
public class DoctorScheduleRangeCreateReq {

    private Long doctorId;        // 可选：不传则默认当前登录医生
    private String startDate;     // yyyy-MM-dd，例如 2026-02-28
    private Integer days;         // 生成多少天，默认 7

    private String startTime;     // HH:mm，例如 09:00
    private String endTime;       // HH:mm，例如 11:00
    private Integer slotMinutes;  // 例如 30

    private Boolean overwrite;    // 覆盖：true/false
    private Boolean skipWeekend;  // 跳过周末：true/false
}