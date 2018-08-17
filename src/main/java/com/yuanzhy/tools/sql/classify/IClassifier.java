package com.yuanzhy.tools.sql.classify;

import com.yuanzhy.tools.sql.input.IInput;
import com.yuanzhy.tools.sql.common.model.SqlLog;

import java.util.List;

/**
 * SQL分类器
 *
 * @author yuanzhy
 * @date 2018/6/13
 */
public interface IClassifier {
    /**
     * 做分类
     * @return 分类后的sqlLogs集合，一类SQL只存储一条
     * @param input input
     */
    List<SqlLog> doClassify(IInput input);
}
