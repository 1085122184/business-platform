package com.cjx.decision.service;

import com.cjx.decision.entity.frorcl.OrderDetail;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    List<OrderDetail> getCompanyDetails(LocalDate targetDate, String companyName);
}
