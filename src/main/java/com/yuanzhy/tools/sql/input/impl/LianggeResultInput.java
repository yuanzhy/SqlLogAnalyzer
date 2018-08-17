package com.yuanzhy.tools.sql.input.impl;

import com.yuanzhy.tools.sql.input.BaseFolderInput;
import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.SqlUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;

/**
 * 良哥的sqlOnly分析结果做为输入
 * 分析同一秒 同一类SQL执行的数量，倒序排一下
 *
 * @Author yuanzhy
 * @Date 2018/8/8
 */
public class LianggeResultInput extends BaseFolderInput {

    public LianggeResultInput(String path) {
        super(path);
    }

    protected void buildInput(String path) {
        log.info("path is {}", path);
        files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("[") && name.endsWith(".txt");
            }
        });
//        if (ff != null) {
//            for (File f : ff) {
//                if (f.isDirectory()) {
//                }
//            }
//        }
    }

    @Override
    public Iterator<SqlLog> iterator() {
        this.buildInput(path);
        if (ArrayUtils.isEmpty(files)) {
            throw new NullPointerException("没有找到LianggeResult日志文件");
        }
        return new LianggeResultIterator();
    }

    /**
     * Liangge迭代器实现
     */
    private class LianggeResultIterator extends BaseFolderIterator {

        @Override
        public boolean hasNext() {
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        // 当前文件已读完
                        this.newBufferedReader();
                        // 没有下一个文件了
                        if (br == null) {
                            return false;
                        } else {
                            line = br.readLine();
                        }
                    } else if ("".equals(line.trim())) {
                        // 一条记录的结束
                        break;
                    }
                    sb.append(line);
                }
                String filename = files[fileIndex-1].getName();
                String sqlStr = sb.toString();
                nextLog = new SqlLog();
                String threadId = StringUtils.substringBetween(filename, "[", "]");
                if (threadId == null) {
                    threadId = StringUtils.substringBetween(filename, "[", ".txt");
                }
                nextLog.setThreadId(threadId);
                nextLog.setTime(StringUtils.substringBefore(sqlStr, " ["));
                // 正则性能较差，此处没用
                String logContent = StringUtils.substringAfter(sqlStr, "jdbc.sqlonly -").trim();
                if (logContent.contains("java:")) {
                    // class and  method
                    String classMethod = StringUtils.substringBefore(logContent,"(").trim();
                    nextLog.setClassName(StringUtils.substringBeforeLast(classMethod,"."));
                    nextLog.setMethodName(StringUtils.substringAfterLast(classMethod,"."));
                    // line number
                    String lineNumber = StringUtils.substringBefore(sqlStr,")");
                    lineNumber = StringUtils.substringAfter(lineNumber, "java:");
                    nextLog.setLineNumber(NumberUtils.toInt(lineNumber));
                    // sql
                    String sql = StringUtils.substringAfter(sqlStr, ")").trim();
                    // 如果日志开头是 “59. select xxx”这种形式，需要把前置的数据和点去掉
                    nextLog.setSql(SqlUtil.deleteNumPrefix(sql));
                } else {
                    // sql
                    nextLog.setSql(SqlUtil.deleteNumPrefix(logContent));
                }
                return true;
            } catch (IOException e) {
                log.error("读取日志失败：{}，切换下一个日志文件", files[fileIndex].getName(), e);
                // 出现异常，切换下一个文件吧
                this.newBufferedReader();
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

    }
}
