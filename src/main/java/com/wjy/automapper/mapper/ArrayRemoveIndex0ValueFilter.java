package com.wjy.automapper.mapper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.ValueFilter;

/**
 * 序列化过滤器，配合JSON.toJSONString()使用。删除数组首元素（该元素用来复制时保持泛型类型不丢失） 使用例：JSON.toJSONString(object, new
 * ArrayRemoveIndex0ValueFilter())
 *
 * @author weijiayu
 * @date 2024/5/24 11:34
 */
public class ArrayRemoveIndex0ValueFilter implements ValueFilter {

    @Override
    public Object process(Object object, String name, Object value) {
        if (value != null && value instanceof JSONArray) {
            JSONArray v = (JSONArray)value;
            if (v.size() > 0) {
                v.remove(0);
            }
        }
        return value;
    }
}
