package com.yuanzhy.tools.sql.common.util;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 序列化大量数据性能太差，弃用
 * @Author yuanzhy
 * @Date 2018/8/17
 */
@Deprecated
public class SerializeUtil {

    public static void writeObject(File file, Object value) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            oos.writeObject(value);
        } catch (Exception e) {
            throw new RuntimeException("写入对象失败", e);
        } finally {
            IOUtils.closeQuietly(oos);
        }
    }

    public static Object readObject(File file) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("读取对象失败", e);
        } finally {
            IOUtils.closeQuietly(ois);
        }
    }

    public static void addMark(File folder, String mark) throws IOException {
        new File(folder.getParent(), folder.getName() + "-" + mark).createNewFile();
    }

    public static String getMark(final File folder) {
        File[] files = folder.getParentFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !folder.getName().equals(name);
            }
        });
        for (int i=0; i<files.length; i++) {
            String name = files[i].getName();
            if (name.startsWith(folder.getName())) {
                return name.substring(name.lastIndexOf("-") + 1);
            }
        }
        return null;
    }
}
