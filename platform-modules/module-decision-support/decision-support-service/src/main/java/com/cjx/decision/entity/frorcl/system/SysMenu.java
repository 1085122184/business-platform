package com.cjx.decision.entity.frorcl.system;

import com.cjx.common.jpa.entity.OracleAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Menu entity for SYS_MENU.
 */
@Getter
@Setter
@Entity
@Table(name = "SYS_MENU")
public class SysMenu extends OracleAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_menu_seq")
    @SequenceGenerator(name = "sys_menu_seq", sequenceName = "SEQ_SYS_MENU", allocationSize = 1)
    @Column(name = "MENU_ID")
    private Long id;

    @Column(name = "PARENT_ID", nullable = false)
    private Long parentId;

    @Column(name = "MENU_NAME", nullable = false, length = 100)
    private String menuName;

    @Column(name = "MENU_TYPE", nullable = false, length = 1)
    private String menuType;

    @Column(name = "PATH", length = 200)
    private String path;

    @Column(name = "COMPONENT", length = 200)
    private String component;

    @Column(name = "PERMS", length = 200)
    private String perms;

    @Column(name = "ICON", length = 100)
    private String icon;

    @Column(name = "ORDER_NUM", nullable = false)
    private Integer orderNum;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "REMARK", length = 500)
    private String remark;
}
