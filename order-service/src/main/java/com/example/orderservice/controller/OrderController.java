package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-service")
public class OrderController {

    private final Environment env;
    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrders(@PathVariable("userId") String userId) {
        Iterable<OrderEntity> orderEntities = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();
        orderEntities.forEach(o -> result.add(modelMapper.map(o, ResponseOrder.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder orderDetails) {


        OrderDto orderDto = modelMapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /* jpa */
//        OrderDto createdOrder = orderService.createOrder(orderDto);
//        ResponseOrder responseOrder = modelMapper.map(createdOrder, ResponseOrder.class);

        /* kafka */
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());

        /* Send an order to the Kafka */
        kafkaProducer.sendOrder("example-order-topic", orderDto);
        orderProducer.send("orders", orderDto);

        ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ResponseOrder> getOrder(@PathVariable("orderId") String orderId) {
        OrderDto orderDto = orderService.getOrderByOrderId(orderId);
        ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseOrder);
    }



    @GetMapping("/health-check")
    public String status() { return String.format("It's working in Order Service on PORT %s", env.getProperty("local.server.port")); }
}
