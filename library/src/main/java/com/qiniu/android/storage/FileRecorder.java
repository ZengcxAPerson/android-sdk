package com.qiniu.android.storage;

import android.os.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Date;
import java.util.stream.Stream;

/**
 * 实现分片上传时上传进度的接口方法
 */
public final class FileRecorder implements Recorder {

    /**
     * 记录路径
     */
    public String directory;

    /**
     * 构造方法
     *
     * @param directory 记录路径
     * @throws IOException 异常
     */
    public FileRecorder(String directory) throws IOException {
        this.directory = directory;
        File f = new File(directory);
        if (!f.exists()) {
            boolean r = f.mkdirs();
            if (!r) {
                throw new IOException("mkdir failed");
            }
            return;
        }
        if (!f.isDirectory()) {
            throw new IOException("does not mkdir");
        }
    }

    private static String hash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(base.getBytes());
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 纪录分片上传进度
     *
     * @param key  上传文件进度文件保存名
     * @param data 上传文件的进度数据
     */
    @Override
    public void set(String key, byte[] data) {
        if (key == null) {
            return;
        }
        File f = new File(directory, hash(key));
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
            fo.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fo != null) {
            try {
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取分片上传进度
     *
     * @param key 上传文件进度文件保存名
     */
    @Override
    public byte[] get(String key) {
        File f = new File(directory, hash(key));
        FileInputStream fi = null;
        byte[] data = null;
        int read = 0;
        try {
            if (outOfDate(f)) {
                f.delete();
                return null;
            }
            data = new byte[(int) f.length()];
            fi = new FileInputStream(f);
            read = fi.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fi != null) {
            try {
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (read == 0) {
            return null;
        }
        return data;
    }

    private boolean outOfDate(File f) {
        return f.lastModified() + 1000 * 3600 * 24 * 2 < new Date().getTime();
    }

    /**
     * 删除已上传文件的进度文件
     *
     * @param key 上传文件进度文件保存名
     */
    @Override
    public void del(String key) {
        File f = new File(directory, hash(key));
        f.delete();
    }

    /**
     * 删除所有路径
     */
    public void deleteAll() {
        try {
            File folder = new File(directory);
            deleteDirectoryLegacyIO(folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteDirectoryLegacyIO(File file) {
        if (!file.exists()) {
            return;
        }

        if (!file.isDirectory()) {
            file.delete();
            return;
        }

        File[] list = file.listFiles();
        if (list != null) {
            for (File temp : list) {
                deleteDirectoryLegacyIO(temp);
            }
        }
        file.delete();
    }

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    @Override
    public String getFileName() {
        return null;
    }
}