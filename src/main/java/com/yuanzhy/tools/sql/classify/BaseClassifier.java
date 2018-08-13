package com.yuanzhy.tools.sql.classify;

import com.yuanzhy.tools.sql.input.IInput;
import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.util.ConfigUtil;
import com.yuanzhy.tools.sql.util.JvmUtil;
import com.yuanzhy.tools.sql.util.SqlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public abstract class BaseClassifier implements IClassifier {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final boolean ENABLE_DISK_CACHE = Boolean.parseBoolean(ConfigUtil.getProperty("tools.impl.enableDiskCache"));

    @Override
    public List<SqlLog> doClassify(IInput input) {
        Map<String, SqlLog> tmpMap = new HashMap<String, SqlLog>();
        List<SqlLog> result = new LinkedList<SqlLog>();
        Iterator<SqlLog> ite = input.iterator();
        while (ite.hasNext()) {
            SqlLog sqlLog = ite.next();
            if (!SqlUtil.isSelectSql(sqlLog.getSql())) {
                continue;
            }
            String classifyKey = this.getClassifyKey(sqlLog);
            if (tmpMap.containsKey(classifyKey)) {
                // 已经包含同类sql，计个数
                SqlLog existsLog = tmpMap.get(classifyKey);
                existsLog.setTotalCount(existsLog.getTotalCount()+1);
                continue;
            }
            tmpMap.put(classifyKey, sqlLog);
            result.add(sqlLog);
            if (ENABLE_DISK_CACHE && JvmUtil.heapUsedHalf()) {
                sqlLog.storeSql();
            }
        }
        return result;
    }

    protected abstract String getClassifyKey(SqlLog sqlLog);
}
