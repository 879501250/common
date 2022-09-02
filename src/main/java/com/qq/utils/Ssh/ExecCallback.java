package com.qq.utils.Ssh;

public interface ExecCallback {

    /**
     * 回调执行方法
     * @param out 命令执行的输出
     */
    void callback(String out);
}
