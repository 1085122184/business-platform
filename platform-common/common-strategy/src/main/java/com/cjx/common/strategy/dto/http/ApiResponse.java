package dongyue.common.strategy.dto.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * 统一响应结果
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="响应码（200:成功，其他:失败）")
    private String code;

    @Schema(description ="响应消息")
    private String message;

    @Schema(description ="响应数据")
    private T data;

    @Schema(description ="请求追踪ID")
    private String traceId;

    @Schema(description ="时间戳")
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("200")
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> failure(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 系统错误
     */
    public static <T> ApiResponse<T> error(String message) {
        return failure("500", message);
    }
}
