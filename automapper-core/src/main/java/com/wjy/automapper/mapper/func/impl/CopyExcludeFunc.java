package com.wjy.automapper.mapper.func.impl;

import java.util.Arrays;
import java.util.HashSet;

import com.alibaba.fastjson.JSONObject;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * 按key复制，跳过指定key
 * 
 * @author weijiayu
 * @date 2024/6/11 10:45
 */
@Slf4j
public class CopyExcludeFunc extends CopyBaseFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.COPY_EXCLUDE.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            HashSet<String> excludeKeySet = new HashSet<>(Arrays.asList(val.split(",")));
            return copyWithFilter(srcObject, srcPath, targetObject, targetPath, excludeKeySet, false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }
}
