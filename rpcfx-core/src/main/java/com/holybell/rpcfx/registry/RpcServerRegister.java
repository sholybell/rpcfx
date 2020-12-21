package com.holybell.rpcfx.registry;

import com.alibaba.fastjson.JSON;
import com.holybell.rpcfx.annotations.RpcServer;
import com.holybell.rpcfx.zk.ZkUtil;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * 注册RpcServer
 */
public class RpcServerRegister implements ApplicationListener<WebServerInitializedEvent> {

    private final Logger logger = LoggerFactory.getLogger(RpcServerRegister.class);

    public static final String zkNodePrefix = "/rpcfx/service";

    /**
     * WEB容器启动完毕事件监听
     */
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();  // IP
            int port = event.getWebServer().getPort();                  // 端口
            Map<String, Object> beansWithAnnotation = event.getApplicationContext().getBeansWithAnnotation(RpcServer.class);
            beansWithAnnotation.forEach((key, value) -> {
                RpcServer rpcServer = AnnotationUtils.findAnnotation(value.getClass(), RpcServer.class);
                ServiceInfo serviceInfo = new ServiceInfo(key, host, port, rpcServer.group(), rpcServer.version());
                Class interfacs = value.getClass().getInterfaces()[0];
                ZkUtil.createNode(zkNodePrefix + "/" + interfacs.getCanonicalName() + "/" + rpcServer.group(), JSON.toJSONString(serviceInfo), CreateMode.EPHEMERAL_SEQUENTIAL);
            });
        } catch (UnknownHostException e) {
            logger.warn("获取RPC服务器IP异常", e);
        }
    }
}
