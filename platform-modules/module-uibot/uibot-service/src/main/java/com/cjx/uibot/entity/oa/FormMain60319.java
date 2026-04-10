package com.cjx.uibot.entity.oa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "FORMMAIN_60319")
public class FormMain60319 {
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "STATE")
    private Integer state;

    @Size(max = 100)
    @Column(name = "FIELD0031", length = 100)
    private String field0031;

    @Size(max = 255)
    @Column(name = "FIELD0015")
    private String field0015;

    @Size(max = 100)
    @Column(name = "FIELD0019", length = 100)
    private String field0019;

    @Size(max = 100)
    @Column(name = "FIELD0037", length = 100)
    private String field0037;

    @Size(max = 100)
    @Column(name = "FIELD0045", length = 100)
    private String field0045;

    @Size(max = 100)
    @Column(name = "FIELD0052", length = 100)
    private String field0052;

    @Size(max = 100)
    @Column(name = "FIELD0053", length = 100)
    private String field0053;

    @Size(max = 100)
    @Column(name = "FIELD0055", length = 100)
    private String field0055;

    @Size(max = 100)
    @Column(name = "FIELD0057", length = 100)
    private String field0057;

    @Size(max = 100)
    @Column(name = "FIELD0058", length = 100)
    private String field0058;

}