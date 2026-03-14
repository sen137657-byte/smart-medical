package com.smartmedical.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 数据库唯一索引冲突异常
     * 例如：同一医生/患者同一时间重复预约
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        return Result.fail("预约冲突：该时间段已被占用，请重新选择时间");
    }

    /**
     * 参数类型不匹配（比如 id 需要 Long，但传了 abc）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String param = e.getName(); // 参数名，比如 id
        return Result.fail("参数 " + param + " 不合法");
    }

    /**
     * 处理所有运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * 处理所有未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.fail("系统异常，请联系管理员");
    }

}