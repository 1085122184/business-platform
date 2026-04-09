package com.cjx.common.jpa.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity与DTO通用转换工具类
 * <p>提供Entity、DTO、VO之间的相互转换功能</p>
 * <p>支持单对象转换、列表转换、分页对象转换</p>
 *
 * @author Generator
 * @version 1.0.0
 * @since 2024-01-01
 */
public class EntityDtoConverter {
    /**
     * 私有构造方法，防止实例化
     */
    private EntityDtoConverter() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * Entity转DTO（单个对象）
     *
     * @param source      源Entity对象
     * @param targetClass 目标DTO类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的DTO对象
     */
    public static <S, T> T convertToDto(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Entity转DTO失败: " + e.getMessage(), e);
        }
    }

    /**
     * DTO转Entity（单个对象）
     *
     * @param source      源DTO对象
     * @param targetClass 目标Entity类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的Entity对象
     */
    public static <S, T> T convertToEntity(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("DTO转Entity失败: " + e.getMessage(), e);
        }
    }

    /**
     * Entity列表转DTO列表
     *
     * @param sourceList  源Entity列表
     * @param targetClass 目标DTO类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的DTO列表
     */
    public static <S, T> List<T> convertToDtoList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(source -> convertToDto(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * DTO列表转Entity列表
     *
     * @param sourceList  源DTO列表
     * @param targetClass 目标Entity类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的Entity列表
     */
    public static <S, T> List<T> convertToEntityList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(source -> convertToEntity(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * Entity分页对象转DTO分页对象
     *
     * @param sourcePage  源Entity分页对象
     * @param targetClass 目标DTO类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的DTO分页对象
     */
    public static <S, T> Page<T> convertToDtoPage(Page<S> sourcePage, Class<T> targetClass) {
        if (sourcePage == null) {
            return Page.empty();
        }

        List<T> targetList = convertToDtoList(sourcePage.getContent(), targetClass);
        return new PageImpl<>(targetList, sourcePage.getPageable(), sourcePage.getTotalElements());
    }

    /**
     * DTO分页对象转Entity分页对象
     *
     * @param sourcePage  源DTO分页对象
     * @param targetClass 目标Entity类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的Entity分页对象
     */
    public static <S, T> Page<T> convertToEntityPage(Page<S> sourcePage, Class<T> targetClass) {
        if (sourcePage == null) {
            return Page.empty();
        }

        List<T> targetList = convertToEntityList(sourcePage.getContent(), targetClass);
        return new PageImpl<>(targetList, sourcePage.getPageable(), sourcePage.getTotalElements());
    }

    /**
     * 自定义属性复制（忽略指定属性）
     *
     * @param source           源对象
     * @param targetClass      目标类型
     * @param ignoreProperties 需要忽略的属性名数组
     * @param <S>              源类型
     * @param <T>              目标类型
     * @return 转换后的目标对象
     */
    public static <S, T> T convertWithIgnore(S source, Class<T> targetClass, String... ignoreProperties) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target, ignoreProperties);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新Entity（将DTO的非空属性复制到Entity）
     *
     * @param source 源DTO对象
     * @param target 目标Entity对象
     * @param <S>    源类型
     * @param <T>    目标类型
     * @return 更新后的Entity对象
     */
    public static <S, T> T updateEntity(S source, T target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("源对象和目标对象不能为空");
        }

        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
        return target;
    }

    /**
     * 获取对象中值为null的属性名数组
     *
     * @param source 源对象
     * @return null属性名数组
     */
    private static String[] getNullPropertyNames(Object source) {
        Field[] fields = source.getClass().getDeclaredFields();
        Set<String> nullProperties = new HashSet<>();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(source) == null) {
                    nullProperties.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }

        return nullProperties.toArray(new String[0]);
    }

    /**
     * 批量更新Entity列表
     *
     * @param sourceList 源DTO列表
     * @param targetList 目标Entity列表
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 更新后的Entity列表
     */
    public static <S, T> List<T> updateEntityList(List<S> sourceList, List<T> targetList) {
        if (sourceList == null || targetList == null) {
            throw new IllegalArgumentException("源列表和目标列表不能为空");
        }

        if (sourceList.size() != targetList.size()) {
            throw new IllegalArgumentException("源列表和目标列表大小必须相同");
        }

        List<T> resultList = new ArrayList<>();
        for (int i = 0; i < sourceList.size(); i++) {
            resultList.add(updateEntity(sourceList.get(i), targetList.get(i)));
        }

        return resultList;
    }

    /**
     * Entity集合转DTO集合
     *
     * @param sourceSet   源Entity集合
     * @param targetClass 目标DTO类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的DTO集合
     */
    public static <S, T> Set<T> convertToDtoSet(Set<S> sourceSet, Class<T> targetClass) {
        if (sourceSet == null || sourceSet.isEmpty()) {
            return Collections.emptySet();
        }

        return sourceSet.stream()
                .map(source -> convertToDto(source, targetClass))
                .collect(Collectors.toSet());
    }

    /**
     * DTO集合转Entity集合
     *
     * @param sourceSet   源DTO集合
     * @param targetClass 目标Entity类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的Entity集合
     */
    public static <S, T> Set<T> convertToEntitySet(Set<S> sourceSet, Class<T> targetClass) {
        if (sourceSet == null || sourceSet.isEmpty()) {
            return Collections.emptySet();
        }

        return sourceSet.stream()
                .map(source -> convertToEntity(source, targetClass))
                .collect(Collectors.toSet());
    }

    /**
     * 深度复制对象（通过序列化实现）
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 深度复制后的对象
     */
    public static <S, T> T deepCopy(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyAllFields(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("深度复制失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制所有字段（包括父类字段）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    private static void copyAllFields(Object source, Object target) {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        while (sourceClass != null && sourceClass != Object.class) {
            Field[] fields = sourceClass.getDeclaredFields();
            for (Field sourceField : fields) {
                try {
                    Field targetField = getField(targetClass, sourceField.getName());
                    if (targetField != null) {
                        sourceField.setAccessible(true);
                        targetField.setAccessible(true);
                        Object value = sourceField.get(source);
                        if (value != null) {
                            targetField.set(target, value);
                        }
                    }
                } catch (Exception e) {
                    // 忽略无法复制的字段
                }
            }
            sourceClass = sourceClass.getSuperclass();
        }
    }

    /**
     * 获取指定名称的字段（包括父类字段）
     *
     * @param clazz     类对象
     * @param fieldName 字段名称
     * @return 字段对象
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 批量转换Map（key保持不变，value进行转换）
     *
     * @param sourceMap   源Map
     * @param targetClass 目标value类型
     * @param <K>         Key类型
     * @param <S>         源Value类型
     * @param <T>         目标Value类型
     * @return 转换后的Map
     */
    public static <K, S, T> Map<K, T> convertMapValues(Map<K, S> sourceMap, Class<T> targetClass) {
        if (sourceMap == null || sourceMap.isEmpty()) {
            return Collections.emptyMap();
        }

        return sourceMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> convertToDto(entry.getValue(), targetClass)
                ));
    }

    /**
     * 检查两个对象的指定属性是否相同
     *
     * @param obj1          对象1
     * @param obj2          对象2
     * @param propertyNames 需要比较的属性名
     * @return 相同返回true，否则返回false
     */
    public static boolean compareProperties(Object obj1, Object obj2, String... propertyNames) {
        if (obj1 == null || obj2 == null) {
            return obj1 == obj2;
        }

        for (String propertyName : propertyNames) {
            try {
                Field field1 = getField(obj1.getClass(), propertyName);
                Field field2 = getField(obj2.getClass(), propertyName);

                if (field1 == null || field2 == null) {
                    return false;
                }

                field1.setAccessible(true);
                field2.setAccessible(true);

                Object value1 = field1.get(obj1);
                Object value2 = field2.get(obj2);

                if (!Objects.equals(value1, value2)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    /**
     * 合并两个对象的属性（source的非空属性覆盖target的属性）
     *
     * @param source 源对象
     * @param target 目标对象
     * @param <T>    对象类型
     * @return 合并后的对象
     */
    public static <T> T merge(T source, T target) {
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }

        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object sourceValue = field.get(source);
                if (sourceValue != null) {
                    field.set(target, sourceValue);
                }
            } catch (Exception e) {
                // 忽略无法访问的字段
            }
        }

        return target;
    }

    /**
     * 判断对象是否为空（所有字段都为null）
     *
     * @param obj 待检查的对象
     * @return 为空返回true，否则返回false
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(obj) != null) {
                    return false;
                }
            } catch (Exception e) {
                // 忽略无法访问的字段
            }
        }

        return true;
    }

    /**
     * 获取对象中非空字段的Map
     *
     * @param obj 源对象
     * @return 非空字段的Map（字段名 -> 字段值）
     */
    public static Map<String, Object> getNonNullFieldMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> fieldMap = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    fieldMap.put(field.getName(), value);
                }
            } catch (Exception e) {
                // 忽略无法访问的字段
            }
        }

        return fieldMap;
    }

    /**
     * 将Map转换为对象
     *
     * @param map         源Map
     * @param targetClass 目标类型
     * @param <T>         目标类型
     * @return 转换后的对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> targetClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            Field[] fields = targetClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = map.get(field.getName());
                if (value != null) {
                    field.set(target, value);
                }
            }

            return target;
        } catch (Exception e) {
            throw new RuntimeException("Map转对象失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为Map
     *
     * @param obj 源对象
     * @return 转换后的Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (Exception e) {
                // 忽略无法访问的字段
            }
        }

        return map;
    }
}
