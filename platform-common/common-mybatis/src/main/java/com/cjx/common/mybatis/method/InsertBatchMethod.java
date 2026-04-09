package com.cjx.common.mybatis.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 批量插入方法
 * 真正的批量插入，性能比循环insert高10倍以上
 *
 * @author company
 * @date 2024-01-20
 */
public class InsertBatchMethod extends AbstractMethod {
    private static final String METHOD_NAME = "insertBatch";

    public InsertBatchMethod() {
        super(METHOD_NAME);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>insert into %s %s values %s</script>";
        final String tableName = tableInfo.getTableName();
        final String columns = prepareColumns(tableInfo);
        final String values = prepareValues(tableInfo);

        final String sqlScript = String.format(sql, tableName, columns, values);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlScript, modelClass);

        return this.addInsertMappedStatement(mapperClass, modelClass, METHOD_NAME, sqlSource,new NoKeyGenerator(), null, null);
    }

    private String prepareColumns(TableInfo tableInfo) {
        StringBuilder columns = new StringBuilder("(");
        columns.append(tableInfo.getKeyColumn()).append(",");
        tableInfo.getFieldList().forEach(field -> {
            columns.append(field.getColumn()).append(",");
        });
        columns.delete(columns.length() - 1, columns.length());
        columns.append(")");
        return columns.toString();
    }

    private String prepareValues(TableInfo tableInfo) {
        StringBuilder values = new StringBuilder();
        values.append("<foreach collection=\"list\" item=\"item\" separator=\",\">");
        values.append("(");
        values.append("#{item.").append(tableInfo.getKeyProperty()).append("},");
        tableInfo.getFieldList().forEach(field -> {
            values.append("#{item.").append(field.getProperty()).append("},");
        });
        values.delete(values.length() - 1, values.length());
        values.append(")");
        values.append("</foreach>");
        return values.toString();
    }
}
