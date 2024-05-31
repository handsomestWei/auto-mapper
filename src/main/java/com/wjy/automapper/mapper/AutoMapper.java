package com.wjy.automapper.mapper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.constant.XmlConstant;
import com.wjy.automapper.rule.RuleManager;
import com.wjy.automapper.rule.xml.RuleRootElement;
import com.wjy.automapper.rule.xml.RuleSingleElement;
import com.wjy.automapper.schema.SchemaManager;
import com.wjy.automapper.util.JsonPathUtil;
import com.wjy.automapper.util.JsonUtil;
import com.wjy.automapper.util.PathUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/24 15:15
 */
@Slf4j
public class AutoMapper {

    private volatile static SchemaManager schemaManager;
    private volatile static RuleManager ruleManager;
    private volatile static AutoMapper autoMapper;

    private AutoMapper() {
        String resourcePath = PathUtil.getResourcePath();
        new AutoMapper(resourcePath + XmlConstant.SCHEMA_FILE_DEFAULT_DIR,
            resourcePath + XmlConstant.RULE_FILE_DEFAULT_DIR);
    }

    private AutoMapper(String schemaFileDir, String ruleFileDir) {
        schemaManager = SchemaManager.newInstance(schemaFileDir);
        ruleManager = RuleManager.newInstance(ruleFileDir);
    }

    public static AutoMapper newInstance() {
        String resourcePath = PathUtil.getResourcePath();
        return newInstance(resourcePath + XmlConstant.SCHEMA_FILE_DEFAULT_DIR,
            resourcePath + XmlConstant.RULE_FILE_DEFAULT_DIR);
    }

    public static AutoMapper newInstance(String schemaFileDir, String ruleFileDir) {
        if (autoMapper != null) {
            autoMapper.destroy();
        }
        autoMapper = new AutoMapper(schemaFileDir, ruleFileDir);
        return autoMapper;
    }

    public static AutoMapper getInstance() {
        if (autoMapper == null) {
            synchronized (AutoMapper.class) {
                if (autoMapper == null) {
                    autoMapper = new AutoMapper();
                }
            }
        }
        return autoMapper;
    }

    public void destroy() {
        try {
            schemaManager.destroy();
        } catch (Exception e) {
        }
        try {
            ruleManager.destroy();
        } catch (Exception e) {
        }
    }

    public JSONObject map(JSONObject srcObject, String targetSchemaId, String ruleId) {
        JSONObject targetObject = null;
        try {
            JSONObject targetTplObject = SchemaManager.getInstance().getTplObjWithXml(targetSchemaId);
            targetObject = JsonUtil.shallowClone(targetTplObject);
            RuleRootElement ruleRootElement = RuleManager.getInstance().getRule(ruleId);
            targetObject = map(srcObject, targetObject, targetTplObject, ruleRootElement);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    public JSONObject map(JSONObject srcObject, Class targetClass, String ruleId) {
        JSONObject targetObject = null;
        try {
            JSONObject targetTplObject = SchemaManager.getInstance().getTplObjWithClass(targetClass);
            targetObject = JsonUtil.shallowClone(targetTplObject);
            RuleRootElement ruleRootElement = RuleManager.getInstance().getRule(ruleId);
            targetObject = map(srcObject, targetObject, targetTplObject, ruleRootElement);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    public JSONObject map(JSONObject srcObject, JSONObject targetObject, JSONObject targetTplObject,
        RuleRootElement ruleRootElement) {
        try {
            List<RuleSingleElement> ruleList = ruleRootElement.getRs();
            for (RuleSingleElement r : ruleList) {
                targetObject =
                    map(srcObject, r.getFrom(), targetObject, r.getTo(), targetTplObject, r.getFunc(), r.getVal());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    private JSONObject map(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObject, String func, String val) {
        try {
            if (StringUtils.isEmpty(targetPath)) {
                return targetObject;
            }
            if (StringUtils.isNotEmpty(func)) {
                return MapperUtil.callFunc(srcObject, srcPath, targetObject, targetPath, targetTplObject, func, val);
            }

            Object srcValue = JSONPath.eval(srcObject, srcPath);
            if (srcValue == null) {
                return targetObject;
            }
            if (srcValue instanceof JSONArray && JsonPathUtil.containsArray(targetPath)) {
                // 多对多
                Object[] objects = ((JSONArray)srcValue).toArray();
                JsonPathUtil.arraySetManyToMany(targetObject, targetPath, objects);
            } else {
                // 1对1
                JSONPath.set(targetObject, targetPath, srcValue);
            }
            return targetObject;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return targetObject;
        }
    }
}
