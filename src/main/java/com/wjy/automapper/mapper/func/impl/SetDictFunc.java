package com.wjy.automapper.mapper.func.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;
import com.wjy.automapper.util.JsonPathUtil;
import com.wjy.automapper.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 字典值转换，支持双向。多值用逗号分隔，短划线左边是原值，短划线右边是转换后值。例：0-1,1-2
 * 
 * @author weijiayu
 * @date 2024/6/11 10:42
 */
@Slf4j
public class SetDictFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.SET_DICT.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            Map.Entry<String, JSONObject> entry =
                JsonUtil.getNotNonePathAndObjectPair(srcObject, srcPath, targetObject, targetPath);
            if (entry == null) {
                return targetObject;
            }
            JSONObject tmpObject = entry.getValue();
            String tmpPath = entry.getKey();

            Object searchObject = JSONPath.eval(tmpObject, tmpPath);
            if (searchObject == null) {
                return targetObject;
            }
            HashMap<String, String> dictMap = new HashMap<>();
            String[] dictPairs = val.split(",");
            for (String dictPair : dictPairs) {
                String[] tokens = dictPair.split("-");
                dictMap.put(tokens[0], tokens[1]);
            }
            if (searchObject instanceof List) {
                Object[] objects = ((List)searchObject).toArray();
                int len = objects.length;
                for (int i = 0; i < len; i++) {
                    if (objects[i] == null) {
                        continue;
                    }
                    String itemPath = tmpPath.replace(JsonPathUtil.LIST_TOKEN, "[" + i + "]");
                    String dictVal = dictMap.get(objects[i].toString());
                    if (StringUtils.isEmpty(dictVal)) {
                        continue;
                    }
                    JSONPath.set(tmpObject, itemPath, dictVal);
                }
            } else {
                String dictVal = dictMap.get(searchObject.toString());
                if (StringUtils.isNotEmpty(dictVal)) {
                    JSONPath.set(tmpObject, tmpPath, val);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }
}
