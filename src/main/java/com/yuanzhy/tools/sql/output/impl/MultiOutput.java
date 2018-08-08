package com.yuanzhy.tools.sql.output.impl;

import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.output.IOutput;
import com.yuanzhy.tools.sql.util.ConfigUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 多重的输出
 * @author yuanzhy
 * @date 2018/6/16
 */
public class MultiOutput implements IOutput {

    private static Logger log = LoggerFactory.getLogger(MultiOutput.class);

    private List<IOutput> outputs = new ArrayList<IOutput>();

    public void addOutput(IOutput output) {
        outputs.add(output);
    }

    @Override
    public void doOutput(final List<SqlLog> sqlLogs) {
        log.info("开始并发输出结果");
        String sThread = ConfigUtil.getProperty("tools.impl.output.multi.thread");
        int nThreads = NumberUtils.toInt(sThread);
        if (nThreads == 0) {
            nThreads = Runtime.getRuntime().availableProcessors();
        }
        if (outputs.size() <= nThreads) {
            nThreads = outputs.size();
        }
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (final IOutput output : outputs) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    // 复制一份arrayList，防止实现操作了原list导致出现问题
                    output.doOutput(new ArrayList<SqlLog>(sqlLogs));
                }
            });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.DAYS);
            log.info("===================并发输出结果完成");
        } catch (InterruptedException e) {
            log.error("线程池被唤醒, 并发输出结果不保证准确性", e);
        }
    }
}
