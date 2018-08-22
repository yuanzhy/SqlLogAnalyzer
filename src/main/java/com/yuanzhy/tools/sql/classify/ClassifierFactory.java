package com.yuanzhy.tools.sql.classify;

import com.yuanzhy.tools.sql.common.util.ConfigUtil;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class ClassifierFactory {

    public static IClassifier newInstance() {
        String className = ConfigUtil.getProperty("tools.impl.classifier");
        try {
            return (IClassifier) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建classifier失败" + className, e);
        }
    }

}
