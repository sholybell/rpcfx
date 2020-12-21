package com.holybell.rpcfx.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ZkUtil {

    private static final Logger logger = LoggerFactory.getLogger(ZkUtil.class);

    private static final CuratorFramework client = ClientFactory.createSimeple("localhost:2181");

    static {
        client.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CloseableUtils.closeQuietly(client);
        }));
    }

    /**
     * 校验节点是否存在
     */
    public static boolean checkExists(String zkPath) {
        try {
            return Optional.ofNullable(client.checkExists().forPath(zkPath)).isPresent();
        } catch (Exception e) {
            logger.warn("判断zk节点是否存在异常", e);
            throw new RuntimeException("判断zk节点是否存在异常", e);
        }
    }

    /**
     * 创建节点
     *
     * @param zkPath  节点路径
     * @param payload 节点内容
     */
    public static String createNode(String zkPath, String payload, @Nullable CreateMode createMode) {
        try {
            if (!checkExists(zkPath)) {   // 不存在再创建节点
                byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
                return client.create()
                        .creatingParentsIfNeeded()
                        .withMode(Optional.ofNullable(createMode).orElse(CreateMode.PERSISTENT))
                        .forPath(zkPath, payloadBytes);
            }
            return "";
        } catch (Exception e) {
            logger.warn("创建zk节点异常", e);
            throw new RuntimeException("创建zk节点异常", e);
        }
    }

    /**
     * 修改节点信息
     */
    public static Stat updateNode(String zkPath, String payload) {
        try {
            byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
            return client.setData().forPath(zkPath, payloadBytes);
        } catch (Exception e) {
            logger.warn("修改zk节点信息异常", e);
            throw new RuntimeException("修改zk节点信息异常", e);
        }
    }

    /**
     * 删除节点信息
     */
    public static void deleteNode(String zkPath) {
        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(zkPath);
        } catch (Exception e) {
            logger.warn("删除zk节点异常", e);
            throw new RuntimeException("删除zk节点异常", e);
        }
    }

    /**
     * 读取节点信息
     */
    public static String readNode(String zkPath) {
        try {
            Stat stat = client.checkExists().forPath(zkPath);
            if (stat != null) {
                // 读取节点信息
                byte[] payload = client.getData().forPath(zkPath);
                return new String(payload, StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception e) {
            logger.warn("读取zk节点信息异常", e);
            throw new RuntimeException("读取zk节点信息异常", e);
        }
    }

    /**
     * 监听节点变动,仅能监听一次就作废
     */
    public static String watch(String zkPath, Watcher watcher) {
        try {
            byte[] bytes = client.getData().usingWatcher(watcher).forPath(zkPath);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("监听zk节点异常", e);
            throw new RuntimeException("监听zk节点异常", e);
        }
    }

    /**
     * 添加PathCache监听器
     * Path Cache用来观察ZNode的子节点并缓存状态，如果ZNode的子节点被创建，更新或者删除，
     * 那么Path Cache会更新缓存，并且触发事件给注册的监听器。
     * Path Cache是通过PathChildrenCache类来实现的，监听器注册是通过PathChildrenCacheListener
     *
     * @param zkPath  zk节点
     * @param isCache 是否使用缓存
     */
    public static void addPathCacheListener(String zkPath, boolean isCache,
                                            @Nullable PathChildrenCache.StartMode startMode,
                                            PathChildrenCacheListener listener) {
        try {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, zkPath, isCache);
            pathChildrenCache.start(Optional.ofNullable(startMode).orElse(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE));
            pathChildrenCache.getListenable().addListener(listener);
        } catch (Exception e) {
            logger.warn("添加PathChildrenCache监听器异常", e);
            throw new RuntimeException("添加PathChildrenCache监听器异常", e);
        }
    }

    /**
     * Node Cache用来观察ZNode自身，如果ZNode节点本身被创建，更新或者删除，
     * 那么Node Cache会更新缓存，并触发事件给注册的监听器。
     * <p>
     * Node Cache是通过NodeCache类来实现的，监听器对应的接口为NodeCacheListener。
     */
    public void addNodeCacheListener(String zkPath, Boolean isCache,
                                     @Nullable Boolean buildInitial,
                                     NodeCacheListener listener) {
        try {
            NodeCache nodeCache = new NodeCache(client, zkPath, Optional.ofNullable(isCache).orElse(true));
            nodeCache.start(Optional.ofNullable(buildInitial).orElse(false));
            nodeCache.getListenable().addListener(listener);
        } catch (Exception e) {
            logger.warn("添加NodeCache监听器异常", e);
            throw new RuntimeException("添加NodeCache监听器异常", e);
        }
    }

    /**
     * 相当于 PathChildrenCache 和 NodeCache 的结合，监听子节点和节点本身
     */
    public void addTreeCacheListener(String zkPath, TreeCacheListener listener) {
        try {
            TreeCache treeCache = new TreeCache(client, zkPath);
            treeCache.start();
            treeCache.getListenable().addListener(listener);
        } catch (Exception e) {
            logger.warn("添加TreeCache监听器异常", e);
            throw new RuntimeException("添加TreeCache监听器异常", e);
        }
    }

    /**
     * 获取所有子节点信息
     */
    public static List<String> getChildrens(String zkPath) {
        try {
            return client.getChildren().forPath(zkPath);
        } catch (Exception e) {
            logger.warn("获取zk子节点列表信息异常", e);
            throw new RuntimeException("获取zk子节点列表信息异常", e);
        }
    }
}
