package com.yuanzhy.tools.sql.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yuanzhy
 * @date 2018/6/14
 */
public class SqlUtil {

    private static final Pattern START_NUM_PATTERN = Pattern.compile("^\\d+\\.\\s+");


    public static boolean isSql(String str) {
        return isDMLSql(str) || isSelectSql(str);
    }

    public static boolean isSelectSql(String str) {
        return str.startsWith("select") || str.startsWith("SELECT")
                || str.startsWith("set rowcount") || str.startsWith("SET ROWCOUNT");
    }

    public static boolean isDMLSql(String str) {
        return str.startsWith("insert") || str.startsWith("INSERT") ||
                str.startsWith("update") || str.startsWith("UPDATE") ||
                str.startsWith("delete") || str.startsWith("DELETE");
    }

    /**
     * 如果日志开头是 “49. select xxx”这种形式，需要把前置的数据和点去掉
     *
     * @param str str
     * @return
     */
    public static String deleteNumPrefix(String str) {
        Matcher m = START_NUM_PATTERN.matcher(str);
        if (m.find()) {
            str = m.replaceFirst("");
        }
        return str;
    }
}
