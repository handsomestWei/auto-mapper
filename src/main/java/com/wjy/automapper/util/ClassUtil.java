package com.wjy.automapper.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.rules.RuleFactory;

import com.sun.codemodel.JCodeModel;

import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author weijiayu
 * @date 2024/5/28 13:03
 */
@Slf4j
public class ClassUtil {

    // path例：$.data.param.name
    public static Field getDeepField(Class clazz, String fieldPath) throws Exception {
        try {
            String[] path = fieldPath.split("\\.");
            int len = path.length;
            Object currentObj = clazz.newInstance();
            for (int i = 1; i < len; i++) {
                Field field = currentObj.getClass().getDeclaredField(path[i]);
                field.setAccessible(true);
                if (i == len - 1) {
                    return field;
                }
                Type t = field.getType();
                if (List.class.getTypeName().equals(field.getType().getTypeName())) {
                    t = ((ParameterizedTypeImpl)field.getGenericType()).getActualTypeArguments()[0];
                }
                currentObj = Class.forName(t.getTypeName()).newInstance();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Class genClass(String jsonSchemaResourcePath, String className, String packageName, String outPutDir)
        throws Exception {
        JCodeModel codeModel = new JCodeModel();
        URL source = ClassUtil.class.getResource(jsonSchemaResourcePath);
        SchemaMapper mapper = new SchemaMapper(new RuleFactory(), new SchemaGenerator());
        mapper.generate(codeModel, className, packageName, source);
        codeModel.build(new File(outPutDir));
        return codeModel.getClass();
    }
}
