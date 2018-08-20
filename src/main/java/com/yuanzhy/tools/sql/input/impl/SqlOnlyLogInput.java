package com.yuanzhy.tools.sql.input.impl;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.SqlUtil;
import com.yuanzhy.tools.sql.input.BaseFolderInput;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

/**
 * @author yuanzhy
 * @date 2018/6/16
 */
public class SqlOnlyLogInput extends BaseFolderInput {

    public SqlOnlyLogInput(String path) {
        super(path);
    }

    protected void buildInput(String path) {
        log.info("path is {}", path);
        files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("_sqlonly");
            }
        });
    }

    @Override
    public Iterator<SqlLog> iterator() {
        this.buildInput(path);
        if (ArrayUtils.isEmpty(files)) {
            log.error("没有找到sqlOnly日志文件");
            throw new NullPointerException("没有找到sqlOnly日志文件");
        }
        return new SqlOnlyLogIterator();
    }
    /**
     * jdbcLog迭代器实现
     */
    private class SqlOnlyLogIterator extends BaseFolderIterator {

        @Override
        protected SqlLog parseLog(String log) {
            SqlLog sqlLog = new SqlLog();
            // thread id 线程id叫法很多，无法确定规则，先使用中括号区分把
            String threadId = StringUtils.substringAfter(log, "] [");
            threadId = StringUtils.substringBefore(threadId,"]");
            sqlLog.setThreadId(threadId);
            sqlLog.setTime(StringUtils.substringBefore(log, " ["));
            // 正则性能较差，此处没用
            String logContent = StringUtils.substringAfter(log, "jdbc.sqlonly -").trim();
            if (logContent.contains("java:")) {
                // class and method
                String classMethod = StringUtils.substringBefore(logContent,"(").trim();
                sqlLog.setClassName(StringUtils.substringBeforeLast(classMethod,"."));
                sqlLog.setMethodName(StringUtils.substringAfterLast(classMethod,"."));
                // line number
                String lineNumber = StringUtils.substringBefore(log,")");
                lineNumber = StringUtils.substringAfter(lineNumber, "java:");
                sqlLog.setLineNumber(NumberUtils.toInt(lineNumber));
                // sql
                String sql = StringUtils.substringAfter(log, ")").trim();
                // 如果日志开头是 “49. select xxx”这种形式，需要把前置的数据和点去掉
                sqlLog.setSql(SqlUtil.deleteNumPrefix(sql));
            } else {
                // sql
                String sql = logContent;
                sqlLog.setSql(SqlUtil.deleteNumPrefix(sql));
            }
            // 日志量大，容易造成栈溢出，不在这里判断了，在分类出过滤下把
//                if (!SqlUtil.isSelectSql(nextLog.getSql())) {
//                    // insert update delete sql不做处理，继续读取下一条日志内容
//                    return hasNext();
//                }
            // log cost
            String cost = StringUtils.substringAfter(log, "{executed in ");
            cost = StringUtils.substringBefore(cost, " msec}");
            sqlLog.setLogCost(NumberUtils.toInt(cost.trim()));
            return sqlLog;
        }

        @Override
        protected boolean isItemEnd(String line) {
            return "".equals(line.trim());
        }
    }
}
