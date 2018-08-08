package com.yuanzhy.tools.sql.execute;

import com.yuanzhy.tools.sql.execute.impl.SingleThreadExecutor;
import com.yuanzhy.tools.sql.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class ExecutorFactory {

    protected static Logger log = LoggerFactory.getLogger(ExecutorFactory.class);

    public static IExecutor newInstance() {
        String className = ConfigUtil.getProperty("tools.impl.executor");
        try {
            return (IExecutor) Class.forName(className).newInstance();
        } catch (Exception e) {
            log.error("创建executor失败，使用SingleThreadExecutor");
            return new SingleThreadExecutor();
        }
    }
}
