package com.wjy.automapper.util;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

/**
 * @author weijiayu
 * @date 2024/5/25 15:44
 */
public class JsonPathUtil {

    // 识别list。区别于[*]，该通配符也可作用在对象返回对象的所有属性值
    public static final String LIST_TOKEN = "[0:]";

    public static boolean containsArray(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return path.contains(LIST_TOKEN);
    }

    public static JSONArray getArray(JSONObject jsonObject, String path) {
        return (JSONArray)JSONPath.eval(jsonObject, path.split("\\" + LIST_TOKEN)[0]);
    }

    public static Object getTplItem(JSONObject tplObject, String path) {
        path = path.replaceAll("\\" + LIST_TOKEN, "[0]");
        return JSONPath.eval(tplObject, path);
    }

    // 多对多
    public static void arraySetManyToMany(JSONObject targetObject, String targetPath, Object[] values) {
        int len = values.length;
        for (int i = 0; i < len; i++) {
            String itemPath = targetPath.replace(JsonPathUtil.LIST_TOKEN, "[" + i + "]");
            JSONPath.set(targetObject, itemPath, values[i]);
        }
    }

    // 一对多
    public static void arraySetOneToMany(JSONObject targetObject, String targetPath, Object value) {
        int size = JSONPath.size(targetObject, targetPath);
        for (int i = 0; i < size; i++) {
            String itemPath = targetPath.replace(JsonPathUtil.LIST_TOKEN, "[" + i + "]");
            JSONPath.set(targetObject, itemPath, value);
        }
    }

    // 一对多
    public static void arraySetOneToMany(JSONObject targetObject, String targetPath, JSONObject tplValue) {
        String arrayPath = targetPath.substring(0, targetPath.lastIndexOf("."));
        int size = JSONPath.size(targetObject, arrayPath);
        for (int i = 0; i < size; i++) {
            String itemPath = targetPath.replace(JsonPathUtil.LIST_TOKEN, "[" + i + "]");
            JSONPath.set(targetObject, itemPath, JsonUtil.shallowClone(tplValue));
        }
    }
}
