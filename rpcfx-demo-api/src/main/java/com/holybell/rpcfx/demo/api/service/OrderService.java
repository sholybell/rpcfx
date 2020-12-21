package com.holybell.rpcfx.demo.api.service;

import com.holybell.rpcfx.annotations.RpcClientProxy;
import com.holybell.rpcfx.demo.api.model.Order;

@RpcClientProxy
public interface OrderService {

    Order findOrderById(int id);

}
