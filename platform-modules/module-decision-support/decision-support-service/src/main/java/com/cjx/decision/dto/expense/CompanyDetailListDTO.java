package com.cjx.decision.dto.expense;

import lombok.Data;

import java.util.List;

/**
 * 公司三费明细分页数据DTO
 *
 * @author system
 * @version 1.0.0
 */
@Data
public class CompanyDetailListDTO {
    
    /**
     * 数据列表
     */
    private List<CompanyComparisonDTO> list;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页条数
     */
    private Integer pageSize;
}
