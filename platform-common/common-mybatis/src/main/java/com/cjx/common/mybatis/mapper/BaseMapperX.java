package com.cjx.common.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 扩展MyBatis Plus的BaseMapper
 * 提供批量操作方法
 *
 * @author company
 * @date 2024-01-20
 */
public interface BaseMapperX<T> extends BaseMapper<T> {
    /**
     * 批量插入（真正的批量插入）
     *
     * @param list 数据列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<T> list);

    /**
     * 批量更新（使用case when）
     *
     * @param list 数据列表
     * @return 影响行数
     */
    int updateBatch(@Param("list") List<T> list);
}
