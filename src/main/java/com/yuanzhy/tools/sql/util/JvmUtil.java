package com.yuanzhy.tools.sql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 监控超过一半的情况不可靠。。大部分情况对象是可回收的。
 *
 * @author yuanzhy
 * @date 2018/8/12
 */
@Deprecated
public final class JvmUtil {

    private static Logger log = LoggerFactory.getLogger(JvmUtil.class);
    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static long halfMaxHeapMemory = memoryMXBean.getHeapMemoryUsage().getMax() / 2;

    static {
        log.info("jvm堆内存：{}M", memoryMXBean.getHeapMemoryUsage().getMax()/1024/1024);
        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long used = memoryMXBean.getHeapMemoryUsage().getUsed();
                if (used >= halfMaxHeapMemory) {
                    log.warn("jvm内存使用已超过一半");
                    heapUsedHalf = true;
                    timer.cancel();
                    memoryMXBean = null;
                }
            }
        }, 0, 100);
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
