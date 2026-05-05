package com.cjx.decision.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * One-time login ticket consume request.
 */
@Data
public class LoginTicketRequest {

    @NotBlank(message = "ticket must not be blank")
    private String ticket;
}
