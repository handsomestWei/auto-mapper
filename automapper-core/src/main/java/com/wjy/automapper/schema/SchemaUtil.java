package com.wjy.automapper.schema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.wjy.automapper.constant.XmlConstant;
import com.wjy.automapper.schema.xml.SchemaPropertyElement;
import com.wjy.automapper.schema.xml.SchemaRootElement;
import com.wjy.automapper.util.JsonUtil;
import com.wjy.automapper.util.XmlUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/22 16:43
 */
@Slf4j
public class SchemaUtil extends XmlUtil {

    public static String createSchemaFile(String schemaId, String schemaDesc, String jsonEgStr, String outPutDir) {
        try {
            String filePath = outPutDir + schemaId + XmlConstant.SCHEMA_FILE_SUFFIX;
            File f = new File(filePath);
            if (f.isDirectory()) {
                return null;
            }
            if (!f.exists() && !f.createNewFile()) {
                return null;
            }
            try (OutputStream outputStream = new FileOutputStream(f)) {
                SchemaRootElement rootElement = jsonToXmlDoc(schemaId, schemaDesc, jsonEgStr);
                marshaller(rootElement, outputStream);
            }
            return filePath;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static SchemaRootElement jsonToXmlDoc(String schemaId, String schemaDesc, String jsonEgStr) {
        JSONObject jsonObject = JSON.parseObject(jsonEgStr, Feature.OrderedField);
        SchemaRootElement rootElement = new SchemaRootElement(schemaId, schemaDesc);
        List<SchemaPropertyElement> ps = new ArrayList<>();
        Set<String> keySet = jsonObject.keySet();
        for (String keyName : keySet) {
            ps.add(jsonToXmlElement(keyName, jsonObject.get(keyName)));
        }
        rootElement.setPs(ps);
        return rootElement;
    }

    public static SchemaRootElement loadSchemaFile(String filePath) {
        try {
            return (SchemaRootElement)unmarshaller(SchemaRootElement.class, new File(filePath));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static JSONObject xmlDocToJson(SchemaRootElement rootElement) {
        try {
            if (rootElement == null) {
                return null;
            }
            JSONObject rootObject = JsonUtil.newJsonObject();
            List<SchemaPropertyElement> ps = rootElement.getPs();
            if (CollectionUtils.isEmpty(ps)) {
                return rootObject;
            }
            for (SchemaPropertyElement pElement : ps) {
                rootObject.put(pElement.getName(), xmlElementToJson(pElement));
            }
            return rootObject;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getSchemaId(String schemaFileName) {
        try {
            if (StringUtils.isEmpty(schemaFileName) || !schemaFileName.endsWith(XmlConstant.SCHEMA_FILE_SUFFIX)) {
                return null;
            }
            return schemaFileName.split(XmlConstant.SCHEMA_FILE_SUFFIX)[0];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private static SchemaPropertyElement jsonToXmlElement(String name, Object object) {
        SchemaPropertyElement element = new SchemaPropertyElement();
        element.setName(name);
        if (object instanceof JSONArray || object instanceof JSONObject) {
            if (object instanceof JSONArray) {
                element.setType(List.class.getTypeName());
                JSONArray jsonArray = (JSONArray)object;
                object = jsonArray.get(0);
            } else {
                element.setType(Object.class.getTypeName());
            }
            element.setEg("");
            List<SchemaPropertyElement> ps = new ArrayList<>();
            if (object instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject)object;
                Set<String> keySet = jsonObject.keySet();
                for (String keyName : keySet) {
                    ps.add(jsonToXmlElement(keyName, jsonObject.get(keyName)));
                }
            }
            element.setPs(ps);
        } else {
            element.setType(object.getClass().getTypeName());
            element.setEg(object.toString());
        }

        return element;
    }

    private static Object xmlElementToJson(SchemaPropertyElement pElement) {
        JSONArray jsonArray = new JSONArray();
        Object elementObject = null;
        String defVal = pElement.getDefVal();
        String elementType = pElement.getType();
        boolean isTypeList = false;
        if (List.class.getTypeName().equals(elementType)) {
            isTypeList = true;
        } else if (StringUtils.isNotEmpty(defVal)) {
            // 设置默认值
            try {
                elementObject = Class.forName(elementType).getConstructor(String.class).newInstance(defVal);
            } catch (Exception e) {
            }
        }
        List<SchemaPropertyElement> ps = pElement.getPs();
        if (!CollectionUtils.isEmpty(ps)) {
            elementObject = JsonUtil.newJsonObject();
            for (SchemaPropertyElement pe : ps) {
                ((JSONObject)elementObject).put(pe.getName(), xmlElementToJson(pe));
            }
        }
        if (isTypeList) {
            if (elementObject != null) {
                // 避免List<T>泛型被擦除，先在数组里填充一个对象，保证类型的属性不丢失
                jsonArray.add(elementObject);
            }
            return jsonArray;
        } else {
            return elementObject;
        }
    }
}
