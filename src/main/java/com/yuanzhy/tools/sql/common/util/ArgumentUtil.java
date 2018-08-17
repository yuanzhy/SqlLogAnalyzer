package com.yuanzhy.tools.sql.common.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yuanzhy
 * @date 2018/8/11
 */
public final class ArgumentUtil {

    private static Logger log = LoggerFactory.getLogger(ArgumentUtil.class);

    private static Map<String, String> argsMap = new HashMap<String, String>();

    private static String[] args;

    public static String getArgument(String key) {
        return argsMap.get(key);
    }

    public static void parseArgs(String[] args) {
        if (ArrayUtils.isEmpty(args)) {
            argsMap.put("path", getPath(null));
        } else {
            ArgumentUtil.args = args;
            argsMap.put("path", args[0]); // 兼容之前的直接传递path
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    String key = arg.substring(2, arg.indexOf("="));
                    String value = arg.substring(arg.indexOf("=") + 1);
                    if ("path".equals(key)) {
                        value = getPath(value);
                    }
                    argsMap.put(key, value);
                }
            }
        }
    }

    /**
     * @param param param
     * @return
     */
    private static String getPath(String param) {
        String jarPath = ConfigUtil.getJarPath();
        if (param == null || param.startsWith("--")) {
            return jarPath;
        }
        if (isAbsolutePath(param)) {
            return param;
        } else if (param.startsWith("./")) {
            return jarPath + param.substring(1);
        } else if (param.startsWith("../")) {
            int count = StringUtils.countMatches(param, "../");
            File path = new File(jarPath);
            for (int i = 0; i < count; i++) {
                path = path.getParentFile();
            }
            return path.getAbsolutePath();
        } else {
            return jarPath + "/" + param;
        }
    }

    private static boolean isAbsolutePath(String path) {
        if (path.startsWith("/")) {
            return true;
        }
        if (isWindows()) {// windows
            if (path.contains(":") || path.startsWith("\\")) {
                return true;
            }
        } else {// not windows, just unix compatible
            if (path.startsWith("~")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否windows系统
     */
    private static boolean isWindows() {
        boolean isWindows = false;
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String sharpOsName = osName.replaceAll("windows", "{windows}")
                    .replaceAll("^win([^a-z])", "{windows}$1").replaceAll("([^a-z])win([^a-z])", "$1{windows}$2");
            isWindows = sharpOsName.contains("{windows}");
        } catch (Exception e) {
            log.warn("获取操作系统类型出错", e);
        }
        return isWindows;
    }
}
