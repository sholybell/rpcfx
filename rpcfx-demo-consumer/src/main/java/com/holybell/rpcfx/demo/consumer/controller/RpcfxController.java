package com.holybell.rpcfx.demo.consumer.controller;

import com.holybell.rpcfx.demo.api.model.Order;
import com.holybell.rpcfx.demo.api.model.User;
import com.holybell.rpcfx.demo.api.service.OrderService;
import com.holybell.rpcfx.demo.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class RpcfxController {

    private static final Logger logger = LoggerFactory.getLogger(RpcfxController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    @GetMapping("rpcCall")
    public void rpcCall() {
        User user = userService.findById(1);
        if (logger.isInfoEnabled()) {
            logger.info("find user id=1 from server : {}", user.getName());
        }

        user = userService.findById(1111);
        if (logger.isInfoEnabled()) {
            logger.info("find user id=1 from server : {}", Optional.ofNullable(user).orElse(new User()));
        }

        Order order = orderService.findOrderById(1992129);
        if (logger.isInfoEnabled()) {
            logger.info("find order name={}, amount={}", order.getName(), order.getAmount());
        }
    }
}
