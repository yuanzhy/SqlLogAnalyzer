package com.yuanzhy.tools.sql.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author yuanzhy
 * @Date 2018/8/14
 */
/**
 * 逻辑没问题，小碎文件多，性能太差 弃用
 */
@Deprecated
public final class MemoUtil {

    private static Logger log = LoggerFactory.getLogger(MemoUtil.class);
    /**
     * 缓存中间结果
     * key=className
     * value=map
     */
    private static ConcurrentMap<String, Map<String, Object>> map;

    static {
//        register();
    }

    public static <T> T getMemo(String key) {
        checkState();
        return (T) getClassMap(getCallClassName()).get(key);
    }

    public static <T> void saveMemo(String key, T object) {
        checkState();
        getClassMap(getCallClassName()).put(key, object);
    }

    public static void deleteMemo(String key) {
        checkState();
        getClassMap(getCallClassName()).remove(key);
    }

    public static void clearMemo() {
        checkState();
        String className = getCallClassName();
        map.remove(className);
    }

    private static String getCallClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement ele = stackTrace[i];
            if (!ele.getClassName().equals(MemoUtil.class.getName())) {
                return ele.getClassName();
            }
        }
        throw new RuntimeException("非法的调用");
    }


    private static Map<String, Object> getClassMap(String className) {
        Map<String, Object> m = map.get(className);
        if (m == null) {
            m = new HashMap<String, Object>();
            map.put(className, m);
        }
        return m;
    }

    private static void checkState() {
        if (map == null) {
            throw new IllegalStateException("没有注册Memo");
        }
    }

    /**
     * 注册备忘录，程序会在异常结束时将中间结果缓存在disk
     * 如存在缓存结果，也将会还原结果
     */
    public static void register() {
        if (map != null) {
            log.warn("不可重复注册备忘录");
            return;
        }
        final String tempPath = StorageUtil.getTempPath().concat("memo");
        if (tempPath == null) {
            throw new IllegalArgumentException("path不能为空，请先调用ArgumentUtil.parseArgs()解析参数");
        }
        long start = System.currentTimeMillis();
        File memoFolder = new File(tempPath);
        map = new ConcurrentHashMap<String, Map<String, Object>>();
        if (memoFolder.exists()) {
            // 做还原操作
            log.info("检测到\"{}\"有未完成的分析任务，将继续执行此任务", ArgumentUtil.getString("path"));
            try {
                for (File classFolder : memoFolder.listFiles()) {
                    Map<String, Object> classMap = new HashMap<String, Object>();
                    map.put(classFolder.getName(), classMap);
                    for (File userFile : classFolder.listFiles()) {
                        if (userFile.isDirectory()) {
                            if ("map".equals(SerializeUtil.getMark(userFile))) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                classMap.put(userFile.getName(), map);
                                for (File f : userFile.listFiles()) {
                                    map.put(CodecUtil.decode(f.getName()), SerializeUtil.readObject(f));
                                }
                            } else if ("list".equals(SerializeUtil.getMark(userFile))) {
                                List<Object> list = new ArrayList<Object>();
                                classMap.put(userFile.getName(), list);
                                for (File f : userFile.listFiles()) {
                                    list.add(SerializeUtil.readObject(f));
                                }
                            } else {
                                log.warn("未知的文件类型，无法还原：{}", userFile.getAbsolutePath());
                            }
                        } else if (userFile.length() > 0) {
                            classMap.put(userFile.getName(), SerializeUtil.readObject(userFile));
                        }
                    }
                }
                log.info("还原任务成功，耗时：{}秒", (System.currentTimeMillis() - start)/1000);
            } catch (Exception e) {
                log.error("还原任务失败", e);
            } finally {
                StorageUtil.asyncDeleteFile(memoFolder);
            }
        }
        // 注册备份钩子
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    /**
     * 逻辑没问题，小碎文件多，性能太差
     */
    @Deprecated
    private static class ShutdownThread extends Thread {
        @Override
        public void run() {
            // 如果是正常退出，不做处理
            if (map.isEmpty()) {
                return;
            }
            long start = System.currentTimeMillis();
            log.info("程序异常退出，正在缓存中间结果，请耐心等待...");
            final String tempPath = StorageUtil.getTempPath().concat("memo");
            try {
                new File(tempPath).mkdirs();
                Iterator<Map.Entry<String, Map<String, Object>>> classIte = map.entrySet().iterator();
                while (classIte.hasNext()) {
                    Map.Entry<String, Map<String, Object>> classEntry = classIte.next();
                    File classFolder = new File(tempPath, classEntry.getKey());
                    classFolder.mkdirs();
                    Iterator<Map.Entry<String, Object>> userIte = classEntry.getValue().entrySet().iterator();
                    while (userIte.hasNext()) {
                        Map.Entry<String, Object> userEntry = userIte.next();
                        // deal map
                        if (userEntry.getValue() instanceof Map) {
                            File folder = new File(classFolder, userEntry.getKey());
                            SerializeUtil.addMark(folder, "map");
                            Map<String, Object> map = (Map<String, Object>)userEntry.getValue();
                            Iterator<Map.Entry<String, Object>> ite = map.entrySet().iterator();
                            while (ite.hasNext()) {
                                Map.Entry<String, Object> e = ite.next();
                                File file = new File(folder, CodecUtil.encode(e.getKey()));
                                SerializeUtil.writeObject(file, e.getValue());
                                ite.remove();
                            }
                        } else if (userEntry.getValue() instanceof List) { // deal list
                            File folder = new File(classFolder, userEntry.getKey());
                            SerializeUtil.addMark(folder, "list");
                            Iterator<Object> ite = ((List<Object>)userEntry.getValue()).iterator();
                            int i = 0;
                            while (ite.hasNext()) {
                                File file = new File(folder, String.valueOf(i++));
                                SerializeUtil.writeObject(file, ite.next());
                                ite.remove();
                            }
                        } else { // deal other
                            File file = new File(classFolder, userEntry.getKey());
                            SerializeUtil.writeObject(file, userEntry.getValue());
                        }
                        userIte.remove();
                    }
                    classIte.remove();
                }
                log.info("程序异常退出，中间结果已保存在\"{}\", 下次执行此任务将继续", tempPath);
                log.info("如果是内存溢出情况建议解决方案如下：(需根据机器内存和日志量动态选择解决方案)");
                log.info("1.启动时指定一个较大的堆内存，如：java -Xmx8192m -jar ...");
                log.info("2.启用磁盘缓存，方法：修改config.properties的tools.impl.enableDiskCache=true");
            } catch (Exception e) {
                log.error("缓存中间结果出错", e);
                new File(StorageUtil.getTempPath().concat("memo")).deleteOnExit();
            }
            log.info("缓存中间结果耗时：{}秒", (System.currentTimeMillis() - start)/1000);
        }
    }
}
