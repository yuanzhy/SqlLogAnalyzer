package com.yuanzhy.tools.sql.output.impl;

import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.output.BaseFileOutput;
import com.yuanzhy.tools.sql.output.IOutput;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author yuanzhy
 * @date 2018/6/16
 */
public class SqlTimingFileOutput extends BaseFileOutput implements IOutput {
    @Override
    protected String getResultFilename(String suffix) {
        return "/result/timeDesc_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + suffix + ".txt";
    }

    @Override
    protected void sort(List<SqlLog> sqlLogs) {
        log.info("============执行时间倒叙排序");
        Collections.sort(sqlLogs, new SqlTimingComparator());
    }

    /**
     * SQL执行时间倒叙排列比较器
     */
    private static class SqlTimingComparator implements Comparator<SqlLog> {

        @Override
        public int compare(SqlLog o1, SqlLog o2) {
            if (o1.getCost() == o2.getCost()) {
                return 0;
            }
            return (o1.getCost() > o2.getCost()) ? -1 : 1;
        }
    }
}
