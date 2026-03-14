package com.smartmedical.common;

import java.util.Map;

/**
 * 问诊状态常量类
 * 数据库存储状态码（WAITING / REPLIED）
 * 页面显示中文文本（待回复 / 已回复）
 */
public class ConsultationStatus {

    /** 待回复 */
    public static final String WAITING = "WAITING";

    /** 已回复 */
    public static final String REPLIED = "REPLIED";

    private static final Map<String, String> TEXT_MAP = Map.of(
            WAITING, "待回复",
            REPLIED, "已回复"
    );

    public static String textOf(String status) {
        return TEXT_MAP.getOrDefault(status, "未知状态");
    }
}