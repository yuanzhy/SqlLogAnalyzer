package com.yuanzhy.tools.sql.output.impl;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.output.IOutput;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author yuanzhy
 * @Date 2018/8/8
 */
public class SameSecondThreadTotalCountFileOutput extends TotalCountFileOutput implements IOutput {

    protected static final String SST_TEMPLATE = "{order}.\r\n" +
            "{time} [{threadId}] {className}.{methodName}({classNameAfter}.java:{lineNumber})\r\n" +
            "{sql}\r\n" +
            "- same second thread sql count {totalCount}\r\n\r\n";

    @Override
    protected String getResultFilename() {
        return "sameSecondThreadDesc_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".txt";
    }

    @Override
    protected String convert(SqlLog sqlLog, int index) {
        return SST_TEMPLATE.replace("{order}", String.valueOf(index))
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
