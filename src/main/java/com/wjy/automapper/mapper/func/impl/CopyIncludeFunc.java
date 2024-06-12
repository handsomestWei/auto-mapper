package com.wjy.automapper.mapper.func.impl;

import java.util.Arrays;
import java.util.HashSet;

import com.alibaba.fastjson.JSONObject;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * 按key复制，包含指定key
 * 
 * @author weijiayu
 * @date 2024/6/11 10:47
 */
@Slf4j
public class CopyIncludeFunc extends CopyBaseFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.COPY_INCLUDE.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            HashSet<String> includeKeySet = new HashSet<>(Arrays.asList(val.split(",")));
            return copyWithFilter(srcObject, srcPath, targetObject, targetPath, includeKeySet, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }
}
