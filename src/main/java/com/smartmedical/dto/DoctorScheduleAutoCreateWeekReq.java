package com.smartmedical.dto;

import lombok.Data;

/**
 * 自动生成未来一周（上午+下午）排班
 */
@Data
public class DoctorScheduleAutoCreateWeekReq {

    /** 可选：不传则默认当前登录医生 */
    private Long doctorId;

    /** 生成天数：默认 7 */
    private Integer days = 7;

    /** 上午开始/结束：HH:mm */
    private String morningStart = "09:00";
    private String morningEnd   = "11:00";

    /** 下午开始/结束：HH:mm */
    private String afternoonStart = "14:00";
    private String afternoonEnd   = "17:00";

    /** 每个号源时长（分钟）：默认 30 */
    private Integer slotMinutes = 30;

    /**
     * 是否覆盖当天排班：
     * true：先删除当天未被BOOKED占用的排班，再生成
     * false：只补充不存在的 timeSlot
     */
    private Boolean overwrite = false;
}