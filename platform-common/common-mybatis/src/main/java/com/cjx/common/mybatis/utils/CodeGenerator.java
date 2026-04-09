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
 * @author 崔吉旭
 */
public class CodeGenerator {

    public static void generatedCode(String url, String userName, String passWord, String packetName,String tableName,String modelPath) {
        FastAutoGenerator.create(url, userName, passWord)
                .globalConfig(builder -> {
                    builder.author("崔吉旭") // 设置作者
                            .outputDir(Paths.get(System.getProperty("user.dir"))+"/"+modelPath+"/src/main/java"); // 指定输出目录
                })
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                // 自定义类型转换
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder ->
                        builder.parent(packetName) // 设置父包名
                                .entity("entity.dos") // 设置实体类包名
                                .mapper("mapper") // 设置 Mapper 接口包名
                                .service("service") // 设置 Service 接口包名
                                .serviceImpl("service.impl") // 设置 Service 实现类包名
                                .xml("mappers") // 设置 Mapper XML 文件包名
                                .pathInfo(Collections.singletonMap(OutputFile.xml, Paths.get(System.getProperty("user.dir"))+"/"+modelPath+"/src/main/resources/mappers")) // 设置mapperXml生成路径
                )
                .strategyConfig(builder ->
                        builder.addInclude(tableName) // 设置需要生成的表名
                                .addTablePrefix("t_", "c_") // 设置过滤表前缀
                                .entityBuilder().enableLombok().enableFileOverride().naming(NamingStrategy.underline_to_camel).enableTableFieldAnnotation()
                                .controllerBuilder().enableHyphenStyle().enableFileOverride().enableRestStyle()
                                .mapperBuilder().enableMapperAnnotation().enableFileOverride()
                                .serviceBuilder().enableFileOverride()
                )
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}
