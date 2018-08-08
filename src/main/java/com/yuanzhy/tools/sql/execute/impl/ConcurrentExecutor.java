package com.yuanzhy.tools.sql.execute.impl;

import com.yuanzhy.tools.sql.execute.BaseExecutor;
import com.yuanzhy.tools.sql.execute.IExecutor;
import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.util.ConfigUtil;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 并发的将分组后的sql进行执行，提高测试效率
 * @author yuanzhy
 * @date 2018/6/13
 */
public class ConcurrentExecutor extends BaseExecutor implements IExecutor {

    private SingleThreadExecutor singleThreadExecutor = new SingleThreadExecutor();

    /**
     * 并发的在数据库里执行一下，将时间设置到sqlLog中
     * @param sqlLogs sqlLogs
     */
    @Override
    public void doExecute(List<SqlLog> sqlLogs) {
        log.info("===================开始并发执行SQL，总数：{}", sqlLogs.size());
        String sThread = ConfigUtil.getProperty("tools.impl.executor.thread");
        int nThreads = NumberUtils.toInt(sThread);
        if (nThreads == 0) {
            nThreads = Runtime.getRuntime().availableProcessors();
        }
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        final int size = sqlLogs.size();
        int num = size / nThreads;
        num = sqlLogs.size() % nThreads == 0 ? num : num + 1;
        for (int i=0; i < nThreads; i++) {
            int toIndex = i*nThreads + num;
            if (toIndex > size) {
                toIndex = size;
            }
            final List<SqlLog> segmentLogs = sqlLogs.subList(i*nThreads, toIndex);
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    singleThreadExecutor.doExecute(new ArrayList<SqlLog>(segmentLogs));
                }
            });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.DAYS);
            log.info("===================并发执行SQL完成");
        } catch (InterruptedException e) {
            log.error("线程池被唤醒, 并发执行结果不保证准确性", e);
        }
    }
}
