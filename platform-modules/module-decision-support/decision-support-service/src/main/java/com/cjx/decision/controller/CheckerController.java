package com.cjx.decision.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author cuijixu
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Validated
@Tag(name = "获取卡片指标数据", description = "获取卡片指标数据")
@CrossOrigin(origins = "*")
public class CheckerController {

}
