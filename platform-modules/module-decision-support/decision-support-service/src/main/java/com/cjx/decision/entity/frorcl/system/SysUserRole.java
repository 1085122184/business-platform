package com.cjx.decision.entity.frorcl.system;

import com.cjx.common.jpa.entity.OracleCreateTimeEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * User-role relation entity for SYS_USER_ROLE.
 */
@Getter
@Setter
@Entity
@Table(name = "SYS_USER_ROLE")
public class SysUserRole extends OracleCreateTimeEntity {

    @EmbeddedId
    private SysUserRoleId id;
}
