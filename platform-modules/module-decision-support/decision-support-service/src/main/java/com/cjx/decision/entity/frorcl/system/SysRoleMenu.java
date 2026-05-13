package com.cjx.decision.entity.frorcl.system;

import com.cjx.common.jpa.entity.OracleCreateTimeEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Role-menu relation entity for SYS_ROLE_MENU.
 */
@Getter
@Setter
@Entity
@Table(name = "SYS_ROLE_MENU")
public class SysRoleMenu extends OracleCreateTimeEntity {

    @EmbeddedId
    private SysRoleMenuId id;
}
