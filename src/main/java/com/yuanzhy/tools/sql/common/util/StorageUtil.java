package com.yuanzhy.tools.sql.common.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yuanzhy
 * @date 2018/8/11
 */
public final class StorageUtil {

    private static Logger log = LoggerFactory.getLogger(StorageUtil.class);

    private static final String TEMP_PATH;
    /**
     *
     * key=storageId
     * value=position  文本在文件中的position
     */
    private static ConcurrentMap<String, Long> positionMap = new ConcurrentHashMap<String, Long>();
    /**
     * key=filename
     * value=Writer 缓存writer，避免频繁创建文件流耗费时间
     */
    private static ConcurrentMap<String, Writer> writerMap = new ConcurrentHashMap<String, Writer>();

    static {
        String path = ArgumentUtil.getArgument("path");
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path不能为空，请先调用ArgumentUtil.parseArgs()解析参数");
        }
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String processId = runtimeMXBean.getName().split("@")[0];
        TEMP_PATH = path + "result/temp/" + processId;
        new File(TEMP_PATH).mkdirs();
        // 注册清理钩子
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                log.info("程序退出，清理临时文件");
                try {
                    for (Writer writer : writerMap.values()) {
                        IOUtils.closeQuietly(writer);
                    }
                    FileUtils.deleteQuietly(new File(TEMP_PATH));
                    log.info("清理临时文件成功");
                } catch (Exception e) {
                    log.error("清理临时文件失败,请手动删除：{}", TEMP_PATH, e);
                }
            }
        });
    }

    /**
     *
     * @param source source
     * @return storageId 存储标识
     */
    public static String store(String source) {
        if (source.contains("\n")) {
            throw new IllegalArgumentException("不能包含换行符");
        }
        String storageId = DigestUtils.md5Hex(source);
        if (positionMap.containsKey(storageId)) {
            return storageId;
        }
        File file = getFile(storageId);
        synchronized (StorageUtil.class) { // 锁后续在优化，先实现功能
            if (positionMap.containsKey(storageId)) {
                return storageId;
            }
            positionMap.put(storageId, file.length());
            try {
                Writer writer = writerMap.get(file.getName());
                if (writer == null) {
                    writer = new BufferedWriter(new FileWriter(file, true));
                    writerMap.put(file.getName(), writer);
                }
                writer.write(source + "\r\n");
                writer.flush();
                // 流不关闭，避免频繁创建耗费大量时间，会在程序退出的钩子里关闭
//                FileUtils.writeStringToFile(file, source + "\r\n", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return storageId;
    }

    /**
     *
     * @param storageId 存储标识
     * @return
     */
    public static String get(String storageId) {
        File file = getFile(storageId);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            raf.seek(positionMap.get(storageId));
            return raf.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(raf);
        }
    }

    private static File getFile(String storageId) {
        File file = new File(TEMP_PATH, storageId.substring(0, 1));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("创建缓存文件失败", e);
            }
        }
        return file;
    }

    public static String getTempPath() {
        return TEMP_PATH;
    }


    public static void asyncDeleteFile(final File file) {
        new Thread() {
            @Override
            public void run() {
                log.info("开始异步清理临时缓存文件");
                try {
                    FileUtils.deleteQuietly(file);
                    log.info("异步清理临时缓存文件成功");
                } catch (Exception e) {
                    log.error("异步清理临时缓存文件失败,请手动删除：{}", file.getAbsolutePath(), e);
                }
            }
        }.start();
    }
}
