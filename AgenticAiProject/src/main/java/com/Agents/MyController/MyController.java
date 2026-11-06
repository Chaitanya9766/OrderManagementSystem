package com.Agents.MyController;

import com.Agents.Service.OrderService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class MyController {

    private final OrderService orderService;

    public MyController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/chat")
    public String chat(@RequestParam String message, @RequestParam(required = false, defaultValue = "default-user") String userId) {
        return orderService.processChat(message);
    }
}