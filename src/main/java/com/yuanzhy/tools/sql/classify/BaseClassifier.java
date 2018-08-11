package com.yuanzhy.tools.sql.classify;

import com.yuanzhy.tools.sql.input.IInput;
import com.yuanzhy.tools.sql.model.SqlLog;
import com.yuanzhy.tools.sql.util.SqlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public abstract class BaseClassifier implements IClassifier {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<SqlLog> doClassify(IInput input) {
        Map<String, SqlLog> tmpMap = new HashMap<String, SqlLog>();
        List<SqlLog> result = new ArrayList<SqlLog>();
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
            sqlLog.storeSql();
            tmpMap.put(classifyKey, sqlLog);
            result.add(sqlLog);
        }
        return result;
    }

    protected abstract String getClassifyKey(SqlLog sqlLog);
}
