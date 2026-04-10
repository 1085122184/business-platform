package com.cjx.uibot.entity.uibot;

import com.cjx.common.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @author Administrator
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_users", schema = "uibot")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "id", nullable = false, length = 100))
})
public class TUser extends BaseEntity {
    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 50)
    @Column(name = "ding_user_id", nullable = false, length = 50)
    private String dingUserId;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 20)
    @NotNull
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 512)
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

}