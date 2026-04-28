package com.wjy.automapper.mapper.func;

import com.alibaba.fastjson.JSONObject;

/**
 * @author weijiayu
 * @date 2024/6/7 17:08
 */
public interface AutoMapperFunc {

    String getFuncName();

    JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val);
}
