package com.yuanzhy.tools.sql.input;

import com.yuanzhy.tools.sql.common.model.SqlLog;

import java.util.Iterator;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public interface IInput {
    /**
     * 获取sql日志的迭代器
     * @return
     */
    Iterator<SqlLog> iterator();

}
