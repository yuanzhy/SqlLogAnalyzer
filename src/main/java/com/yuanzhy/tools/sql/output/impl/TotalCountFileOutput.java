package com.yuanzhy.tools.sql.output.impl;

import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.output.BaseFileOutput;
import com.yuanzhy.tools.sql.output.IOutput;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class TotalCountFileOutput extends BaseFileOutput implements IOutput {

    @Override
    protected String getResultFilename(String suffix) {
        return "/result/countDesc_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + suffix + ".txt";
    }

    @Override
    protected void prepare(List<SqlLog> sqlLogs) {
        super.prepare(sqlLogs);
        log.info("============日志同类sql总数倒叙排序");
        Collections.sort(sqlLogs, new TotalCountComparator());
    }


}
