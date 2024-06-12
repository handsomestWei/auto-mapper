package com.wjy.automapper.mapper.func.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

/**
 * @author weijiayu
 * @date 2024/6/11 10:44
 */
public class CopyBaseFunc {

    // includeFlag：true-包含，false-不包含
    public JSONObject copyWithFilter(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        HashSet<String> filterKeySet, boolean includeFlag) {
        Object srcValue = JSONPath.eval(srcObject, srcPath);
        if (srcValue == null) {
            return targetObject;
        }
        if (srcValue instanceof List) {
            Object[] srcSubObjects = ((List)srcValue).toArray();
            Object[] targetSubObjects = ((List)JSONPath.eval(targetObject, targetPath)).toArray();
            int len = srcSubObjects.length;
            if (targetSubObjects.length != len) {
                return targetObject;
            }
            for (int i = 0; i < len; i++) {
                copyWithFilter(((JSONObject)srcSubObjects[i]).getInnerMap().entrySet(), (JSONObject)targetSubObjects[i],
                    filterKeySet, includeFlag);
            }
        } else {
            copyWithFilter(((JSONObject)srcValue).getInnerMap().entrySet(),
                (JSONObject)JSONPath.eval(targetObject, targetPath), filterKeySet, includeFlag);
        }
        return targetObject;
    }

    public void copyWithFilter(Set<Map.Entry<String, Object>> srcEntrySet, JSONObject targetObject,
        HashSet<String> filterKeySet, boolean includeFlag) {
        for (Map.Entry<String, Object> entry : srcEntrySet) {
            if (includeFlag) {
                if (filterKeySet.contains(entry.getKey())) {
                    // include
                    targetObject.put(entry.getKey(), entry.getValue());
                } else {
                    continue;
                }
            } else {
                if (filterKeySet.contains(entry.getKey())) {
                    // exclude
                    continue;
                } else {
                    targetObject.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
