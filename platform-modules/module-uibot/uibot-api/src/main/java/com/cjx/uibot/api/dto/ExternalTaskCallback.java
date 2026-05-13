package dongyue.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 外部任务回调
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTaskCallback implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    @NotBlank(message = "队列类型不能为空")
    private String queueType;

    @NotNull(message = "成功标识不能为空")
    private Boolean success;

    private String message;
    private Object result;
}