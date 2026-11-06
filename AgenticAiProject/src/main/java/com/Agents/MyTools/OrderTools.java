package com.Agents.MyTools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.Agents.Entity.Order;
import com.Agents.OrderRepository.OrderRepository;

@Component
public class OrderTools {

    private final OrderRepository orderRepository;

    public OrderTools(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Tool(description = "Get the status of an order using the order ID")
    public String getOrderStatus(
            @ToolParam(description = "The unique order ID string, e.g., ORD-1234") String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "Order with ID " + orderId + " was not found.";
        }
        return "Order " + order.getOrderId() + " for " + order.getItem() + " is currently: " + order.getStatus();
    }

    @Tool(description = "Cancel an order using the order ID")
    public String cancelOrder(
            @ToolParam(description = "The unique order ID string of the order to cancel") String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "Cannot cancel. Order " + orderId + " not found.";
        }
        if ("SHIPPED".equalsIgnoreCase(order.getStatus())) {
            return "Order " + orderId + " has already shipped and cannot be cancelled.";
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return "Order " + orderId + " has been successfully cancelled.";
    }

    @Tool(description = "Create a new order with the given item and customer name")
    public String createOrder(
            @ToolParam(description = "The name of the customer placing the order") String customerName,
            @ToolParam(description = "The item being ordered") String item) {
        String orderId = "ORD-" + (int)(Math.random() * 9000 + 1000);
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerName(customerName);
        order.setItem(item);
        order.setStatus("PROCESSING");
        orderRepository.save(order);
        return "Successfully created order " + orderId + " for " + customerName + " (" + item + ").";
    }
  
    @Tool(description = "Update the status of an order using the order ID and new status")
    public String updateOrderStatus(
            @ToolParam(description = "The unique order ID string to update") String orderId,
            @ToolParam(description = "The new status to set, e.g., SHIPPED, DELIVERED") String newStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return "Order " + orderId + " not found.";
        order.setStatus(newStatus.toUpperCase());
        orderRepository.save(order);
        return "Order " + orderId + " status successfully updated to " + order.getStatus();
    }
}