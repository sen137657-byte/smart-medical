package com.smartmedical.dto;

/**
 * 医生批量生成排班请求
 */
public class DoctorScheduleBatchCreateReq {

    private Long doctorId;       // 可选：不传则默认当前登录医生
    private String workDate;     // yyyy-MM-dd
    private String startTime;    // HH:mm 例如 09:00
    private String endTime;      // HH:mm 例如 12:00（不包含 endTime）
    private Integer slotMinutes; // 例如 30
    private Boolean overwrite;   // 是否覆盖当天原有排班（可选，默认 false）

    public Long getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getWorkDate() {
        return workDate;
    }
    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotMinutes() {
        return slotMinutes;
    }
    public void setSlotMinutes(Integer slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }
}