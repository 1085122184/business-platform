package com.cjx.decision.entity.frorcl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Immutable;

/**
 * Mapping for DB view
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Immutable
@Table(name = "V_LVLENG_RIXIAOSHOU")
public class VLvlengRixiaoshou {
    @Size(max = 2000)
    @Column(name = "渠道", length = 2000)
    private String 渠道;

    @Size(max = 4000)
    @Column(name = "物料", length = 4000)
    private String 物料;

    @Size(max = 2000)
    @Column(name = "物料描述", length = 2000)
    private String 物料描述;

    @Size(max = 2000)
    @Column(name = "销量", length = 2000)
    private String 销量;

    @Size(max = 2000)
    @Column(name = "过账日期", length = 2000)
    private String 过账日期;

    @Size(max = 2000)
    @Column(name = "工厂", length = 2000)
    private String 工厂;

    @Size(max = 12)
    @Column(name = "工厂描述", length = 12)
    private String 工厂描述;

    @Size(max = 2000)
    @Column(name = "物料组", length = 2000)
    private String 物料组;

    @Size(max = 2000)
    @Column(name = "物料组描述", length = 2000)
    private String 物料组描述;

    @Size(max = 40)
    @Column(name = "金额", length = 40)
    private String 金额;

    @Size(max = 2000)
    @Column(name = "单价", length = 2000)
    private String 单价;

    @Size(max = 2000)
    @Column(name = "办事处描述", length = 2000)
    private String 办事处描述;

    @Size(max = 4000)
    @Column(name = "客户编码", length = 4000)
    private String 客户编码;

    @Size(max = 2000)
    @Id
    @Column(name = "客户名称", length = 2000)
    private String 客户名称;

}