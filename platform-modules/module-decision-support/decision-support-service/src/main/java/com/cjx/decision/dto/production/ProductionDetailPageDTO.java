package com.cjx.decision.dto.production;

import lombok.Data;

import java.util.List;

@Data
public class ProductionDetailPageDTO<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
}
