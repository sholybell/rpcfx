package com.holybell.rpcfx.demo.consumer.errorhandler;

import com.holybell.rpcfx.errorhandler.RpcfxErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义RPC异常处理器,指定特定名称异常处理器，覆盖默认异常处理器
 */
//@Component
public class CustomErrorHandler implements RpcfxErrorHandler {

    private Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public Object handleError(Throwable e) {
        if (logger.isInfoEnabled()) {
            logger.info("RpcfxErrorHandler 处理异常 自定义异常处理器", e);
        }
        return null;
    }
}
