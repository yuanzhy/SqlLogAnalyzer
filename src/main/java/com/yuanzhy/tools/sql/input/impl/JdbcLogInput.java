package com.yuanzhy.tools.sql.input.impl;

import com.yuanzhy.tools.sql.input.BaseFolderInput;
import com.yuanzhy.tools.sql.input.IInput;
import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.util.SqlUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class JdbcLogInput extends BaseFolderInput implements IInput {


    public JdbcLogInput(String path) {
        super(path);
    }

    protected void buildInput(String path) {
        log.info("path is {}", path);
        files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("_jdbc_");
            }
        });
    }

    @Override
    public Iterator<SqlLog> iterator() {
        this.buildInput(path);
        if (ArrayUtils.isEmpty(files)) {
            log.error("没有找到jdbc日志文件");
            throw new NullPointerException("没有找到jdbc日志文件");
//            return emptyIterator();
        }
        return new jdbcLogIterator();
    }

    /**
     * jdbcLog迭代器实现
     */
    private class jdbcLogIterator extends BaseFolderIterator {

        @Override
        public boolean hasNext() {
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    // 当前文件已读完
                    if (StringUtils.isBlank(line)) {
                        this.newBufferedReader(fileIndex++);
                        // 没有下一个文件了
                        if (br == null) {
                            return false;
                        } else {
                            line = br.readLine();
                        }
                    }
                    sb.append(line);
                    // 一条记录的结束
                    if (line.contains("{executed in")) {
                        break;
                    }
                    if (line.contains("[na:")) {
                        // 是出错日志，不做处理, 继续处理下一条记录
                        return hasNext();
                    }
                }
                String sqlStr = sb.toString();
                nextLog = new SqlLog();
                // thread id 线程id叫法很多，无法确定规则，先使用中括号区分把
                String threadId = StringUtils.substringAfter(sqlStr, "] [");
                threadId = StringUtils.substringBefore(threadId,"]");
                nextLog.setThreadId(threadId);
                nextLog.setTime(StringUtils.substringBefore(sqlStr, " ["));
                // 正则性能较差，此处没用
                String logContent = StringUtils.substringAfter(sqlStr, "jdbc.sqltiming -").trim();
                if (logContent.contains("java:")) {
                    // class and method
                    String classMethod = StringUtils.substringBefore(logContent,"(").trim();
                    nextLog.setClassName(StringUtils.substringBeforeLast(classMethod,"."));
                    nextLog.setMethodName(StringUtils.substringAfterLast(classMethod,"."));
                    // line number
                    String lineNumber = StringUtils.substringBefore(sqlStr,")");
                    lineNumber = StringUtils.substringAfter(lineNumber, "java:");
                    nextLog.setLineNumber(NumberUtils.toInt(lineNumber));
                    // sql
                    String sql = StringUtils.substringAfter(sqlStr, ")");
                    sql = StringUtils.substringBefore(sql, "{executed in").trim();
                    nextLog.setSql(SqlUtil.deleteNumPrefix(sql));
                } else {
                    // sql
                    String sql = StringUtils.substringBefore(logContent, "{executed in").trim();
                    nextLog.setSql(SqlUtil.deleteNumPrefix(sql));
                }
                // 日志量大，容易造成栈溢出，不在这里判断了，在分类出过滤下把
//                if (!SqlUtil.isSelectSql(nextLog.getSql())) {
//                    // insert update delete sql不做处理，继续读取下一条日志内容
//                    return hasNext();
//                }
                // log cost
                String cost = StringUtils.substringAfter(sqlStr, "{executed in ");
                cost = StringUtils.substringBefore(cost, " msec}");
                nextLog.setLogCost(NumberUtils.toInt(cost.trim()));
                return true;
            } catch (IOException e) {
                log.error("读取日志失败：{}，切换下一个日志文件", files[fileIndex-1].getName(), e);
                // 出现异常，切换下一个文件吧
                this.newBufferedReader(fileIndex++);
                if (br == null) {
                    // 所有文件已经读完
                    return false;
                }
                return hasNext();
            }
        }

        @Override
        public SqlLog next() {
            return nextLog;
        }

        @Override
        public void remove() {
            // ignore
        }
    }
}