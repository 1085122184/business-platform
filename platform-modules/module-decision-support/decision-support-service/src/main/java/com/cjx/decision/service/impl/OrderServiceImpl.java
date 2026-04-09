package com.cjx.decision.service.impl;

import com.cjx.decision.entity.frorcl.OrderDetail;
import com.cjx.decision.repository.frorcl.OrderRepository;
import com.cjx.decision.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public List<OrderDetail> getCompanyDetails(LocalDate targetDate, String companyName) {
        String thisMonth = targetDate.minusDays(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String lastMonth = targetDate.minusDays(1).with(TemporalAdjusters.lastDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return orderRepository.findOrderDetail(thisMonth,lastMonth,companyName);
    }
}
