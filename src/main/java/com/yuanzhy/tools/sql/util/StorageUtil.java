package com.yuanzhy.tools.sql.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author yuanzhy
 * @date 2018/8/11
 */
public final class StorageUtil {

    private static final String STORE_PATH;

    static {
        String path = ArgumentUtil.getArgument("path");
        if (StringUtils.isEmpty(path)) {
            path = System.getProperty("java.io.tmpdir");
        }
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String processId = runtimeMXBean.getName().split("@")[0];
        STORE_PATH = path + "result/temp/" + processId;
    }

    /**
     *
     * @param source source
     * @return filename
     */
    public static String store(String source) {
        String filename = DigestUtils.md5Hex(source);
        File file = new File(STORE_PATH, filename);
        if (file.exists()) {
            return filename;
        }
        try {
            FileUtils.writeStringToFile(file, source, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filename;
    }

    /**
     *
     * @param filename filename
     * @return
     */
    public static String get(String filename) {
        File file = new File(STORE_PATH, filename);
        if (file.exists()) {
            try {
                return FileUtils.readFileToString(file, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

}
