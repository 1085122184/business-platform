package com.cjx.decision.service.impl;

import com.cjx.decision.dto.dashboard.OrderDetailDTO;
import com.cjx.decision.projection.frorcl.OrderDetail;
import com.cjx.decision.repository.frorcl.OrderRepository;
import com.cjx.decision.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<OrderDetailDTO> getOrderWithDetails(LocalDate targetDate, String companyName) {
        String thisMonth = targetDate.minusDays(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String lastMonth = targetDate.minusDays(1).with(TemporalAdjusters.lastDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Map<String, Object>> mapList = orderRepository.findOrderWithDetails(thisMonth,lastMonth,companyName);
        return convertToDTO(mapList);
    }



    private List<OrderDetailDTO> convertToDTO(List<Map<String, Object>> queryResult) {
        // 按订单分组
        Map<String, List<Map<String, Object>>> groupedByOrder =
                queryResult.stream()
                        .collect(Collectors.groupingBy(map -> map.get("orderNo").toString()));

        List<OrderDetailDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByOrder.entrySet()) {
            List<Map<String, Object>> orderItems = entry.getValue();

            // 创建主订单DTO对象
            OrderDetailDTO dto = new OrderDetailDTO();
            Map<String, Object> firstItem = orderItems.get(0);

            dto.setOrderDate(firstItem.get("orderDate") != null ? firstItem.get("orderDate").toString() : null);
            dto.setOrderNo(firstItem.get("orderNo") != null ? firstItem.get("orderNo").toString() : null);
            dto.setMaterialGroup(firstItem.get("materialGroup") != null ? firstItem.get("materialGroup").toString() : null);
            dto.setMaterialDesc(firstItem.get("materialDesc") != null ? firstItem.get("materialDesc").toString() : null);
            dto.setDeliveryStatus(firstItem.get("deliveryStatus") != null ? firstItem.get("deliveryStatus").toString() : null);
            dto.setSalesOrg(firstItem.get("salesOrg") != null ? firstItem.get("salesOrg").toString() : null);
            dto.setOffice(firstItem.get("office") != null ? firstItem.get("office").toString() : null);
            dto.setSalesPerson(firstItem.get("salesPerson") != null ? firstItem.get("salesPerson").toString() : null);
            dto.setCustomer(firstItem.get("customer") != null ? firstItem.get("customer").toString() : null);
            dto.setChannel(firstItem.get("channel") != null ? firstItem.get("channel").toString() : null);

            dto.setOrderAmount(firstItem.get("orderAmount") != null ?
                    new BigDecimal(firstItem.get("orderAmount").toString()) : null);
            dto.setOrderNum(firstItem.get("orderNum") != null ?
                    new BigDecimal(firstItem.get("orderNum").toString()) : null);
            // 创建明细项列表
            List<OrderDetailDTO.OrderDetailItem> detailItems = orderItems.stream()
                    .map(item -> {
                        OrderDetailDTO.OrderDetailItem detailItem = new OrderDetailDTO.OrderDetailItem();

                        // 设置明细项目的数据
                        detailItem.setDetailDate(item.get("detailDate") != null ?
                                item.get("detailDate").toString() : null);;
                        detailItem.setAmount(item.get("amount") != null ?
                                new BigDecimal(item.get("amount").toString()) : null);
                        detailItem.setVolume(item.get("volume") != null ?
                                new BigDecimal(item.get("volume").toString()) : null);
                        detailItem.setPrice(item.get("price") != null ?
                                new BigDecimal(item.get("price").toString()) : null);
                        detailItem.setOffice(item.get("office") != null ?
                                item.get("office").toString() : null);
                        detailItem.setCustomer(item.get("customer") != null ?
                                item.get("customer").toString() : null);
                        detailItem.setMaterialDesc(item.get("materialDesc") != null ?
                                item.get("materialDesc").toString() : null);

                        return detailItem;
                    })
                    .collect(Collectors.toList());

            dto.setDetails(detailItems);
            result.add(dto);
        }

        return result;
    }
}
