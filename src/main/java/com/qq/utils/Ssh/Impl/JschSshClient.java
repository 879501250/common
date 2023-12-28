package com.qq.utils.Ssh.Impl;

import com.jcraft.jsch.*;
import com.qq.utils.File.FileUtil;
import com.qq.utils.Ssh.ExecCallback;
import com.qq.utils.Ssh.SshClient;
import com.qq.utils.String.StrUtil;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JschSshClient implements SshClient {

    private final static Logger log = LoggerFactory.getLogger(JschSshClient.class);

    public final static String HEADER = "-----BEGIN RSA PRIVATE KEY-----";

    public final static String FOOTER = "-----END RSA PRIVATE KEY-----";

    /**
     * jsch session
     */
    private Session session;

    /**
     * 基于用户名密码认证
     *
     * @param username
     * @param password
     * @param host
     * @param port
     * @throws JSchException
     */
    public JschSshClient(String username, String password, String host, int port) throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setConfig("PreferredAuthentications", "password");
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
    }

    /**
     * 通过私钥认证
     *
     * @param host
     * @param port
     * @param username
     * @param privateKey
     * @param passphrase
     * @throws JSchException
     */
    public JschSshClient(String host, int port, String username, String privateKey, byte[] passphrase) throws JSchException {
        JSch jsch = new JSch();
        // 根据用户名，主机ip，端口获取一个Session对象
        session = jsch.getSession(username, host, port);

        // 密钥文本
        if (StrUtil.startWith(privateKey, HEADER)) {
            // 直接采用 private key content 登录，无需写入文件
            jsch.addIdentity(null, privateKey.getBytes(), null, passphrase);
        } else {
            // 密钥文件
            File rsaFile = null;
            // 指定密钥文件
            if (StrUtil.startWith(privateKey, FileUtil.FILE_URL_PREFIX)) {
                String rsaPath = StrUtil.removePrefix(privateKey, FileUtil.FILE_URL_PREFIX);
                rsaFile = FileUtil.file(rsaPath);
            } else if (!StrUtil.hasText(privateKey)) {
                // 密钥文件没有指定，采用默认私钥  SSH 用户 .ssh 目录中的身份
                File home = FileUtil.getUserHomeDir();
                Assert.notNull(home, "用户目录没有找到");
                String sshPath = home + File.separator + ".ssh";
                String idRsaPath = sshPath + File.separator + "id_rsa";
                String idDsaPath = sshPath + File.separator + "id_dsa";
                if (FileUtil.isFile(idRsaPath))
                    rsaFile = FileUtil.file(idRsaPath);
                else if (FileUtil.isFile(idDsaPath))
                    rsaFile = FileUtil.file(idDsaPath);
                else
                    Assert.notNull(rsaFile, "用户目录没有找到私钥信息");
            }
            // 简要私钥文件是否存在
            Assert.state(FileUtil.isFile(rsaFile), "私钥文件不存在：" + FileUtil.getAbsolutePath(rsaFile));
            jsch.addIdentity(FileUtil.getAbsolutePath(rsaFile), passphrase);
        }
        // 第一次登陆时候，是否需要提示信息
        session.setConfig("StrictHostKeyChecking", "no");
        // 设置ssh的DH秘钥交换
        // session.setConfig("kex", "diffie-hellman-group1-sha1");
        // 跳过Kerberos username 身份验证提示
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.connect();
    }

    @Override
    public int exec(String command, ExecCallback execCallback) throws IOException {
        ChannelShell channel = null;
        try {
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        InputStream inputStream = channel.getInputStream();
        OutputStream outputStream = channel.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);

        printWriter.println(command);

        printWriter.println("exit");
        printWriter.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        String msg = null;
        while ((msg = in.readLine()) != null) {
            execCallback.callback(msg);
        }
        int result = channel.getExitStatus();

        in.close();

        return result;
    }

    @Override
    public int exec(List<String> commands, ExecCallback execCallback) throws IOException {
        ChannelShell channel = null;
        try {
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        InputStream inputStream = channel.getInputStream();
        OutputStream outputStream = channel.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);

        for (String command : commands) {
            printWriter.println(command);
        }


        printWriter.println("exit");
        printWriter.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        String msg = null;
        while ((msg = in.readLine()) != null) {
            execCallback.callback(msg);
        }
        while (true) {
            if (channel.isClosed()) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int result = channel.getExitStatus();

        in.close();
        channel.disconnect();

        return result;
    }

    @Override
    public void disconnect() throws IOException {
        session.disconnect();
    }

    @Override
    public void upload(String srcFile, String destFile, boolean overwrite) throws IOException {
        String destDir = FilenameUtils.getFullPath(destFile);
        String destFileName = FilenameUtils.getName(destFile);
        ChannelSftp sftpChannel = null;
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        try {
            // 1.目标文件不可以覆盖 2. 远程目标文件已存在则上传结束
            if (!overwrite && isFileExist(destFile, sftpChannel))
                return;
            sftpChannel.cd("/");
            createDir(destDir, sftpChannel);
            sftpChannel.cd(destDir);
            sftpChannel.put(srcFile, destFileName, ChannelSftp.OVERWRITE);
            System.out.println("Transmission complete");
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new RuntimeException("execute upload error", e);
        } finally {
            if (null != sftpChannel && sftpChannel.isConnected()) {
                sftpChannel.quit();
                sftpChannel.disconnect();
            }
        }
    }

    public boolean isFileExist(String filePath, ChannelSftp sftp) {
        boolean is = false;
        try {
            if (sftp.stat(filePath) != null)
                is = true;
        } catch (SftpException e) {

        }
        return is;
    }

    public static boolean isDirExist(String directory, ChannelSftp sftp) {
        boolean is = false;
        try {
            if (sftp.ls(directory) != null) {
                is = true;
            }
        } catch (SftpException e) {
            System.out.println(directory + " : " + e.getMessage());
        }
        return is;
    }

    public static void createDir(String createpath, ChannelSftp sftp) throws SftpException {
        if (isDirExist(createpath, sftp)) {
            sftp.cd(createpath);
        }
        String pathArr[] = createpath.split("/");
        StringBuffer filePath = new StringBuffer("/");
        for (String path : pathArr) {
            if (path.equals("")) {
                continue;
            }
            filePath.append(path + "/");
            if (isDirExist(filePath.toString(), sftp)) {
                sftp.cd(filePath.toString());
            } else {
                // 建立目录
                sftp.mkdir(filePath.toString());
                // 进入并设置为当前目录
                sftp.cd(filePath.toString());
            }
        }
        sftp.cd(createpath);
    }

    public List<File> batchDownLoadFile(List<String> remotePaths, String localPath) {
        List<File> fileList = new ArrayList<>();
        File file = null;
        ChannelSftp sftp = null;
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            for (String remotePath : remotePaths) {
                String filename = remotePath.substring(remotePath.lastIndexOf("/") + 1);
                file = downloadFile(remotePath.substring(0, remotePath.lastIndexOf("/") + 1),
                        filename, localPath, filename, sftp);
                if (file != null) {
                    fileList.add(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != sftp && sftp.isConnected()) {
                sftp.quit();
                sftp.disconnect();
            }
        }
        return fileList;
    }

    /**
     * 下载单个文件
     *
     * @param remotePath：远程下载目录（以路径符号结束）
     * @param remoteFileName：下载文件名
     * @param localPath：本地保存目录（以路径符号结束）
     * @param localFileName：保存文件名
     * @param sftp
     * @return
     */
    public File downloadFile(String remotePath, String remoteFileName,
                             String localPath, String localFileName, ChannelSftp sftp) {
        FileOutputStream fieloutput = null;
        File file = null;
        try {
            file = new File(localPath + localFileName);
            fieloutput = new FileOutputStream(file);
            sftp.get(remotePath + remoteFileName, fieloutput);
            fieloutput.close();
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fieloutput) {
                try {
                    fieloutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }
}
