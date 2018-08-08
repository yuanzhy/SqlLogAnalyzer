package com.yuanzhy.tools.sql.execute;

import com.yuanzhy.tools.sql.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public abstract class BaseExecutor implements IExecutor {

    protected static Logger log = LoggerFactory.getLogger(BaseExecutor.class);
    static {
        try {
            Class.forName(ConfigUtil.getProperty("tools.sql.driverClass"));
        } catch (ClassNotFoundException e) {
            log.error("加载JDBC驱动类失败，请检查配置项是否正确，并且引入了驱动jar包", e);
        }
    }

    protected Connection getConnection() throws SQLException {
        String jdbcUrl = ConfigUtil.getProperty("tools.sql.jdbcUrl");
        String username = ConfigUtil.getProperty("tools.sql.username");
        String password = ConfigUtil.getProperty("tools.sql.password");
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("关闭Connection出错", e);
            }
        }
    }
}
