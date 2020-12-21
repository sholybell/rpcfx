package com.holybell.rpcfx.demo.provider.service;

import com.holybell.rpcfx.annotations.RpcServer;
import com.holybell.rpcfx.demo.api.model.Order;
import com.holybell.rpcfx.demo.api.service.OrderService;

/**
 * 作为一个RPC的服务端接口实现
 */
@RpcServer(value = "orderService")
public class OrderServiceImpl implements OrderService {

    @Override
    public Order findOrderById(int id) {
        if (id == 1111) {
            throw new RuntimeException("查询订单异常!");
        }
        return new Order(id, "Cuijing" + System.currentTimeMillis(), 9.9f);
    }
}
