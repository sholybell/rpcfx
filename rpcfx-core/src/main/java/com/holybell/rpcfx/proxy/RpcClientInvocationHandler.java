package com.holybell.rpcfx.proxy;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.holybell.rpcfx.api.RpcfxRequest;
import com.holybell.rpcfx.api.RpcfxResponse;
import com.holybell.rpcfx.errorhandler.RpcfxErrorHandler;
import com.holybell.rpcfx.exception.RpcfxException;
import com.holybell.rpcfx.registry.RpcServerRegister;
import com.holybell.rpcfx.registry.ServiceInfo;
import com.holybell.rpcfx.util.HttpUtil;
import com.holybell.rpcfx.zk.ZkUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.holybell.rpcfx.registry.RegistryExtractor;
import com.holybell.rpcfx.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RpcClientInvocationHandler implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(RpcClientInvocationHandler.class);

    private XStream xstream = new XStream(new StaxDriver());

    private static final Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>>> localReigstryCenter = new ConcurrentHashMap<>();

    static {
        ParserConfig.getGlobalInstance().addAccept("com.holybell");
    }

    {
        XStream.setupDefaultSecurity(xstream);
        // TODO 如何动态增加？ 扫描指定包？
        xstream.allowTypes(new String[]{
                "com.holybell.rpcfx.api.RpcfxResponse",
                "com.holybell.rpcfx.demo.api.model.Order",
                "com.holybell.rpcfx.demo.api.model.User"
        });

        // 确保ZK注册根目录存在，不存在先创建，避免先启动RPC客户端
        if (!ZkUtil.checkExists(RpcServerRegister.zkNodePrefix)) {
            ZkUtil.createNode(RpcServerRegister.zkNodePrefix, "", null);
        }
        // 拉取注册信息
        RegistryExtractor.extractRegistry(localReigstryCenter);
        // 监听注册信息
        RegistryListener.listenRegistry(localReigstryCenter);
    }


    private RpcfxErrorHandler errorHandler;
    private Class<?> rpcClientInterface;

    public RpcClientInvocationHandler(Class<?> rpcClientInterface, @Nullable RpcfxErrorHandler errorHandler) {
        this.rpcClientInterface = rpcClientInterface;
        this.errorHandler = errorHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcfxRequest request = new RpcfxRequest();
        request.setServiceClass(rpcClientInterface);
        request.setMethod(method.getName());
        request.setParams(args);

        String url = "";
        String group = "default-group";     // TODO RpcClient注解添加指定group和version的功能
        String serviceName = rpcClientInterface.getCanonicalName();
        if (localReigstryCenter.get(serviceName) == null || localReigstryCenter.get(serviceName).get(group) == null) {
            throw new RpcfxException("未检测到相应服务的注册信息!");
        }

        List<ServiceInfo> serviceInfoList = localReigstryCenter.get(serviceName).get(group);
        // 从zk中拉取注册信息
        ServiceInfo serviceInfo = serviceInfoList.get(serviceInfoList.size() - 1);
        // 获取注册信息最后一个服务
        url = "http://" + serviceInfo.getHost() + ":" + serviceInfo.getPort();

        try {
            RpcfxResponse response = post(request, url);
            if (response.isStatus()) {
                return response.getResult();
            }
            throw response.getException();
        } catch (Exception ex) {
            // 如果有自定义的异常处理器，则捕获异常进行处理
            if (errorHandler != null) {
                errorHandler.handleError(ex);
                return null;
            }
            throw ex;
        }
    }

    private RpcfxResponse post(RpcfxRequest req, String url) throws IOException {
        String reqJson = JSON.toJSONString(req);
        if (logger.isInfoEnabled()) {
            logger.info("request json : {}", reqJson);
        }

        // 1.可以复用client
        // 2.尝试使用httpclient或者netty client

        // 使用HttpUtil作为可复用的client
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json; charset=utf-8");
        String respJson = HttpUtil.post(url, JSON.toJSONString(req), header, 30000);
        if (logger.isInfoEnabled()) {
            logger.info("resp json/xml : {}", respJson);
        }
        if (respJson.contains("<")) {    // TODO 如何更加优雅的判断XML类型?
            return (RpcfxResponse) xstream.fromXML(respJson);
        } else {
            return JSON.parseObject(respJson, RpcfxResponse.class);
        }
    }
}
