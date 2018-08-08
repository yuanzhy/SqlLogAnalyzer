package com.yuanzhy.tools.sql.execute.impl;

import com.yuanzhy.tools.sql.execute.BaseExecutor;
import com.yuanzhy.tools.sql.execute.IExecutor;
import com.yuanzhy.tools.sql.model.SqlLog;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 单线程顺序的执行分组后的sql
 *
 * @author yuanzhy
 * @date 2018/6/13
 */
public class SingleThreadExecutor extends BaseExecutor implements IExecutor {

    @Override
    public void doExecute(List<SqlLog> sqlLogs) {
        log.info("======================开始执行SQL，总数：{}", sqlLogs.size());
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stat = conn.createStatement();
            int errorCount = 0;
            for (int i=0; i < sqlLogs.size(); i++) {
                SqlLog sqlLog = sqlLogs.get(i);
                long t1 = System.currentTimeMillis();
                try {
                    stat.execute(sqlLog.getSql());
                    long t2 = System.currentTimeMillis();
                    sqlLog.setCost((int) (t2-t1));
//                    log.info("==============执行成功");
                } catch (Exception e) {
                    log.error("sql执行失败：{}", sqlLog.getSql(), e);
                    sqlLog.setSuccess(false);
                    errorCount++;
                }
            }
            log.info("======================执行完成，成功: {}, 失败：{}", (sqlLogs.size()-errorCount), errorCount);
            stat.close();
        } catch (SQLException e) {
            log.error("doExecute执行失败", e);
        } finally {
            closeConnection(conn);
        }
    }
}
