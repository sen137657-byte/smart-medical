package com.smartmedical.dto;

/**
 * 医生回复问诊请求 DTO
 */
public class ConsultationReplyReq {

    /**
     * 回复内容
     */
    private String replyContent;

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
}