package com.holybell.rpcfx.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component // 使注解具有Spring @Component 特性
public @interface RpcServer {

    String value() default "";
    /**
     * 分组
     */
    String group() default "default-group";

    /**
     * 版本
     */
    String version() default "1.0";
}
