package com.wjy.automapper.mapper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wjy.automapper.constant.XmlConstant;
import com.wjy.automapper.schema.SchemaUtil;
import com.wjy.automapper.util.JsonUtil;
import com.wjy.automapper.util.PathUtil;

/**
 * @author weijiayu
 * @date 2024/5/31 14:43
 */
class AutoMapperTest {

    public static void main(String[] args) {
        // step1 初始化。单例模式，全局做一次即可
        AutoMapper autoMapper = AutoMapper.newInstance();
        try {
            // step2 使用json示例自动生成json schema xml
            String schemaId = "testJson";
            String jsonData = mockJsonExampleData();
            String schemaDir = PathUtil.getResourcePath() + XmlConstant.SCHEMA_FILE_DEFAULT_DIR;
            String schemaFilePath = SchemaUtil.createSchemaFile(schemaId, "for test", jsonData, schemaDir);
            System.out.println("schemaFilePath=" + schemaFilePath);

            // step3 定义转换规则，编写rule.xml
            String ruleId = "testRule";

            // step4 对源pojo，依据规则做转换
            JSONObject srcObj = mockSrcJsonObject();
            JSONObject rs = AutoMapper.getInstance().map(srcObj, "testJson", "testRule");
            System.out.println("mapper result=" + JsonUtil.format(rs));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            autoMapper.destroy();
        }
    }

    private static String mockJsonExampleData() {
        return "{\n" + "    \"code\": 0,\n" + "    \"msg\": \"success\",\n" + "    \"data\": [\n" + "        {\n"
            + "            \"id\": \"\",\n" + "            \"innerObj\": {\n" + "                \"objId\": \"\"\n"
            + "            }\n" + "        }\n" + "    ]\n" + "}";
    }

    private static JSONObject mockSrcJsonObject() {
        JSONObject srcObject = new JSONObject();
        srcObject.put("code", "test code");
        srcObject.put("msg", "test msg");
        JSONArray jsonArray = new JSONArray();
        srcObject.put("data", jsonArray);
        for (int i = 1; i <= 3; i++) {
            JSONObject object = new JSONObject();
            object.put("idd", "idd" + i);
            jsonArray.add(object);
        }
        return srcObject;
    }
}