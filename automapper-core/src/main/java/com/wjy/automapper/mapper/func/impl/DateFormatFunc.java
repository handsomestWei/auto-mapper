package com.wjy.automapper.mapper.func.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.wjy.automapper.mapper.func.AutoMapperFunc;
import com.wjy.automapper.mapper.func.InnerMapperFuncEnum;
import com.wjy.automapper.util.JsonPathUtil;
import com.wjy.automapper.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 时间格式化。逗号分隔，逗号左边是原格式，逗号右边是转换后格式，对应位置值为空串则视为long时间戳。例：yyyy-MM-dd'T'HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.SSS
 * 
 * @author weijiayu
 * @date 2024/6/11 10:38
 */
@Slf4j
public class DateFormatFunc implements AutoMapperFunc {

    @Override
    public String getFuncName() {
        return InnerMapperFuncEnum.DATE_FORMAT.getFuncName();
    }

    @Override
    public JSONObject execute(JSONObject srcObject, String srcPath, JSONObject targetObject, String targetPath,
        JSONObject targetTplObj, String val) {
        try {
            Map.Entry<String, JSONObject> entry =
                JsonUtil.getNotNonePathAndObjectPair(srcObject, srcPath, targetObject, targetPath);
            if (entry == null) {
                return targetObject;
            }
            JSONObject tmpObject = entry.getValue();
            String tmpPath = entry.getKey();
            // 保留末尾空串
            String[] dateFormats = val.split(",", -1);
            Object searchObject = JSONPath.eval(tmpObject, tmpPath);
            if (searchObject == null) {
                return targetObject;
            }
            if (searchObject instanceof List) {
                Object[] objects = ((List)searchObject).toArray();
                int len = objects.length;
                for (int i = 0; i < len; i++) {
                    if (objects[i] == null) {
                        continue;
                    }
                    String itemPath = tmpPath.replace(JsonPathUtil.LIST_TOKEN, "[" + i + "]");
                    JSONPath.set(tmpObject, itemPath,
                        convertDate(objects[i].toString(), dateFormats[0], dateFormats[1]));
                }
            } else {
                JSONPath.set(tmpObject, tmpPath, convertDate(searchObject.toString(), dateFormats[0], dateFormats[1]));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return targetObject;
    }

    private Object convertDate(String inDateVal, String inFormat, String outFormat) throws Exception {
        Date inDateTime = null;
        if (StringUtils.isEmpty(inFormat)) {
            inDateTime = new Date(Long.parseLong(inDateVal));
        } else {
            inDateTime = new SimpleDateFormat(inFormat).parse(inDateVal);
        }

        Object outDateVal = null;
        if (StringUtils.isEmpty(outFormat)) {
            outDateVal = inDateTime.getTime();
        } else {
            outDateVal = new SimpleDateFormat(outFormat).format(inDateTime);
        }
        return outDateVal;
    }
}
