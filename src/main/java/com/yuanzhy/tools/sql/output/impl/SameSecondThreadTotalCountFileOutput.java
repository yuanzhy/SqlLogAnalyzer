package com.yuanzhy.tools.sql.output.impl;

import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.output.BaseFileOutput;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author yuanzhy
 * @Date 2018/8/8
 */
public class SameSecondThreadTotalCountFileOutput extends BaseFileOutput {

    protected static final String LIANGGE_TEMPLATE = "{order}.\r\n" +
            "{time} [{threadId}] {className}.{methodName}({classNameAfter}.java:{lineNumber})\r\n" +
            "{sql}\r\n" +
            "- every second sql count {totalCount}\r\n\r\n";

    @Override
    protected String getResultFilename(String suffix) {
        return "/result/everySecondThreadDesc_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + suffix + ".txt";
    }

    @Override
    protected void prepare(List<SqlLog> sqlLogs) {
        super.prepare(sqlLogs);
        log.info("============每秒同一线程同类sql执行总数倒叙排序");
        Collections.sort(sqlLogs, new TotalCountComparator());
    }

    @Override
    protected String convert(SqlLog sqlLog, int index) {
        return LIANGGE_TEMPLATE.replace("{order}", String.valueOf(index))
                .replace("{time}", sqlLog.getTime())
                .replace("{threadId}", sqlLog.getThreadId())
                .replace("{className}", sqlLog.getClassName())
                .replace("{methodName}", sqlLog.getMethodName())
                .replace("{classNameAfter}", StringUtils.substringAfterLast(sqlLog.getClassName(), "."))
                .replace("{lineNumber}", String.valueOf(sqlLog.getLineNumber()))
                .replace("{sql}", sqlLog.getSql())
                .replace("{totalCount}", String.valueOf(sqlLog.getTotalCount()));
    }
}
