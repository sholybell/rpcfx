package com.holybell.rpcfx.registry;

import com.alibaba.fastjson.JSON;
import com.holybell.rpcfx.zk.ZkUtil;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RegistryListener {

    private RegistryListener() {

    }

    public static void listenRegistry(Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>>> localRegistry) {
        // 原有的服务变动了实例信息
        List<String> services = ZkUtil.getChildrens(RpcServerRegister.zkNodePrefix);
        for (String service : services) {
            // 注册监听器
            addListener(service, localRegistry);
        }

        // 添加了全新的服务
        ZkUtil.addPathCacheListener(RpcServerRegister.zkNodePrefix,
                true,
                PathChildrenCache.StartMode.BUILD_INITIAL_CACHE,
                (client, event) -> {
                    String serviceName = event.getData().getPath().replace(RpcServerRegister.zkNodePrefix + "/", "");
                    // 添加新的服务
                    RegistryExtractor.extraceRegisty(serviceName, localRegistry);
                    // 监听该服务的目录
                    addListener(serviceName, localRegistry);
                });
    }

    /**
     * 注册监听器
     */
    private static void addListener(String service, Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>>> localRegistry) {
        ZkUtil.addPathCacheListener(RpcServerRegister.zkNodePrefix + "/" + service,
                true,
                PathChildrenCache.StartMode.BUILD_INITIAL_CACHE,
                (client, event) -> {
                    PathChildrenCacheEvent.Type type = event.getType();
                    String group = getGroup(event.getData().getPath());
                    ServiceInfo serviceInfoFromZk = JSON.parseObject(new String(event.getData().getData(), StandardCharsets.UTF_8), ServiceInfo.class);
                    ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>> groupRegistryMap = localRegistry.get(service);
                    if (groupRegistryMap != null) {
                        CopyOnWriteArrayList<ServiceInfo> serviceInfos = groupRegistryMap.get(group);
                        if (serviceInfos != null) {
                            switch (type) {
                                case CHILD_ADDED:   // 新增节点
                                    serviceInfos.add(serviceInfoFromZk);
                                    break;
                                case CHILD_UPDATED: // 修改节点
                                    serviceInfos.removeIf(serviceInfo -> serviceInfoFromZk.getUniqueId().equals(serviceInfo.getUniqueId()));
                                    serviceInfos.add(serviceInfoFromZk);
                                    break;
                                case CHILD_REMOVED: // 删除节点
                                    serviceInfos.removeIf(serviceInfo -> serviceInfoFromZk.getUniqueId().equals(serviceInfo.getUniqueId()));
                                    break;
                                default:
                                    // 什么都不做
                            }
                        }
                    }
                });
    }

    /**
     * 从实例节点中抽取分组名称
     */
    private static String getGroup(String zkPath) {
        String group = zkPath.substring(zkPath.lastIndexOf("/") + 1, zkPath.length());
        return group.substring(0, group.length() - 10);
    }

}
