package com.cjx.decision.dto.auth;

import lombok.Data;

/**
 * Response for PC DingTalk bridge handoff.
 */
@Data
public class DingTalkBridgeResponse {

    private String externalUrl;

    private Integer expiresIn;
}
