package com.yuanzhy.tools.sql.input.impl;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.SqlUtil;
import com.yuanzhy.tools.sql.input.BaseFolderInput;
import com.yuanzhy.tools.sql.input.IInput;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Iterator;

/**
 * 良哥的sqlOnly分析结果做为输入
 * 分析同一秒 同一类SQL执行的数量，倒序排一下
 *
 * @Author yuanzhy
 * @Date 2018/8/8
 */
public class LianggeResultInput extends BaseFolderInput implements IInput {

    public LianggeResultInput(String path) {
        super(path);
    }

    @Override
    protected boolean acceptFile(String filename) {
        return filename.contains("[") && filename.endsWith(".txt");
    }

    @Override
    public Iterator<SqlLog> iterator0() {
        return new LianggeResultIterator();
    }

    /**
     * Liangge迭代器实现
     */
    private class LianggeResultIterator extends BaseFolderIterator {

        @Override
        protected SqlLog parseLog(String log) {
            SqlLog sqlLog = new SqlLog();
            String filename = files[fileIndex].getName();
            String threadId = StringUtils.substringBetween(filename, "[", "]");
            if (threadId == null) {
                threadId = StringUtils.substringBetween(filename, "[", ".txt");
            }
            sqlLog.setThreadId(threadId);
            sqlLog.setTime(StringUtils.substringBefore(log, " ["));
            // 正则性能较差，此处没用
            String logContent = StringUtils.substringAfter(log, "jdbc.sqlonly -").trim();
            if (logContent.contains("java:")) {
                // class and  method
                String classMethod = StringUtils.substringBefore(logContent,"(").trim();
                sqlLog.setClassName(StringUtils.substringBeforeLast(classMethod,"."));
                sqlLog.setMethodName(StringUtils.substringAfterLast(classMethod,"."));
                // line number
                String lineNumber = StringUtils.substringBefore(log,")");
                lineNumber = StringUtils.substringAfter(lineNumber, "java:");
                sqlLog.setLineNumber(NumberUtils.toInt(lineNumber));
                // sql
                String sql = StringUtils.substringAfter(log, ")").trim();
                // 如果日志开头是 “59. select xxx”这种形式，需要把前置的数据和点去掉
                sqlLog.setSql(SqlUtil.deleteNumPrefix(sql));
            } else {
                // sql
                sqlLog.setSql(SqlUtil.deleteNumPrefix(logContent));
            }
            return sqlLog;
        }

        @Override
        protected boolean isItemEnd(String line) {
            return "".equals(line.trim());
        }

        @Override
        protected boolean isItemError(String line) {
            return false;
        }
    }
}
