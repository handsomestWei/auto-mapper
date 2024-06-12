package com.wjy.automapper.mapper.func;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.wjy.automapper.util.ClassUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 函数注册和调用
 * 
 * @author weijiayu
 * @date 2024/6/7 17:27
 */
@Slf4j
public class AutoMapperFuncManager {

    private final String innerMapperFuncPackageName = "com.wjy.automapper.mapper.func.impl";
    // 内置函数
    private HashMap<String, AutoMapperFunc> innerFuncMap = new HashMap<>();
    // 自定义函数。需要实现AutoMapperFunc接口，以JDK SPI方式扩展
    private HashMap<String, AutoMapperFunc> spiFuncMap = new HashMap<>();
    private volatile static AutoMapperFuncManager funcManager;

    private AutoMapperFuncManager() {
        registerMapperFunc();
    }

    public static AutoMapperFuncManager newInstance() {
        funcManager = new AutoMapperFuncManager();
        return funcManager;
    }

    public static AutoMapperFuncManager getInstance() {
        if (funcManager == null) {
            synchronized (AutoMapperFuncManager.class) {
                if (funcManager == null) {
                    funcManager = new AutoMapperFuncManager();
                }
            }
        }
        return funcManager;
    }

    public JSONObject execute(String funcName, JSONObject srcObject, String srcPath, JSONObject targetObject,
        String targetPath, JSONObject targetTplObject, String val) {
        try {
            if (StringUtils.isEmpty(funcName)) {
                return targetObject;
            }
            // 函数名相同时，优先使用自定义
            AutoMapperFunc func = spiFuncMap.get(funcName);
            if (func == null) {
                func = innerFuncMap.get(funcName);
            }
            if (func != null) {
                targetObject = func.execute(srcObject, srcPath, targetObject, targetPath, targetTplObject, val);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    private void registerMapperFunc() {
        registerInnerMapperFunc();
        registerSpiMapperFunc();
    }

    // 内置函数注册
    private void registerInnerMapperFunc() {
        try {
            List<AutoMapperFunc> innerFuncList =
                ClassUtil.getClassInstanceWithPackageScan(innerMapperFuncPackageName, AutoMapperFunc.class);
            for (AutoMapperFunc innerFunc : innerFuncList) {
                if (StringUtils.isEmpty(innerFunc.getFuncName())) {
                    continue;
                }
                innerFuncMap.put(innerFunc.getFuncName(), innerFunc);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    // SPI方式扩展函数注册
    private void registerSpiMapperFunc() {
        List<AutoMapperFunc> spiFuncList = ClassUtil.getClassInstanceWithSpi(AutoMapperFunc.class);
        for (AutoMapperFunc spiFunc : spiFuncList) {
            if (StringUtils.isEmpty(spiFunc.getFuncName())) {
                continue;
            }
            spiFuncMap.put(spiFunc.getFuncName(), spiFunc);
        }
    }
}
