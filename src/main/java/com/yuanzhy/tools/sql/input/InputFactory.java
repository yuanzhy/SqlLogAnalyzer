package com.yuanzhy.tools.sql.input;

import com.yuanzhy.tools.sql.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class InputFactory {

    private static Logger log = LoggerFactory.getLogger(InputFactory.class);

    public static IInput newInstance(String path) {
        String className = ConfigUtil.getProperty("tools.impl.input");
        try {
            return (IInput) Class.forName(className).
                    getDeclaredConstructor(String.class).newInstance(path);
        } catch (Exception e) {
            throw new RuntimeException("创建input对象失败", e);
        }
    }
}
