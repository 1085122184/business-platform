package com.cjx.common.core.utils;

import com.cjx.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean属性复制转换工具类
 * <p>
 * 用于不同JavaBean之间的属性复制和类型转换，支持单对象、集合、Map等多种转换场景
 * 基于Spring BeanUtils实现，性能优于反射，适合企业级应用
 * </p>
 *
 * @author your-name
 * @since 1.0.0
 */
@Slf4j
public final class BeanConverterUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private BeanConverterUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 复制对象属性
     * <p>仅复制字段名和类型完全匹配的属性，不复制null值</p>
     *
     * @param source 源对象，不能为null
     * @param target 目标对象，不能为null
     * @throws IllegalArgumentException 当source或target为null时抛出
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null) {
            throw new IllegalArgumentException("Source object must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object must not be null");
        }

        try {
            BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            log.error("Failed to copy properties from {} to {}",
                    source.getClass().getSimpleName(),
                    target.getClass().getSimpleName(), e);
            throw new BusinessException("Property copy failed", e);
        }
    }

    /**
     * 复制对象属性（忽略null值）
     * <p>只复制源对象中值不为null的属性到目标对象</p>
     *
     * @param source 源对象，不能为null
     * @param target 目标对象，不能为null
     * @throws IllegalArgumentException 当source或target为null时抛出
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        if (source == null) {
            throw new IllegalArgumentException("Source object must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object must not be null");
        }

        try {
            String[] nullPropertyNames = getNullPropertyNames(source);
            BeanUtils.copyProperties(source, target, nullPropertyNames);
        } catch (Exception e) {
            log.error("Failed to copy non-null properties from {} to {}",
                    source.getClass().getSimpleName(),
                    target.getClass().getSimpleName(), e);
            throw new BusinessException("Property copy (ignore null) failed", e);
        }
    }

    /**
     * 对象类型转换
     * <p>创建目标类型的新实例，并将源对象的属性复制到新实例</p>
     *
     * @param source      源对象
     * @param targetClass 目标类的Class对象，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换后的目标对象，如果source为null则返回null
     * @throws IllegalArgumentException 当targetClass为null时抛出
     * @throws BusinessException        当对象创建或属性复制失败时抛出
     */
    public static <S, T> T convert(S source, Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Failed to convert {} to {}",
                    source.getClass().getSimpleName(),
                    targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("Object conversion failed from %s to %s",
                            source.getClass().getSimpleName(),
                            targetClass.getSimpleName()), e);
        }
    }

    /**
     * 对象类型转换（忽略null值）
     * <p>创建目标类型的新实例，仅复制源对象中非null的属性</p>
     *
     * @param source      源对象
     * @param targetClass 目标类的Class对象，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换后的目标对象，如果source为null则返回null
     * @throws IllegalArgumentException 当targetClass为null时抛出
     * @throws BusinessException        当对象创建或属性复制失败时抛出
     */
    public static <S, T> T convertIgnoreNull(S source, Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            String[] nullPropertyNames = getNullPropertyNames(source);
            BeanUtils.copyProperties(source, target, nullPropertyNames);
            return target;
        } catch (Exception e) {
            log.error("Failed to convert {} to {} (ignore null)",
                    source.getClass().getSimpleName(),
                    targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("Object conversion (ignore null) failed from %s to %s",
                            source.getClass().getSimpleName(),
                            targetClass.getSimpleName()), e);
        }
    }

    /**
     * 批量转换集合
     * <p>将源对象列表中的每个对象转换为目标类型</p>
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标类的Class对象，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换后的目标对象列表，如果sourceList为null或empty则返回空列表
     * @throws IllegalArgumentException 当targetClass为null时抛出
     * @throws BusinessException        当批量转换失败时抛出
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        try {
            return sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(source -> convert(source, targetClass))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to convert list to {}", targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("List conversion failed to %s", targetClass.getSimpleName()), e);
        }
    }

    /**
     * 批量转换集合（忽略null值）
     * <p>将源对象列表中的每个对象转换为目标类型，仅复制非null属性</p>
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标类的Class对象，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换后的目标对象列表，如果sourceList为null或empty则返回空列表
     * @throws IllegalArgumentException 当targetClass为null时抛出
     * @throws BusinessException        当批量转换失败时抛出
     */
    public static <S, T> List<T> convertListIgnoreNull(List<S> sourceList, Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        try {
            return sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(source -> convertIgnoreNull(source, targetClass))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to convert list to {} (ignore null)", targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("List conversion (ignore null) failed to %s",
                            targetClass.getSimpleName()), e);
        }
    }

    /**
     * 使用自定义转换函数进行对象转换
     * <p>提供更灵活的转换方式，支持自定义转换逻辑</p>
     *
     * @param source    源对象
     * @param converter 自定义转换函数，不能为null
     * @param <S>       源对象类型
     * @param <T>       目标对象类型
     * @return 转换后的目标对象，如果source为null则返回null
     * @throws IllegalArgumentException 当converter为null时抛出
     * @throws BusinessException        当转换失败时抛出
     */
    public static <S, T> T convert(S source, Function<? super S, ? extends T> converter) {
        if (converter == null) {
            throw new IllegalArgumentException("Converter function must not be null");
        }
        if (source == null) {
            return null;
        }

        try {
            return converter.apply(source);
        } catch (Exception e) {
            log.error("Failed to convert object using custom converter from {}",
                    source.getClass().getSimpleName(), e);
            throw new BusinessException(
                    String.format("Custom conversion failed from %s",
                            source.getClass().getSimpleName()), e);
        }
    }

    /**
     * 使用自定义转换函数批量转换集合
     * <p>适用于复杂的转换场景，如需要额外处理、计算或依赖注入的情况</p>
     * <p>支持方法引用：convertList(list, source -> convert(source, TargetClass.class))</p>
     * <p>或简写为：convertList(list, toConverter(TargetClass.class))</p>
     *
     * @param sourceList 源对象列表
     * @param converter  自定义转换函数，不能为null
     * @param <S>        源对象类型
     * @param <T>        目标对象类型
     * @return 转换后的目标对象列表，如果sourceList为null或empty则返回空列表
     * @throws IllegalArgumentException 当converter为null时抛出
     * @throws BusinessException        当批量转换失败时抛出
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Function<? super S, ? extends T> converter) {
        if (converter == null) {
            throw new IllegalArgumentException("Converter function must not be null");
        }
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        try {
            return sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(converter)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to convert list using custom converter", e);
            throw new BusinessException("Custom list conversion failed", e);
        }
    }

    /**
     * 创建一个转换函数，用于方法引用
     * <p>简化批量转换时的Lambda表达式写法</p>
     * <pre>
     * // 传统写法
     * List&lt;UserVO&gt; list = sourceList.stream()
     *     .map(source -> BeanConverterUtil.convert(source, UserVO.class))
     *     .collect(Collectors.toList());
     *
     * // 使用toConverter简化
     * List&lt;UserVO&gt; list = sourceList.stream()
     *     .map(BeanConverterUtil.toConverter(UserVO.class))
     *     .collect(Collectors.toList());
     *
     * // 或直接使用convertList
     * List&lt;UserVO&gt; list = BeanConverterUtil.convertList(sourceList,
     *     BeanConverterUtil.toConverter(UserVO.class));
     * </pre>
     *
     * @param targetClass 目标类的Class对象，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换函数
     * @throws IllegalArgumentException 当targetClass为null时抛出
     */
    public static <S, T> Function<S, T> toConverter(Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        return source -> convert(source, targetClass);
    }

    /**
     * 创建一个转换函数（忽略null值），用于方法引用
     * <p>简化批量转换时的Lambda表达式写法</p>
     *
     * @param targetClass 目标类的Class对象，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换函数
     * @throws IllegalArgumentException 当targetClass为null时抛出
     */
    public static <S, T> Function<S, T> toConverterIgnoreNull(Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        return source -> convertIgnoreNull(source, targetClass);
    }

    /**
     * 创建一个增强转换函数，用于方法引用
     * <p>先进行标准属性复制，再应用增强函数</p>
     * <pre>
     * // 使用示例
     * List&lt;UserVO&gt; list = sourceList.stream()
     *     .map(BeanConverterUtil.toConverterWithEnhancer(UserVO.class, vo -> {
     *         vo.setCreateTime(new Date());
     *         return vo;
     *     }))
     *     .collect(Collectors.toList());
     * </pre>
     *
     * @param targetClass 目标类的Class对象，不能为null
     * @param enhancer    增强函数，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换函数
     * @throws IllegalArgumentException 当targetClass或enhancer为null时抛出
     */
    public static <S, T> Function<S, T> toConverterWithEnhancer(Class<T> targetClass,
                                                                Function<? super T, ? extends T> enhancer) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (enhancer == null) {
            throw new IllegalArgumentException("Enhancer function must not be null");
        }
        return source -> convertWithEnhancer(source, targetClass, enhancer);
    }

    /**
     * 使用Class和自定义转换函数进行对象转换
     * <p>先执行标准属性复制，再应用自定义转换逻辑进行增强处理</p>
     *
     * @param source      源对象
     * @param targetClass 目标类的Class对象，不能为null
     * @param enhancer    增强函数，用于在基础转换后进行额外处理，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换并增强后的目标对象，如果source为null则返回null
     * @throws IllegalArgumentException 当targetClass或enhancer为null时抛出
     * @throws BusinessException        当转换失败时抛出
     */
    public static <S, T> T convertWithEnhancer(S source, Class<T> targetClass,
                                               Function<? super T, ? extends T> enhancer) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (enhancer == null) {
            throw new IllegalArgumentException("Enhancer function must not be null");
        }
        if (source == null) {
            return null;
        }

        try {
            T target = convert(source, targetClass);
            return enhancer.apply(target);
        } catch (Exception e) {
            log.error("Failed to convert and enhance {} to {}",
                    source.getClass().getSimpleName(),
                    targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("Conversion with enhancer failed from %s to %s",
                            source.getClass().getSimpleName(),
                            targetClass.getSimpleName()), e);
        }
    }

    /**
     * 使用Class和增强函数批量转换集合
     * <p>对列表中每个对象先执行标准属性复制，再应用增强函数</p>
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标类的Class对象，不能为null
     * @param enhancer    增强函数，不能为null
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 转换并增强后的目标对象列表，如果sourceList为null或empty则返回空列表
     * @throws IllegalArgumentException 当targetClass或enhancer为null时抛出
     * @throws BusinessException        当批量转换失败时抛出
     */
    public static <S, T> List<T> convertListWithEnhancer(List<S> sourceList, Class<T> targetClass,
                                                         Function<? super T, ? extends T> enhancer) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (enhancer == null) {
            throw new IllegalArgumentException("Enhancer function must not be null");
        }
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        try {
            return sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(source -> convertWithEnhancer(source, targetClass, enhancer))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to convert list with enhancer to {}", targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("List conversion with enhancer failed to %s",
                            targetClass.getSimpleName()), e);
        }
    }

    /**
     * 获取对象中值为null的属性名数组
     *
     * @param source 源对象，不能为null
     * @return 值为null的属性名数组
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();

        Set<String> nullPropertyNames = new HashSet<>(propertyDescriptors.length);
        for (PropertyDescriptor pd : propertyDescriptors) {
            Object propertyValue = beanWrapper.getPropertyValue(pd.getName());
            if (propertyValue == null) {
                nullPropertyNames.add(pd.getName());
            }
        }
        return nullPropertyNames.toArray(new String[0]);
    }

    /**
     * Map转换为JavaBean对象
     * <p>将Map中的键值对映射到目标对象的属性</p>
     *
     * @param sourceMap   源Map数据
     * @param targetClass 目标类的Class对象，不能为null
     * @param <T>         目标对象类型
     * @return 转换后的目标对象，如果sourceMap为null或empty则返回null
     * @throws IllegalArgumentException 当targetClass为null时抛出
     * @throws BusinessException        当Map转换失败时抛出
     */
    public static <T> T mapToBean(Map<String, Object> sourceMap, Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (CollectionUtils.isEmpty(sourceMap)) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanWrapper beanWrapper = new BeanWrapperImpl(target);

            sourceMap.forEach((key, value) -> {
                if (beanWrapper.isWritableProperty(key)) {
                    try {
                        beanWrapper.setPropertyValue(key, value);
                    } catch (Exception e) {
                        log.warn("Failed to set property [{}] with value [{}] to {}",
                                key, value, targetClass.getSimpleName());
                    }
                }
            });

            return target;
        } catch (Exception e) {
            log.error("Failed to convert Map to {}", targetClass.getSimpleName(), e);
            throw new BusinessException(
                    String.format("Map to Bean conversion failed to %s", targetClass.getSimpleName()), e);
        }
    }

    /**
     * JavaBean对象转换为Map
     * <p>将对象的所有可读属性转换为Map的键值对，排除class属性</p>
     *
     * @param bean 源JavaBean对象
     * @return Map数据，如果bean为null则返回空Map
     * @throws BusinessException 当Bean转换失败时抛出
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            return Collections.emptyMap();
        }

        try {
            Map<String, Object> resultMap = new HashMap<>(16);
            BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
            PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();

            for (PropertyDescriptor pd : propertyDescriptors) {
                String propertyName = pd.getName();
                // 排除class属性
                if (!"class".equals(propertyName) && beanWrapper.isReadableProperty(propertyName)) {
                    Object propertyValue = beanWrapper.getPropertyValue(propertyName);
                    resultMap.put(propertyName, propertyValue);
                }
            }

            return resultMap;
        } catch (Exception e) {
            log.error("Failed to convert Bean {} to Map", bean.getClass().getSimpleName(), e);
            throw new BusinessException(
                    String.format("Bean to Map conversion failed from %s",
                            bean.getClass().getSimpleName()), e);
        }
    }

    /**
     * 从Map中获取BigDecimal值，处理各种类型转换
     * @param map 原始Map
     * @param key 键
     * @return BigDecimal值，如果转换失败返回null
     */
    public static BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        return getBigDecimal(map, key, null);
    }

    /**
     * 从Map中获取BigDecimal值，处理各种类型转换
     * @param map 原始Map
     * @param key 键
     * @param defaultValue 默认值
     * @return BigDecimal值，如果转换失败返回默认值
     */
    public static BigDecimal getBigDecimal(Map<String, Object> map, String key, BigDecimal defaultValue) {
        return Optional.ofNullable(map.get(key))
                .map(BeanConverterUtil::convertToBigDecimal)
                .orElse(defaultValue);
    }

    private static BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return new BigDecimal(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format: " + value, e);
            }
        } else {
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert " + value.getClass().getSimpleName()
                        + " to BigDecimal: " + value, e);
            }
        }
    }
}