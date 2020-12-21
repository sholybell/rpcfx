package com.holybell.rpcfx.aspects;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.holybell.rpcfx.annotations.RpcClient;
import com.holybell.rpcfx.api.RpcfxRequest;
import com.holybell.rpcfx.api.RpcfxResponse;
import com.holybell.rpcfx.errorhandler.RpcfxErrorHandler;
import com.holybell.rpcfx.exception.RpcfxException;
import com.holybell.rpcfx.registry.RegistryExtractor;
import com.holybell.rpcfx.registry.RegistryListener;
import com.holybell.rpcfx.registry.RpcServerRegister;
import com.holybell.rpcfx.registry.ServiceInfo;
import com.holybell.rpcfx.util.HttpUtil;
import com.holybell.rpcfx.zk.ZkUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Aspect
public class RpcProxyFactoryAspect {

    @Autowired(required = false)
    private RpcfxErrorHandler rpcfxErrorHandler;

    private Logger logger = LoggerFactory.getLogger(RpcProxyFactoryAspect.class);

    private XStream xstream = new XStream(new StaxDriver());

    private static final Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>>> localReigstryCenter = new ConcurrentHashMap<>();

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


    // 是否降级处理
    @Value("${rpcfx.fallback}")
    private boolean fallback = Boolean.FALSE;

    static {
        ParserConfig.getGlobalInstance().addAccept("com.holybell");
    }

    @Pointcut("execution(* com.holybell.rpcfx.demo.api.service.*.*(..))")
    public void pointcut() {
        // 直接拦截指定包中的所有方法
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RpcfxRequest request = new RpcfxRequest();
        Class _interface = joinPoint.getSignature().getDeclaringType();
        request.setServiceClass(_interface);
        request.setMethod(joinPoint.getSignature().getName());
        request.setParams(joinPoint.getArgs());

        String url = "";
        String group = "default-group";     // TODO RpcClient注解添加指定group和version的功能
        String serviceName = _interface.getCanonicalName();
        if (localReigstryCenter.get(serviceName) == null || localReigstryCenter.get(serviceName).get(group) == null) {
            throw new RpcfxException("未检测到相应服务的注册信息!");
        }

        List<ServiceInfo> serviceInfoList = localReigstryCenter.get(serviceName).get(group);
        // 从zk中拉取注册信息
        ServiceInfo serviceInfo = serviceInfoList.get(serviceInfoList.size() - 1);
        // 获取注册信息最后一个服务
        url = "http://" + serviceInfo.getHost() + ":" + serviceInfo.getPort();

        // RpcClient注解的url覆盖注册中心的
        url = Optional.ofNullable(getUrl(joinPoint)).orElse(url);

        try {
            RpcfxResponse response = post(request, url);
            if (response.isStatus()) {
                return response.getResult();
            }
            throw response.getException();
        } catch (Exception ex) {
            // 降级处理
            if (fallback) {
                return joinPoint.proceed();
            }
            // 如果有自定义的异常处理器，则捕获异常进行处理
            if (rpcfxErrorHandler != null) {
                rpcfxErrorHandler.handleError(ex);
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

    /*
     * 获取方法上面的注解对象
     */
    private String getUrl(ProceedingJoinPoint joinPoint) {
        RpcClient annotation = joinPoint.getTarget().getClass().getAnnotation(RpcClient.class);
        if (annotation != null) {
            return StringUtils.isNotEmpty(annotation.url()) ? annotation.url() : null;
        }
        return null;
    }

}

