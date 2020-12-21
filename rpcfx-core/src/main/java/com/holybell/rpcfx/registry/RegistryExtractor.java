package com.holybell.rpcfx.registry;

import com.alibaba.fastjson.JSON;
import com.holybell.rpcfx.zk.ZkUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 从Zk里面拉取注册信息
 */
public class RegistryExtractor {

    private RegistryExtractor(){

    }

    /**
     * 从ZK中拉取注册信息
     */
    public static void extractRegistry(Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>>> localRegistry) {
        List<String> serviceNameList = ZkUtil.getChildrens(RpcServerRegister.zkNodePrefix);
        serviceNameList.forEach(serviceName -> {
            List<String> instances = ZkUtil.getChildrens(RpcServerRegister.zkNodePrefix + "/" + serviceName);
            for (String instance : instances) {
                // 删除ZK自己生成的末尾10位序列号
                String group = instance.substring(0, instance.length() - 10);
                String instanceInfo = ZkUtil.readNode(RpcServerRegister.zkNodePrefix + "/" + serviceName + "/" + instance);
                localRegistry.computeIfAbsent(serviceName, zkPath -> new ConcurrentHashMap<>()).computeIfAbsent(group, zkPath -> new CopyOnWriteArrayList<>()).add(JSON.parseObject(instanceInfo, ServiceInfo.class));
            }
        });
    }

    /**
     * 拉取指定服务
     */
    public static void extraceRegisty(String serviceName, Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>>> localRegistry) {
        List<String> instances = ZkUtil.getChildrens(RpcServerRegister.zkNodePrefix + "/" + serviceName);
        for (String instance : instances) {
            // 删除ZK自己生成的末尾10位序列号
            String group = instance.substring(0, instance.length() - 10);
            String instanceInfo = ZkUtil.readNode(RpcServerRegister.zkNodePrefix + "/" + serviceName + "/" + instance);
            localRegistry.computeIfAbsent(serviceName, zkPath -> new ConcurrentHashMap<>()).computeIfAbsent(group, zkPath -> new CopyOnWriteArrayList<>()).add(JSON.parseObject(instanceInfo, ServiceInfo.class));
        }
    }
}
