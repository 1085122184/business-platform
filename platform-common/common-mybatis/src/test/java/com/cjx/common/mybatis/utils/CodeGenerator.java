package com.cjx.common.mybatis.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;
import java.sql.Types;
import java.util.Collections;

/**
 * MyBatis-Plus代码生成器
 * 用于自动生成Entity、Mapper、Service等代码
 * 注意: 此类仅用于开发阶段，不应打包到生产环境
 *
 * @author 崔吉旭
 */
public class CodeGenerator {

    /**
     * 生成代码
     * @param url 数据库连接URL
     * @param userName 数据库用户名
     * @param passWord 数据库密码
     * @param packetName 父包名
     * @param tableName 表名
     * @param modelPath 模块路径
     */
    public static void generatedCode(String url, String userName, String passWord, String packetName, String tableName, String modelPath) {
        FastAutoGenerator.create(url, userName, passWord)
                .globalConfig(builder -> {
                    builder.author("崔吉旭")
                            .outputDir(Paths.get(System.getProperty("user.dir")) + "/" + modelPath + "/src/main/java");
                })
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder ->
                        builder.parent(packetName)
                                .entity("entity.dos")
                                .mapper("mapper")
                                .service("service")
                                .serviceImpl("service.impl")
                                .xml("mappers")
                                .pathInfo(Collections.singletonMap(OutputFile.xml, Paths.get(System.getProperty("user.dir")) + "/" + modelPath + "/src/main/resources/mappers"))
                )
                .strategyConfig(builder ->
                        builder.addInclude(tableName)
                                .addTablePrefix("t_", "c_")
                                .entityBuilder().enableLombok().enableFileOverride().naming(NamingStrategy.underline_to_camel).enableTableFieldAnnotation()
                                .controllerBuilder().enableHyphenStyle().enableFileOverride().enableRestStyle()
                                .mapperBuilder().enableMapperAnnotation().enableFileOverride()
                                .serviceBuilder().enableFileOverride()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
