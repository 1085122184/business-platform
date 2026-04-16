package com.cjx.common.core.result;

import com.cjx.common.core.enums.ResultCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * 统一返回结果封装
 * @author system
 */
@Data
public class Result<T>  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 返回消息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
