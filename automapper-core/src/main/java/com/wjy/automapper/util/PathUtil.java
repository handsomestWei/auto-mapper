package com.wjy.automapper.util;

/**
 * @author weijiayu
 * @date 2024/5/27 10:39
 */
public class PathUtil {

    public static String getResourcePath() {
        return PathUtil.class.getClassLoader().getResource("").getPath();
    }
}
