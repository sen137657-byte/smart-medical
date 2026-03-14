package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 在线问诊回复表
 */
@TableName("consultation_reply")
public class ConsultationReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId; // 问诊ID
    private Long doctorId;       // 医生ID

    private String replyContent; // 回复内容

    private LocalDateTime createTime;

    // getter setter

    public Long getId() {
        return id;
    }

    public Long getConsultationId() {
        return consultationId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setConsultationId(Long consultationId) {
        this.consultationId = consultationId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}