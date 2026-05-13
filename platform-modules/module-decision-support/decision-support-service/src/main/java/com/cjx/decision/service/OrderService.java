package com.cjx.decision.service;

import com.cjx.decision.dto.dashboard.OrderDetailDTO;
import com.cjx.decision.projection.frorcl.OrderDetail;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    List<OrderDetail> getCompanyDetails(LocalDate targetDate, String companyName);

    List<OrderDetailDTO> getOrderWithDetails(LocalDate targetDate, String companyName);
}
