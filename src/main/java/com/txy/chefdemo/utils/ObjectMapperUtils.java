package com.txy.chefdemo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author tianxinyu
 * @Create 2026-01-08
 */
public class ObjectMapperUtils {
    // 线程安全，可复用的 ObjectMapper
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ObjectMapperUtils() {
        // 私有构造，防止实例化
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJSON(Object obj) {
        if (ObjectUtils.anyNull(obj)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象序列化JSON失败", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJSON(String json, Class<T> clazz) {
        if (ObjectUtils.anyNull(json, clazz)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON反序列化为对象失败", e);
        }
    }

    /**
     * JSON 字符串转 List<T>
     */
    public static <T> List<T> fromJSONToList(String json, Class<T> clazz) {
        if (ObjectUtils.anyNull(json, clazz)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException("JSON反序列化为List失败", e);
        }
    }

    /**
     * JSON 字符串反序列化为 Map<K, V>
     */
    public static <K, V> Map<K, V> fromJSONToMap(String json, Class<K> keyClazz, Class<V> valueClazz) {
        if (ObjectUtils.anyNull(json, keyClazz, valueClazz)) {
            return null;
        }
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructMapType(Map.class, keyClazz, valueClazz);
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化为 Map 失败", e);
        }
    }
}
