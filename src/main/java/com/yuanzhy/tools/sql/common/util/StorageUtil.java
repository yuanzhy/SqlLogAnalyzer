package com.yuanzhy.tools.sql.common.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yuanzhy
 * @date 2018/8/11
 */
public final class StorageUtil {

    private static final String TEMP_PATH;
    /** 缓存已有文件名称，提升判断文件是否存在的性能 */
    private static final Set<String> EXISTS_FILENAME = new HashSet<String>();

    static {
        String path = ArgumentUtil.getArgument("path");
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path不能为空，请先调用ArgumentUtil.parseArgs()解析参数");
        }
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
//        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
//        String processId = runtimeMXBean.getName().split("@")[0];
//        STORE_PATH = path + "result/temp/" + processId;
        TEMP_PATH = path + "result/temp/";
    }

    /**
     *
     * @param source source
     * @return filename
     */
    public static String store(String source) {
        String filename = DigestUtils.md5Hex(source);
        if (EXISTS_FILENAME.contains(filename)) {
            return filename;
        }
        File file = new File(getSubFolder(filename), filename);
        if (!file.exists()) {
            try {
                FileUtils.writeStringToFile(file, source, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        EXISTS_FILENAME.add(filename);
        return filename;
    }

    /**
     *
     * @param filename filename
     * @return
     */
    public static String get(String filename) {
        File file = new File(getSubFolder(filename), filename);
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

    private static String getSubFolder(String filename) {
        return TEMP_PATH + "/" + filename.substring(0, 2) + "/" + filename.substring(0, 5);
    }

    /**
     *
     */
    public static void clearTemp() {
        try {
            FileUtils.deleteDirectory(new File(TEMP_PATH));
        } catch (IOException e) {
            throw new RuntimeException("删除临时目录失败", e);
        }
    }

    public static String getTempPath() {
        return TEMP_PATH;
    }


    public static void asyncDeleteFile(final File file) {
        new Thread() {
            @Override
            public void run() {
                FileUtils.deleteQuietly(file);
            }
        }.start();
    }
}
