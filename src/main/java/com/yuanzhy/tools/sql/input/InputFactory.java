package com.yuanzhy.tools.sql.input;

import com.yuanzhy.tools.sql.common.util.ConfigUtil;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class InputFactory {


    public static IInput newInstance(String path) {
        String className = ConfigUtil.getProperty("tools.impl.input");
        try {
            return (IInput) Class.forName(className).
                    getDeclaredConstructor(String.class).newInstance(path);
        } catch (Exception e) {
            throw new RuntimeException("创建input对象失败：" + className, e);
        }
    }
}
