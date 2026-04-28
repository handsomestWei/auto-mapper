package com.wjy.automapper.mapper.func.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;
import com.wjy.automapper.util.JsonPathUtil;
import com.wjy.automapper.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 对象初始化
 * 
 * @author weijiayu
 * @date 2024/6/11 10:20
 */
@Slf4j
public class NewObjectFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.NEW_OBJECT.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
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
}
