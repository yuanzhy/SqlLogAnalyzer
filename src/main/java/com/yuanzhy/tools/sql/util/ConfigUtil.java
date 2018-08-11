package com.yuanzhy.tools.sql.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author yuanzhy
 * @date 2018/6/13
 */
public class ConfigUtil {

    private static Logger log = LoggerFactory.getLogger(ConfigUtil.class);
    /**
     *
     */
    private static Properties props = new Properties();

    static {
        // 先从平级目录找config.properties
        InputStream in = null;
        try {
            File configFile = new File(getJarPath().concat("/config.properties"));
            if (configFile.exists()) {
                log.info("SQLAnalyzer.jar同级目录下找到config.properties，读取此配置");
                in = new FileInputStream(configFile);
            } else {
                log.info("SQLAnalyzer.jar同级目录下没有config.properties，默认读取jar包中的配置");
                in = ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties");
            }
            props.load(in);
        } catch (IOException e) {
            log.error("读取config配置文件失败", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = props.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    public static String getJarPath() {
        String path = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String result = new File(path).getParentFile().getAbsolutePath();
        try {
            return URLDecoder.decode(result, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return result;
        }
    }
}
