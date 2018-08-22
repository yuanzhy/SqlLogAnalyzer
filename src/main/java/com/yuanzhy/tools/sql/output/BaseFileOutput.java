package com.yuanzhy.tools.sql.output;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.ArgumentUtil;
import com.yuanzhy.tools.sql.common.util.ConfigUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author yuanzhy
 * @date 2018/6/16
 */
public abstract class BaseFileOutput implements IOutput {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    /** 单文件不可超过此大小 */
    protected static final long MAX_FILE_SIZE = 160 * 1024 * 1024;

    protected static final String TEMPLATE_FULL = "{order}.  cost {cost} msec\r\n" +
            "{sql}\r\n" +
            "{className}.{methodName}({classNameAfter}.java:{lineNumber})\r\n" +
//            "sql file cost {logCost} msec\r\n" +
            "sql file count {totalCount}\r\n\r\n";

    protected static final String TEMPLATE = "{order}.  cost {cost} msec\r\n" +
            "{sql}\r\n" +
//            "sql file cost {logCost} msec\r\n" +
            "sql file count {totalCount}\r\n\r\n";

    @Override
    public void doOutput(List<SqlLog> sqlLogs) {
        this.sort(sqlLogs);
        final List<SqlLog> outputList = this.getOutputList(sqlLogs);
        final String resultPath = ArgumentUtil.getString("path").concat("/result");
        log.info("============结果输出到文件");
        String filename = ArgumentUtil.getString("resultFilename");
        if (StringUtils.isEmpty(filename)) {
            filename = this.getResultFilename();
        } else if (!filename.endsWith(".txt")) {
            filename = filename + "_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".txt";
        }
        File file = new File(resultPath, filename);
        if (file.exists()) {
            file.delete();
        }
        int fileIndex = 1;
        BufferedWriter writer = null;
        try {
            file.createNewFile();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            int i = 1;
            for (SqlLog sqlLog : outputList) {
                // 只记录执行成功了sql
                if (sqlLog.isSuccess()) {
                    String logString = this.convert(sqlLog, i++);
                    log.debug("prepare output sql: {}", logString);
                    writer.write(logString);
                    if (file.length() >= MAX_FILE_SIZE) {
                        IOUtils.closeQuietly(writer);
                        if (fileIndex == 1) { // 第一个文件重命名为(1)结尾的情况
                            file.renameTo(new File(resultPath, filename.replace(".txt", "(1).txt")));
                        }
                        filename = filename.replace(".txt", "(" + ++fileIndex + ").txt");
                        file = new File(resultPath, filename);
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
                    }
                }
            }
            // print log
            log.info("============分析结果已输出到'{}'：{}", resultPath, filename);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
//        StringBuilder sb = new StringBuilder();

//        try {
//            FileUtils.writeStringToFile(new File(ConfigUtil.getJarPath().concat(filename)), sb.toString(), "UTF-8");
//        } catch (IOException e) {
//            log.error("结果输出到文件失败", e);
//        }
    }

    /**
     * sqlLog对象转换为字符串形式
     *
     * @param sqlLog sqlLog
     * @param index  当前sqlLog所在的索引值
     * @return
     */
    protected String convert(SqlLog sqlLog, int index) {
        if (sqlLog.getClassName() == null || sqlLog.getMethodName() == null) {
            return TEMPLATE.replace("{order}", String.valueOf(index))
                    .replace("{cost}", String.valueOf(sqlLog.getCost()))
                    .replace("{sql}", sqlLog.getSql())
//                            .replace("{logCost}", String.valueOf(sqlLog.getLogCost()))
                    .replace("{totalCount}", String.valueOf(sqlLog.getTotalCount()));
        } else {
            return TEMPLATE_FULL.replace("{order}", String.valueOf(index))
                    .replace("{cost}", String.valueOf(sqlLog.getCost()))
                    .replace("{sql}", sqlLog.getSql())
                    .replace("{className}", sqlLog.getClassName())
                    .replace("{methodName}", sqlLog.getMethodName())
                    .replace("{classNameAfter}", StringUtils.substringAfterLast(sqlLog.getClassName(), "."))
                    .replace("{lineNumber}", String.valueOf(sqlLog.getLineNumber()))
//                            .replace("{logCost}", String.valueOf(sqlLog.getLogCost()))
                    .replace("{totalCount}", String.valueOf(sqlLog.getTotalCount()));
        }
    }

    /**
     * 获取需要输出的结果集
     *
     * @param sqlLogs sqlLogs
     * @return
     */
    protected List<SqlLog> getOutputList(List<SqlLog> sqlLogs) {
        String topString = ConfigUtil.getProperty("tools.impl.output.top");
        int top;
        try {
            top = Integer.parseInt(topString);
        } catch (NumberFormatException e) {
            log.warn("tools.impl.output.top配置错误，当做0处理");
            top = 0;
        }
        log.info("output top is {}", top);
        if (top == 0 || top >= sqlLogs.size()) {
            return sqlLogs;
        } else {
            return sqlLogs.subList(0, top);
        }
    }

    protected abstract String getResultFilename();

    protected abstract void sort(List<SqlLog> sqlLogs);
}
