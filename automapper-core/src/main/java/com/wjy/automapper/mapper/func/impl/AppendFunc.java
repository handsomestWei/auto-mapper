package com.wjy.automapper.mapper.func.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;
import com.wjy.automapper.util.JsonPathUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 值追加。对于字符串拼接，拼接符可配置，默认逗号
 * 
 * @author weijiayu
 * @date 2024/6/11 10:32
 */
@Slf4j
public class AppendFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.APPEND.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            Object srcValue = JSONPath.eval(srcObject, srcPath);
            if (srcValue == null) {
                return targetObject;
            }

            boolean isListType = false;
            Object[] srcValues = null;
            if (srcValue instanceof List) {
                srcValues = ((List)srcValue).toArray();
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
