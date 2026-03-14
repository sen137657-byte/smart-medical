package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.vo.ConsultationReplyVO;
import com.smartmedical.entity.ConsultationReply;

import java.util.List;

public interface ConsultationReplyService extends IService<ConsultationReply> {

    /** 查询某个问诊的回复列表（返回VO给前端） */
    List<ConsultationReplyVO> listReplyVO(Long consultationId);
}