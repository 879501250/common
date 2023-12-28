package com.qq.utils.Ssh;

import com.jcraft.jsch.JSchException;
import com.qq.utils.Ssh.Impl.JschSshClient;
import com.qq.utils.String.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshClientFactory {

    private final static Logger log = LoggerFactory.getLogger(SshClientFactory.class);

    /**
     * 基于用户名密码认证创建客户端
     *
     * @param username 用户名
     * @param password 密码
     * @param host     主机ip
     * @param port     ssh端口号
     * @return
     */
    public static SshClient createSsh(String username, String password, String host, int port) {
        try {
            return new JschSshClient(username, password, host, port);
        } catch (JSchException e) {
            log.error(e.getMessage());
            throw new RuntimeException("无法登陆到服务器。可能是服务器地址、用户名或者密码错误。", e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 基于密钥文件认证
     *
     * @param host
     * @param port
     * @param username
     * @param privateKey
     * @param password
     * @return
     */
    public static SshClient createSsh(String host, int port, String username, String privateKey, String password) {
        try {
            return new JschSshClient(host, port, username, privateKey, StrUtil.hasText(password) ? password.getBytes() : null);
        } catch (JSchException e) {
            throw new RuntimeException("无法登陆到服务器。可能是服务器地址、用户名或密钥错误。", e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
