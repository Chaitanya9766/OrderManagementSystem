package com.Agents.OrderRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Agents.Entity.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
}
