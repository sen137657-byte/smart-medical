package com.smartmedical.common;

import lombok.Data;

/**
 * 统一返回结果封装类
 */
@Data
public class Result<T> {

    /** 是否成功 */
    private boolean success;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 私有构造，外部用静态方法创建 */
    private Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /** 成功（无数据） */
    public static <T> Result<T> ok() {
        return new Result<>(true, "success", null);
    }

    /** 成功（有数据） */
    public static <T> Result<T> ok(T data) {
        return new Result<>(true, "success", data);
    }

    /** 失败 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(false, message, null);
    }
}
