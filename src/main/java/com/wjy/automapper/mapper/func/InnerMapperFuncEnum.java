package com.wjy.automapper.mapper.func;

/**
 * 内置函数
 * 
 * @author weijiayu
 * @date 2024/6/11 10:18
 */
public enum InnerMapperFuncEnum {

    NEW_LIST("newList"), NEW_OBJECT("newObject"), SET_VAL("setVal"), SET_VAL_IF_NONE("setValIfNone"), APPEND("append"),
    DATE_FORMAT("dateFormat"), SET_DICT("setDict"), COPY_EXCLUDE("copyExclude"), COPY_INCLUDE("copyInclude");

    private String funcName;

    InnerMapperFuncEnum(String funcName) {
        this.funcName = funcName;
    }

    public String getFuncName() {
        return funcName;
    }
}
