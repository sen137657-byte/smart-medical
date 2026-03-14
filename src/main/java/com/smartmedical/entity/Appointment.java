package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 预约表 appointment
 */
@TableName("appointment")
public class Appointment {

    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_FINISHED = "FINISHED";


    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;   // patient_profile.id
    private Long doctorId;    // doctor_profile.id

    private LocalDateTime appointmentTime;
    private String status;    // BOOKED / CANCELLED / FINISHED
    private LocalDateTime createTime;

    // ===== getter & setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}