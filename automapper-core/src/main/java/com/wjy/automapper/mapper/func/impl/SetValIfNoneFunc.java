package com.wjy.automapper.mapper.func.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;
import com.wjy.automapper.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 对指定字段设值，如果该字段为none，支持双向
 * 
 * @author weijiayu
 * @date 2024/6/11 10:27
 */
@Slf4j
public class SetValIfNoneFunc implements AutoMapperFunc {
    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.SET_VAL_IF_NONE.getFuncName();
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
            Object searchObj = JSONPath.eval(tmpObject, tmpPath);
            if (searchObj == null || StringUtils.isEmpty(searchObj.toString())) {
                JSONPath.set(tmpObject, tmpPath, val);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }
}
