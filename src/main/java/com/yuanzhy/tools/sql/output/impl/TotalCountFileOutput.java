package com.yuanzhy.tools.sql.output.impl;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.output.BaseFileOutput;
import com.yuanzhy.tools.sql.output.IOutput;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class TotalCountFileOutput extends BaseFileOutput implements IOutput {

    @Override
    protected String getResultFilename() {
        return "countDesc_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".txt";
    }

    @Override
    protected void sort(List<SqlLog> sqlLogs) {
        log.info("============日志同类sql总数倒序排序");
        Collections.sort(sqlLogs, new TotalCountComparator());
    }

    /**
     * SQL执行时间倒序排列比较器
     */
    private static class TotalCountComparator implements Comparator<SqlLog> {

        @Override
        public int compare(SqlLog o1, SqlLog o2) {
            if (o1.getTotalCount() == o2.getTotalCount()) {
                return 0;
            }
            return (o1.getTotalCount() > o2.getTotalCount()) ? -1 : 1;
        }
    }

}
