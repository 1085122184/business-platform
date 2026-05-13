package com.cjx.decision.dto.system.role;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu tree node response.
 */
@Data
public class MenuTreeNodeResponse {

    private Long id;

    private String menuName;

    private Long parentId;

    private List<MenuTreeNodeResponse> children = new ArrayList<>();
}
