package com.wjy.automapper.mapper.func.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;
import com.wjy.automapper.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * list初始化
 * 
 * @author weijiayu
 * @date 2024/6/11 9:51
 */
@Slf4j
public class NewListFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.NEW_LIST.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
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
}
