package com.cjx.common.mybatis.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.cjx.common.mybatis.method.InsertBatchMethod;
import com.cjx.common.mybatis.method.UpdateBatchMethod;

import java.util.List;

/**
 * 自定义SQL注入器
 * 扩展MyBatis Plus的批量操作方法
 *
 * @author company
 * @date 2024-01-20
 */
public class CustomSqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        // 获取MyBatis Plus默认方法
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);

        // 添加自定义批量插入方法
        methodList.add(new InsertBatchMethod());

        // 添加自定义批量更新方法
        methodList.add(new UpdateBatchMethod());

        return methodList;
    }
}
