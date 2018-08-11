package com.yuanzhy.tools.sql.classify.impl;

import com.yuanzhy.tools.sql.model.SqlLog;

/**
 * 每秒同一线程同类SQL  分组
 *
 * @Author yuanzhy
 * @Date 2018/8/8
 */
public class EverySecondThreadStructureClassifier extends StructureClassifier {

    @Override
    protected String getClassifyKey(SqlLog sqlLog) {
        return super.getClassifyKey(sqlLog) + "_" + sqlLog.getTime() + "_" + sqlLog.getThreadId();
    }
}
