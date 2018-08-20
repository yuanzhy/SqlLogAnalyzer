package com.yuanzhy.tools.sql.common.model;

import com.yuanzhy.tools.sql.common.util.StorageUtil;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class SqlLog implements Serializable {
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 方法所在行数
     */
    private int lineNumber;
    /**
     * 日志中线程ID
     */
    private String threadId;
    /**
     * 日志中的执行时间（毫秒）
     */
    private int logCost;
    /**
     * 日志中该类SQL执行的总次数
     */
    private int totalCount = 1;
    /**
     * 实际执行的sql
     */
    private String sql;
    /**
     * executor执行后的花费时间（毫秒）
     */
    private int cost;
    /**
     * 是否执行成功
     */
    private boolean success = true;
    /**
     * time
     */
    private String time;
    /**
     * sql转储文件名
     */
    private String storageId;
    /**
     * 获取 类名
     *
     * @return className 类名
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * 设置 类名
     *
     * @param className 类名
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * 获取 方法名
     *
     * @return methodName 方法名
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * 设置 方法名
     *
     * @param methodName 方法名
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 获取 方法所在行数
     *
     * @return lineNumber 方法所在行数
     */
    public int getLineNumber() {
        return this.lineNumber;
    }

    /**
     * 设置 方法所在行数
     *
     * @param lineNumber 方法所在行数
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * 获取 日志中的执行时间（毫秒）
     *
     * @return logCost 日志中的执行时间（毫秒）
     */
    public int getLogCost() {
        return this.logCost;
    }

    /**
     * 设置 日志中的执行时间（毫秒）
     *
     * @param logCost 日志中的执行时间（毫秒）
     */
    public void setLogCost(int logCost) {
        this.logCost = logCost;
    }

    /**
     * 获取 实际执行的sql
     *
     * @return sql 实际执行的sql
     */
    public String getSql() {
        if (this.storageId == null) {
            return this.sql;
        } else {
            return StorageUtil.get(storageId);
        }
    }

    /**
     * 设置 实际执行的sql
     *
     * @param sql 实际执行的sql
     */
    public void setSql(String sql) {
        this.sql = sql;
        this.storageId = null;
//        if (this.storageId == null) {
//            this.sql = sql;
//        } else {
//            throw new RuntimeException("sql已缓存在磁盘中，不可以赋值");
//        }
    }

    /**
     * 获取 executor执行后的花费时间（毫秒）
     *
     * @return cost executor执行后的花费时间（毫秒）
     */
    public int getCost() {
        return this.cost;
    }

    /**
     * 设置 executor执行后的花费时间（毫秒）
     *
     * @param cost executor执行后的花费时间（毫秒）
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * 获取 是否执行成功
     *
     * @return success 是否执行成功
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * 设置 是否执行成功
     *
     * @param success 是否执行成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取 日志中该类SQL执行的总次数
     *
     * @return totalCount 日志中该类SQL执行的总次数
     */
    public int getTotalCount() {
        return this.totalCount;
    }

    /**
     * 设置 日志中该类SQL执行的总次数
     *
     * @param totalCount 日志中该类SQL执行的总次数
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 获取 日志中线程ID
     *
     * @return threadId 日志中线程ID
     */
    public String getThreadId() {
        return StringUtils.trimToEmpty(this.threadId);
    }

    /**
     * 设置 日志中线程ID
     *
     * @param threadId 日志中线程ID
     */
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    /**
     * 获取 time
     *
     * @return time time
     */
    public String getTime() {
        return this.time;
    }

    /**
     * 设置 time
     *
     * @param time time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     *  为了防止内存溢出，将大SQL做一下MD5存储到文件缓存目录中
     *  执行和输出的时候在读出来
     */
    public void storeSql() {
        this.storageId = StorageUtil.store(this.sql);
        this.sql = null;
    }
}
