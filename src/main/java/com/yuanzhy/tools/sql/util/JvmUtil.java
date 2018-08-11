package com.yuanzhy.tools.sql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanzhy
 * @date 2018/8/12
 */
public final class JvmUtil {

    private static Logger log = LoggerFactory.getLogger(JvmUtil.class);
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    static {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                long max = memoryMXBean.getHeapMemoryUsage().getMax();
                long used = memoryMXBean.getHeapMemoryUsage().getUsed();
                if (max / used < 2) {
                    log.warn("jvm内存使用已超过一半");
                    heapUsedHalf = true;
                    pool.shutdownNow();
                }
            }
        };
        pool.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        log.info("jvm堆内存：{}M", memoryMXBean.getHeapMemoryUsage().getMax()/1024/1024);
    }

    /**
     * 堆是否使用了一半
     */
    private static volatile boolean heapUsedHalf = false;

    /**
     * @return
     */
    public static boolean heapUsedHalf() {
        return heapUsedHalf;
    }
}
