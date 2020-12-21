package com.holybell.rpcfx.demo.consumer.service;

import com.holybell.rpcfx.annotations.RpcClient;
import com.holybell.rpcfx.demo.api.model.Order;
import com.holybell.rpcfx.demo.api.service.OrderService;
import org.springframework.stereotype.Component;

@Component
@RpcClient(url = "http://localhost:8081/")
public class OrderServiceImpl implements OrderService {

    /**
     * 可以作为降级方法
     */
    @Override
    public Order findOrderById(int id) {
        return new Order();
    }
}
