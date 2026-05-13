package com.cjx.common.mybatis.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 批量更新方法
 * 使用case when实现真正的批量更新
 *
 * @author company
 * @date 2024-01-20
 */
public class UpdateBatchMethod extends AbstractMethod {
    private static final String METHOD_NAME = "updateBatch";

    public UpdateBatchMethod() {
        super(METHOD_NAME);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>update %s %s where %s in %s</script>";
        final String tableName = tableInfo.getTableName();
        final String keyColumn = tableInfo.getKeyColumn();
        final String keyProperty = tableInfo.getKeyProperty();
        final String setClauses = prepareSetClauses(tableInfo, keyProperty);
        final String ids = prepareIds(keyProperty);

        final String sqlScript = String.format(sql, tableName, setClauses, keyColumn, ids);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlScript, modelClass);

        return this.addUpdateMappedStatement(mapperClass, modelClass, METHOD_NAME, sqlSource);
    }

    private String prepareSetClauses(TableInfo tableInfo, String keyProperty) {
        StringBuilder setClauses = new StringBuilder("<set>");
        tableInfo.getFieldList().forEach(field -> {
            setClauses.append(field.getColumn()).append(" = case ").append(tableInfo.getKeyColumn());
            setClauses.append("<foreach collection=\"list\" item=\"item\">");
            setClauses.append(" when #{item.").append(keyProperty).append("} then #{item.").append(field.getProperty()).append("}");
            setClauses.append("</foreach>");
            setClauses.append(" end,");
        });
        setClauses.delete(setClauses.length() - 1, setClauses.length());
        setClauses.append("</set>");
        return setClauses.toString();
    }

    private String prepareIds(String keyProperty) {
        return "<foreach collection=\"list\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item." + keyProperty + "}</foreach>";
    }
}
