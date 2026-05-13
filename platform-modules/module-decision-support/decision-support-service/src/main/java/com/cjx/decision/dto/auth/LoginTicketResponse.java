package com.cjx.decision.dto.auth;

import lombok.Data;

/**
 * One-time login ticket response.
 */
@Data
public class LoginTicketResponse {

    private String ticket;

    private Integer expiresIn;
}
