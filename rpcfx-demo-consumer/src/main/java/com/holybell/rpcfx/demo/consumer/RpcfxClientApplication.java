package com.holybell.rpcfx.demo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class RpcfxClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcfxClientApplication.class, args);
    }
}
