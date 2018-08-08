package com.yuanzhy.tools.sql.output;

import com.yuanzhy.tools.sql.model.SqlLog;

import java.util.List;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public interface IOutput {
    /**
     * 执行结果输出
     * @param sqlLogs
     */
    void doOutput(List<SqlLog> sqlLogs);
}
