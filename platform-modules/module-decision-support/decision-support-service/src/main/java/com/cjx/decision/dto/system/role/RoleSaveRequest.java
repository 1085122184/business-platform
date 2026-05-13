package com.cjx.decision.dto.system.role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for creating a role.
 */
@Data
public class RoleSaveRequest {

    @NotBlank(message = "roleName不能为空")
    @Size(min = 2, max = 30, message = "roleName长度必须在2到30之间")
    private String roleName;

    @NotBlank(message = "roleKey不能为空")
    @Size(min = 2, max = 100, message = "roleKey长度必须在2到100之间")
    private String roleKey;

    @NotNull(message = "roleSort不能为空")
    @Min(value = 0, message = "roleSort不能小于0")
    @Max(value = 9999, message = "roleSort不能大于9999")
    private Integer roleSort;

    @NotNull(message = "status不能为空")
    @Min(value = 0, message = "status只能为0或1")
    @Max(value = 1, message = "status只能为0或1")
    private Integer status;

    @Size(max = 500, message = "remark长度不能超过500")
    private String remark;
}
