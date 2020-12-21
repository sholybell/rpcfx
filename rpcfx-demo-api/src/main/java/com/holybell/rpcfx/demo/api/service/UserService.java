package com.holybell.rpcfx.demo.api.service;

import com.holybell.rpcfx.annotations.RpcClientProxy;
import com.holybell.rpcfx.demo.api.model.User;

@RpcClientProxy
public interface UserService {

    User findById(int id);

}
