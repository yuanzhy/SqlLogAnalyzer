package com.yuanzhy.tools.sql.classify;

import com.yuanzhy.tools.sql.classify.impl.StructureClassifier;
import com.yuanzhy.tools.sql.common.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class ClassifierFactory {

    protected static Logger log = LoggerFactory.getLogger(ClassifierFactory.class);

    public static IClassifier newInstance() {
        String className = ConfigUtil.getProperty("tools.impl.classifier");
        try {
            return (IClassifier) Class.forName(className).newInstance();
        } catch (Exception e) {
            log.error("创建classifier失败，使用StructureClassifier", e);
            return new StructureClassifier();
        }
    }

}
