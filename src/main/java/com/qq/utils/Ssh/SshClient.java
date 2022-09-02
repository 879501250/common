package com.qq.utils.Ssh;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SshClient {

    /**
     * 执行ssh 命令
     *
     * @param command      要执行的命令，多个命令通过";"号隔开
     * @param execCallback 命令执行过程中的回调器
     * @return 执行成功返回0，失败返回1
     * @throws IOException
     */
    int exec(String command, ExecCallback execCallback) throws IOException;

    /**
     * 执行多行命令
     *
     * @param command
     * @param execCallback
     * @return
     * @throws IOException
     */
    int exec(List<String> command, ExecCallback execCallback) throws IOException;

    /**
     * sftp上传文件
     *
     * @param srcFile   源文件
     * @param destFile  目标文件
     * @param overwrite 文件是否可以覆盖
     */
    void upload(String srcFile, String destFile, boolean overwrite) throws IOException;

    /**
     * 断开客户端连接
     */
    void disconnect() throws IOException;

    /**
     * 批量下载文件
     *
     * @param remotePaths 远程下载目录
     * @param localPath   本地保存目录（以路径符号结束）
     * @return
     */
    List<File> batchDownLoadFile(List<String> remotePaths, String localPath);
}
