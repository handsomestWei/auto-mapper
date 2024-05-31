package com.wjy.automapper.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author weijiayu
 * @date 2024/5/23 16:39
 */
@Slf4j
public class JsonUtil {

    public static JSONObject newJsonObject() {
        // 保证key的排序按put顺序
        return new JSONObject(true);
    }

    // 深拷贝，保留空值字段
    public static JSONObject deepClone(Object object) {
        return JSON.parseObject(JSON.toJSONString(object, SerializerFeature.SortField,
            SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty), Feature.OrderedField);
    }

    // 浅拷贝，只拷贝外层属性
    public static JSONObject shallowClone(JSONObject tplObj) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry<String, Object> entry : tplObj.getInnerMap().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof JSONArray || value instanceof JSONObject) {
                    jsonObject.put(key, null);
                } else {
                    jsonObject.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return jsonObject;
    }

    // 格式化缩进
    public static String format(JSONObject jsonObject) {
        return JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.SortField,
            SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
    }

    // 使用pojo class生成json模板对象
    public static JSONObject genJsonObject(Class clazz) {
        try {
            JSONObject jsonObject = new JSONObject();
            Object currentObj = clazz.newInstance();
            Field[] fields = currentObj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = null;
                Type t = field.getType();
                if (List.class.getTypeName().equals(t.getTypeName())) {
                    // 提取List泛型的实际类型
                    t = ((ParameterizedTypeImpl)field.getGenericType()).getActualTypeArguments()[0];
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(genJsonObject(t.getClass()));
                    fieldValue = jsonArray;
                } else if (t.getTypeName().startsWith("java.")) {
                    // 基础类型
                    fieldValue = field.get(currentObj);
                } else {
                    // 自定义pojo
                    fieldValue = genJsonObject(t.getClass());
                }
                jsonObject.put(field.getName(), fieldValue);
            }
            return jsonObject;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
