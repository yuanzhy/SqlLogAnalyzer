package com.yuanzhy.tools.sql.output;

import com.yuanzhy.tools.sql.common.util.ConfigUtil;
import com.yuanzhy.tools.sql.output.impl.MultiOutput;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class OutputFactory {

    public static IOutput newInstance() {
        String className = ConfigUtil.getProperty("tools.impl.output");
        try {
            IOutput output = (IOutput) Class.forName(className).newInstance();
            if (output instanceof MultiOutput) {
                String[] implClassNameArr = ConfigUtil.getProperty("tools.impl.output.multi").split(",");
                for (String implClassName : implClassNameArr) {
                    ((MultiOutput)output).addOutput((IOutput) Class.forName(implClassName).newInstance());
                }
            }
            return output;
        } catch (Exception e) {
            throw new RuntimeException("创建output失败：" + className, e);
        }
    }
}
