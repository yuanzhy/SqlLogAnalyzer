package com.yuanzhy.tools.sql.input;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.ArgumentUtil;
import com.yuanzhy.tools.sql.common.util.DateUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public abstract class BaseFolderInput implements IInput {


    protected Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 文件过大，所以采用流的方式一行行读取解析
     * hasNext会提前解析完一条日志记录，并缓存在nextLog
     * next会将此结果返回
     */
    protected File[] files;

    public BaseFolderInput(String path) {
        log.info("path is {}", path);
        files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return acceptFile(name);
            }
        });
    }
    @Override
    public final Iterator<SqlLog> iterator() {
        if (ArrayUtils.isEmpty(files)) {
            throw new NullPointerException("没有找到sqlOnly日志文件");
        }
        return this.iterator0();
    }
    /**
     * 是否处理该文件
     * @param filename filename
     * @return
     */
    protected abstract boolean acceptFile(String filename);
    /**
     * 返回具体的迭代器实现
     * @return
     */
    protected abstract Iterator<SqlLog> iterator0();

    /**
     *
     */
    protected abstract class BaseFolderIterator implements Iterator<SqlLog> {

        private final SqlLog emptyLog = new SqlLog();

        private SqlLog nextLog;
        private BufferedReader br;
        protected int fileIndex = -1;

        public BaseFolderIterator() {
            nextFileReader();
        }

        private void nextFileReader() {
            IOUtils.closeQuietly(br);
            br = null;
            fileIndex++;
            if (files.length < fileIndex + 1) {
                log.info("文件已全部读取完成");
                return;
            }
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(files[fileIndex]), "GBK"));
                log.info("开始读取文件: {}", files[fileIndex].getName());
            } catch (Exception e) {
                IOUtils.closeQuietly(br);
                log.error("创建bufferedReader失败", e);
            }
        }

        @Override
        public final boolean hasNext() {
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        // 当前文件已读完
                        this.nextFileReader();
                        // 没有下一个文件了
                        if (br == null) {
                            return false;
                        } else {
                            line = br.readLine();
                        }
                    }
                    sb.append(line);
                    if (this.isItemEnd(line)) {
                        // 一条记录的结束
                        break;
                    } else if (this.isItemError(line)) {
                        // 是出错日志，不做处理, 继续处理下一条记录
                        return hasNext();
                    }
                }
                if (sb.length() < 19) { // 小于时间的长度，记录肯定是无效的
                    return hasNext();
                }
                // 判断时间区间是否符合要求
                Date sqlDate = DateUtil.parse(sb.substring(0, 19));
                Date startTime = ArgumentUtil.getDate("startTime");
                Date endTime = ArgumentUtil.getDate("endTime");
                if (DateUtil.datetimeInRange(sqlDate, startTime, endTime)) { // 日期时间都符合要求
                    this.nextLog = this.parseLog(sb.toString());
                    return true;
                }
                // 日期时间不符合要求的情况
                if (DateUtil.dayInRangeBoundary(sqlDate, startTime, endTime)) {
                    // 年月日卡在区间的边缘，说明本条记录不符合要求，但文件内可能会有满足要求的情况
//                    log.debug("文件满足分析要求，但本条记录不符合: {}", sb.substring(0, 19));
                    this.nextLog = emptyLog;// 创建一个无效记录，没使用hasNext递归是因为日志量大会导致StackOverflow
                    return true;
                }
                // 日期时间不符合，年月日也不在边缘，此文件肯定没有符合的记录，直接切换文件
                log.info("文件不符合时间要求，不做处理：{}", files[fileIndex].getName());
                this.nextFileReader();
                if (br == null) {
                    // 所有文件已经读完
                    return false;
                }
                return hasNext();
            } catch (IOException e) {
                log.error("读取日志失败：{}，切换下一个日志文件", files[fileIndex].getName(), e);
                // 出现异常，切换下一个文件吧
                this.nextFileReader();
                if (br == null) {
                    // 所有文件已经读完
                    return false;
                }
                return hasNext();
            }
        }

        /**
         * 解析日志
         *
         * @param log 一条日志记录
         * @return SqlLog对象
         */
        protected abstract SqlLog parseLog(String log);

        /**
         * 是否是一条记录的结尾
         *
         * @param line 日志中的一行字符串
         * @return
         */
        protected abstract boolean isItemEnd(String line);

        /**
         * 是否是错误记录
         *
         * @param line 日志中的一行字符串
         * @return
         */
        protected boolean isItemError(String line) {
            return line.contains("[na:") || line.contains("Exception");
        }

        @Override
        public final SqlLog next() {
            if (files.length < fileIndex + 1) {
                throw new NoSuchElementException();
            }
            if (this.nextLog == null) {
                throw new IllegalStateException("请先调用hasNext");
            }
            SqlLog sqlLog = this.nextLog;
            this.nextLog = null;
            return sqlLog;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException("不支持的操作");
        }

    }
}
