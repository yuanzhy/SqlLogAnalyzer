package com.yuanzhy.tools.sql.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuanzhy
 * @date 2018/8/11
 */
public final class ArgumentUtil {

    private static Map<String, String> argsMap = new HashMap<String, String>();

    private static String[] args;

    public static String getArgument(String key) {
        return argsMap.get(key);
    }

    public static void setArgument(String key, String value) {
        argsMap.put(key, value);
    }

    public static String[] getArgs() {
        return args;
    }

    public static void setArgs(String[] args) {
        ArgumentUtil.args = args;
    }
}
