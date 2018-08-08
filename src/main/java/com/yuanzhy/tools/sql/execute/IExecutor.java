package com.yuanzhy.tools.sql.execute;

import com.yuanzhy.tools.sql.model.SqlLog;

import java.util.List;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public interface IExecutor {
    /**
     * 在数据库里执行一下，将时间设置到sqlLog中
     * @param sqlLogs sqlLogs
     */
    void doExecute(List<SqlLog> sqlLogs);
}
