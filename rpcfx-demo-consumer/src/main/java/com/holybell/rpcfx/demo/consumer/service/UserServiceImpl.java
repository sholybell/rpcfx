package com.holybell.rpcfx.demo.consumer.service;

import com.holybell.rpcfx.annotations.RpcClient;
import com.holybell.rpcfx.demo.api.model.User;
import com.holybell.rpcfx.demo.api.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RpcClient(url = "http://localhost:8081/")
public class UserServiceImpl implements UserService {

    /**
     * 可以作为降级方法
     */
    @Override
    public User findById(int id) {
        return new User();
    }
}
