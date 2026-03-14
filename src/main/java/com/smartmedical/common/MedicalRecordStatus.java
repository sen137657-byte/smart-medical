package com.smartmedical.common;

import java.util.Map;

/**
 * 病历状态常量类
 * 数据库存储状态码（DRAFT / ARCHIVED）
 * 页面显示使用中文文本（草稿 / 已归档）
 */
public class MedicalRecordStatus {

    /** 草稿（医生可编辑） */
    public static final String DRAFT = "DRAFT";

    /** 已归档（患者可见，不可修改） */
    public static final String ARCHIVED = "ARCHIVED";

    /**
     * 状态码 -> 中文文本映射
     */
    private static final Map<String, String> TEXT_MAP = Map.of(
            DRAFT, "草稿",
            ARCHIVED, "已归档"
    );

    /**
     * 根据状态码获取中文文本
     */
    public static String textOf(String status) {
        return TEXT_MAP.getOrDefault(status, "未知状态");
    }
}