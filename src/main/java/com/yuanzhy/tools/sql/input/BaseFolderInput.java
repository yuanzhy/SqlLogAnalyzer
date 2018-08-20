package com.yuanzhy.tools.sql.input;

import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.MemoUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public abstract class BaseFolderInput implements IInput {


    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected String path;

    /**
     * 文件过大，所以采用流的方式一行行读取解析
     * hasNext会提前解析完一条日志记录，并缓存在nextLog
     * next会将此结果返回
     */
    protected File[] files;

    public BaseFolderInput(String path) {
        this.path = path;
    }
    @Deprecated
    private static final EmptyIterator<Object> INSTANCE = new EmptyIterator<Object>();
    @Deprecated
    protected <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) INSTANCE;
    }

    /**
     *
     * @param <E>
     */
    @Deprecated
    private static class EmptyIterator<E> implements Iterator<E> {
        public boolean hasNext() { return false; }
        public E next() { throw new NoSuchElementException(); }
        public void remove() { throw new IllegalStateException(); }
    }

    protected abstract class BaseFolderIterator implements Iterator<SqlLog> {

        protected SqlLog nextLog;

        protected int fileIndex = -1;

        protected BufferedReader br;

        public BaseFolderIterator() {
//            Integer memoFileIndex = MemoUtil.getMemo("fileIndex");
//            if (memoFileIndex != null) {
//                fileIndex = memoFileIndex.intValue() - 1;
//                // print log
//                if (log.isInfoEnabled() && fileIndex >= 0) {
//                    log.info("=======================================");
//                    for (int i=0; i <= fileIndex; i++) {
//                        log.info(files[i].getName());
//                    }
//                    log.info("=======================================");
//                    log.info("检测到之前已分析完以上文件，将继续分析剩余文件");
//                }
//            }
            newBufferedReader();
        }

        protected void newBufferedReader() {
            IOUtils.closeQuietly(br);
            fileIndex++;
//            MemoUtil.saveMemo("fileIndex", fileIndex);
            if (files.length < fileIndex+1) {
                br = null;
                log.info("文件已全部读取完成");
//                MemoUtil.clearMemo();
                return;
            }
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(files[fileIndex]), "GBK"));
                log.info("开始读取文件: {}", files[fileIndex].getName());
            } catch (Exception e) {
                log.error("创建bufferedReader失败", e);
                IOUtils.closeQuietly(br);
                br = null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("不支持的操作");
        }

        /**
         * finalize这个实现不太好，以防万一，兜底用一下吧
         * @throws Throwable
         */
        @Override
        protected void finalize() throws Throwable {
            IOUtils.closeQuietly(br);
            super.finalize();
        }
    }
}
