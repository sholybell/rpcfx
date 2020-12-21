package com.holybell.rpcfx.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultRpxfxErrorHandler implements RpcfxErrorHandler {

    private Logger logger = LoggerFactory.getLogger(DefaultRpxfxErrorHandler.class);

    /**
     * 默认的异常处理器仅做日志打印
     */
    @Override
    public Object handleError(Throwable throwable) {
        logger.info("rpxfx default error handler print log : {}", throwable);
        return null;
    }
}
