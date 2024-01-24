package br.fiap.pos.fastfood.preparation.controllers;

import br.fiap.pos.fastfood.preparation.domain.Order;
import br.fiap.pos.fastfood.preparation.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/create")
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("Preparation");
        return orderRepository.save(order);
    }

    @PutMapping("/update/{id}")
    public Order updateOrder(@PathVariable String id, @RequestBody Order order) {
        order.setId(id);
        return orderRepository.save(order);
    }

    @GetMapping("/list")
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }
}
