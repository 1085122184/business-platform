package dongyue.common.strategy.dto.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 策略执行请求
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description  = "策略执行请求")
public class StrategyExecuteRequest  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(name = "策略Key（格式：业务域:操作，如user:create）", required = true, example = "user:create")
    @NotBlank(message = "策略Key不能为空")
    private String strategyKey;

    @Schema (name = "业务参数", required = true)
    @NotNull(message = "业务参数不能为空")
    private Map<String, Object> params;

    @Schema (name = "扩展参数")
    private Map<String, Object> extras;
}
