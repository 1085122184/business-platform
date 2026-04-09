package com.cjx.uibot.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TUserDto implements Serializable {
    String id;
    @NotNull
    @Size(max = 50)
    String username;
    @NotNull
    @Size(max = 255)
    String passwordHash;
    @NotNull
    @Size(max = 20)
    String phoneNumber;
    @Size(max = 100)
    String email;
    @Size(max = 512)
    String avatarUrl;
    @NotNull
    Boolean isActive;
}