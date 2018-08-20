package com.yuanzhy.tools.sql.common.util;

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

    public static boolean isSelectSql(String sql) {
        if (sql == null) {
            return false;
        }
        return sql.startsWith("select") || sql.startsWith("SELECT")
                || sql.startsWith("set rowcount") || sql.startsWith("SET ROWCOUNT");
    }

    public static boolean isDMLSql(String sql) {
        if (sql == null) {
            return false;
        }
        return sql.startsWith("insert") || sql.startsWith("INSERT") ||
                sql.startsWith("update") || sql.startsWith("UPDATE") ||
                sql.startsWith("delete") || sql.startsWith("DELETE");
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
