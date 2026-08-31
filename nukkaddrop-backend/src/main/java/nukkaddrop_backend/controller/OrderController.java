package nukkaddrop_backend.controller;

import nukkaddrop_backend.dto.OrderRequest;
import nukkaddrop_backend.entity.Order;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.OrderRepository;
import nukkaddrop_backend.repository.UserRepository;
import nukkaddrop_backend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(
            OrderRepository orderRepository,
            OrderService orderService,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.userRepository=userRepository;
    }

    // GET ALL ORDERS
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // GET ORDER BY ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .orElse(null);
    }

    // CREATE ORDER
    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    // UPDATE ORDER STATUS
    @PutMapping("/{id}/status")
    public Order updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {

        return orderRepository.findById(id)
                .map(order -> {

                    Order.OrderStatus currentStatus = order.getStatus();

                    boolean validTransition = false;

                    if (currentStatus == Order.OrderStatus.PENDING) {

                        if (status == Order.OrderStatus.ACCEPTED ||
                                status == Order.OrderStatus.CANCELLED) {
                            validTransition = true;
                        }

                    } else if (currentStatus == Order.OrderStatus.ACCEPTED) {

                        if (status == Order.OrderStatus.PREPARING ||
                                status == Order.OrderStatus.CANCELLED) {
                            validTransition = true;
                        }

                    } else if (currentStatus == Order.OrderStatus.PREPARING) {

                        if (status == Order.OrderStatus.READY_FOR_DELIVERY) {
                            validTransition = true;
                        }
                    }

                    if (!validTransition) {
                        return null;
                    }

                    order.setStatus(status);

                    return orderRepository.save(order);
                })
                .orElse(null);
    }

    @PutMapping("/{orderId}/delivery-partner/{deliveryPartnerId}")
    public Order assignDeliveryPartner(
            @PathVariable Long orderId,
            @PathVariable Long deliveryPartnerId) {

        Order order = orderRepository.findById(orderId)
                .orElse(null);

        User deliveryPartner = userRepository
                .findById(deliveryPartnerId)
                .orElse(null);

        if (order == null || deliveryPartner == null) {
            return null;
        }

        if (deliveryPartner.getRole() != User.Role.DELIVERY_PARTNER) {
            return null;
        }

        if (order.getStatus() != Order.OrderStatus.READY_FOR_DELIVERY) {
            return null;
        }

        order.setDeliveryPartner(deliveryPartner);
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);

        return orderRepository.save(order);
    }

    @PutMapping("/{orderId}/delivered/{deliveryPartnerId}")
    public Order markOrderDelivered(
            @PathVariable Long orderId,
            @PathVariable Long deliveryPartnerId) {

        Order order = orderRepository.findById(orderId)
                .orElse(null);

        User deliveryPartner = userRepository.findById(deliveryPartnerId)
                .orElse(null);

        if (order == null || deliveryPartner == null) {
            return null;
        }

        if (deliveryPartner.getRole() != User.Role.DELIVERY_PARTNER) {
            return null;
        }

        if (order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            return null;
        }

        if (order.getDeliveryPartner() == null ||
                !order.getDeliveryPartner().getId().equals(deliveryPartnerId)) {
            return null;
        }

        order.setStatus(Order.OrderStatus.DELIVERED);

        return orderRepository.save(order);
    }

    // DELETE ORDER
    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {

        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return "Order deleted successfully";
        }

        return "Order not found";
    }
}