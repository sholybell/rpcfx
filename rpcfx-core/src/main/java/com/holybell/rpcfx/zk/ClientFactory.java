package com.holybell.rpcfx.zk;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ClientFactory {

    private ClientFactory() {

    }

    /**
     * @param connectionString zk的连接地址
     * @return CuratorFramework 实例
     */
    public static CuratorFramework createSimeple(String connectionString) {

        /**
         * 重试策略：第一次重试等待1s，第二次重试等待2s，第三次重试等待4s
         * 第一个参数：等待时间的基础单位，单位为毫秒
         * 第二个参数：最大的重试次数
         */
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(1000, 3);

        /**
         *获取CuratorFrameworkFactory 实例最简单的方式
         * 第一个参数：zk的连接地址
         * 第二个参数： 重试策略
         */
        return CuratorFrameworkFactory.newClient(connectionString, retry);
    }

    public static CuratorFramework createWithOptions(
            String connectionString, RetryPolicy retryPolicy,
            int connectionTimeoutMs, int sessionTimeoutMs) {

        // 使用builder 方法创建 CuratorFramework 实例
        return CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

}