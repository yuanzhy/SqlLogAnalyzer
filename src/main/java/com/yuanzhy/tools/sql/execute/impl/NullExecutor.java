package com.yuanzhy.tools.sql.execute.impl;

import com.yuanzhy.tools.sql.execute.BaseExecutor;
import com.yuanzhy.tools.sql.execute.IExecutor;
import com.yuanzhy.tools.sql.common.model.SqlLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author yuanzhy
 * @Date 2018/8/8
 */
public class NullExecutor implements IExecutor {

    protected static Logger log = LoggerFactory.getLogger(BaseExecutor.class);

    @Override
    public void doExecute(List<SqlLog> sqlLogs) {
        log.info("NullExecutor");
    }
}
