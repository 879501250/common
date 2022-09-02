package com.qq.utils.File;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    /**
     * 针对ClassPath路径的伪协议前缀（兼容Spring）: "classpath:"
     */
    public static final String CLASSPATH_URL_PREFIX = "classpath:";
    /**
     * URL 前缀表示文件: "file:"
     */
    public static final String FILE_URL_PREFIX = "file:";

    /**
     * 判断是否为文件，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果为文件true
     */
    public static boolean isFile(String path) {
        return (null != path) && file(path).isFile();
    }

    /**
     * 创建File对象，自动识别相对或绝对路径，相对路径将自动从ClassPath下寻找
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return File
     */
    public static File file(String path) {
        if (null == path) {
            return null;
        }
        return new File(path);
    }

    public static File file(File directory, String... names) {
        if (directory.exists()) {
            throw new RuntimeException("directory must not be null");
        }
        if (null == names || names.length == 0) {
            return directory;
        }

        File file = directory;
        for (String name : names) {
            if (null != name) {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * 判断是否为文件，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为文件true
     */
    public static boolean isFile(File file) {
        return (null != file) && file.isFile();
    }

    /**
     * 获取标准的绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * 获取用户目录
     *
     * @return 用户目录
     * @since 4.0.6
     */
    public static File getUserHomeDir() {
        return file(getUserHomePath());
    }

    /**
     * 获取用户路径（绝对路径）
     *
     * @return 用户路径
     * @since 4.0.6
     */
    public static String getUserHomePath() {
        return System.getProperty("user.home");
    }

    //删除文件夹
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下的所有文件
    public static boolean delAllFile(String path) throws InterruptedException {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                //由于关闭io流后文件仍然被占用,因此直接采用gc垃圾回收等待两秒删除
                System.gc();
                Thread.sleep(2000);
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 将文件打包
     *
     * @param srcfile：文件集合
     * @param zipFileName：生成的文件名
     */
    public static void zipFiles(List<File> srcfile, String zipFileName, HttpServletResponse response) throws IOException {
        byte[] buf = new byte[1024];
        // 获取输出流
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileInputStream in = null;
        ZipOutputStream out = null;
        try {
            response.reset();

            //添加响应头的跨域信息--开始
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type");
            // 不同类型的文件对应不同的MIME类型
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(zipFileName + ".zip", "UTF-8"));

            // ZipOutputStream类：完成文件或文件夹的压缩
            out = new ZipOutputStream(bos);
            for (int i = 0; i < srcfile.size(); i++) {
                in = new FileInputStream(srcfile.get(i));
                // 给列表中的文件单独命名
                out.putNextEntry(new ZipEntry(srcfile.get(i).getName()));
                int len = -1;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            }
            out.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }
}
