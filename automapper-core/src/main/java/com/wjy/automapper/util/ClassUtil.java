package com.wjy.automapper.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.rules.RuleFactory;
import org.reflections.Reflections;

import com.sun.codemodel.JCodeModel;
import com.wjy.automapper.mapper.func.AutoMapperFunc;

import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author weijiayu
 * @date 2024/5/28 13:03
 */
@Slf4j
public class ClassUtil {

    public static List getClassInstanceWithPackageScan(String packageName, Class clazz)
        throws IllegalAccessException, InstantiationException {
        List ls = new ArrayList();
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends AutoMapperFunc>> implClassSet = reflections.getSubTypesOf(clazz);
        for (Class<? extends AutoMapperFunc> implClass : implClassSet) {
            ls.add(implClass.newInstance());
        }
        return ls;
    }

    public static List getClassInstanceWithSpi(Class clazz) {
        List ls = new ArrayList();
        ServiceLoader loader = ServiceLoader.load(clazz);
        Iterator it = loader.iterator();
        while (it.hasNext()) {
            ls.add(it.next());
        }
        return ls;
    }

    // 使用路径获取类的深层属性。path例：$.data.param.name
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

    // 使用json schema生成.java文件
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
