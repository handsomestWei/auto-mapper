package com.wjy.automapper.mapper;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.util.JsonPathUtil;
import com.wjy.automapper.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/25 11:52
 */
@Slf4j
public class MapperUtil {

    private static Class<?>[] methodeParamClassArray =
        {JSONObject.class, String.class, JSONObject.class, String.class, JSONObject.class, String.class};

    public static JSONObject callFunc(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObject, String func, String val) {
        try {
            String className = null;
            String methodName = null;
            String[] funcInfo = func.split("#");
            if (funcInfo.length == 2) {
                // 自定义函数
                className = funcInfo[0];
                methodName = funcInfo[1];
            } else {
                // 内置函数
                className = MapperUtil.class.getTypeName();
                methodName = func;
            }
            // 反射调用
            Object funcObject = Class.forName(className).newInstance();
            return (JSONObject)funcObject.getClass().getMethod(methodName, methodeParamClassArray).invoke(funcObject,
                srcObject, srcPath, targetObject, targetPath, targetTplObject, val);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return targetObject;
        }
    }

    public static JSONObject newList(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            int size = JSONPath.size(srcObject, srcPath);
            JSONArray tplJsonArray = (JSONArray)JSONPath.eval(targetTplObj, targetPath);
            Object tplItem = new Object();
            if (tplJsonArray.size() > 0) {
                tplItem = tplJsonArray.get(0);
            }
            Object targetJsonArray = JSONPath.eval(targetObject, targetPath);
            if (targetJsonArray != null) {
                return targetObject;
            }
            targetJsonArray = new JSONArray();
            JSONPath.set(targetObject, targetPath, targetJsonArray);
            for (int i = 0; i < size; i++) {
                ((JSONArray)targetJsonArray).add(JsonUtil.shallowClone((JSONObject)tplItem));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    public static JSONObject newObject(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            Object tplItem = JsonPathUtil.getTplItem(targetTplObj, targetPath);
            JSONObject item = JsonUtil.shallowClone((JSONObject)tplItem);
            if (JsonPathUtil.containsArray(targetPath)) {
                JsonPathUtil.arraySetOneToMany(targetObject, targetPath, item);
            } else {
                JSONPath.set(targetObject, targetPath, item);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    public static JSONObject setVal(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            if (JsonPathUtil.containsArray(targetPath)) {
                JsonPathUtil.arraySetOneToMany(targetObject, targetPath, val);
            } else {
                JSONPath.set(targetObject, targetPath, val);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    public static JSONObject append(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            Object srcValue = JSONPath.eval(srcObject, srcPath);
            if (srcValue == null) {
                return targetObject;
            }

            boolean isListType = false;
            Object[] srcValues = null;
            if (srcValue instanceof JSONArray) {
                srcValues = ((JSONArray)srcValue).toArray();
                isListType = true;
            }

            if (JsonPathUtil.containsArray(targetPath)) {
                JSONArray jsonArray = JsonPathUtil.getArray(targetObject, targetPath);
                if (isListType) {
                    for (Object object : srcValues) {
                        jsonArray.add(object);
                    }
                } else {
                    jsonArray.add(srcValue);
                }
            } else {
                String appendToken = val;
                if (StringUtils.isEmpty(appendToken)) {
                    appendToken = ",";
                }
                if (isListType) {
                    StringBuilder sb = new StringBuilder();
                    for (Object object : srcValues) {
                        sb.append(object.toString()).append(appendToken);
                    }
                    // 调整尾token
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    srcValue = sb.toString();
                }
                Object targetVal = JSONPath.eval(targetObject, targetPath);
                if (targetVal != null) {
                    if (srcValue.toString().length() > 0) {
                        // 调整头token
                        srcValue = targetVal + appendToken + srcValue;
                    }
                }
                JSONPath.set(targetObject, targetPath, srcValue);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }
}
