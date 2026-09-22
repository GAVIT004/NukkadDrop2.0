package nukkaddrop_backend.controller;

import nukkaddrop_backend.dto.OrderRequest;
import nukkaddrop_backend.entity.Order;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.OrderRepository;
import nukkaddrop_backend.repository.UserRepository;
import nukkaddrop_backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        this.userRepository = userRepository;
    }

    // GET ALL ORDERS
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // GET ORDER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id) {

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    // CREATE ORDER
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User shopkeeper = userRepository
                .findByEmail(email)
                .orElse(null);

        if (shopkeeper == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (shopkeeper.getRole() != User.Role.SHOPKEEPER) {
            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - only SHOPKEEPER can create orders"
                    );
        }

        Order order =
                orderService.createOrder(
                        request,
                        shopkeeper
                );

        if (order == null) {

            return ResponseEntity.status(400)
                    .body(
                            "Unable to create order - verify business, shopkeeper association, products, quantities and stock"
                    );
        }

        return ResponseEntity.ok(order);
    }

    // UPDATE ORDER STATUS
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Order.OrderStatus currentStatus =
                order.getStatus();

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

            return ResponseEntity.badRequest()
                    .body(
                            "Invalid order status transition"
                    );
        }

        order.setStatus(status);

        return ResponseEntity.ok(
                orderRepository.save(order)
        );
    }

    // ASSIGN DELIVERY PARTNER
    @PutMapping("/{orderId}/delivery-partner/{deliveryPartnerId}")
    public ResponseEntity<?> assignDeliveryPartner(
            @PathVariable Long orderId,
            @PathVariable Long deliveryPartnerId) {

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        User deliveryPartner = userRepository
                .findById(deliveryPartnerId)
                .orElse(null);

        if (deliveryPartner == null) {
            return ResponseEntity.notFound().build();
        }

        if (deliveryPartner.getRole()
                != User.Role.DELIVERY_PARTNER) {

            return ResponseEntity.badRequest()
                    .body(
                            "Selected user is not a DELIVERY_PARTNER"
                    );
        }

        if (order.getStatus()
                != Order.OrderStatus.READY_FOR_DELIVERY) {

            return ResponseEntity.badRequest()
                    .body(
                            "Order must be READY_FOR_DELIVERY before assigning a delivery partner"
                    );
        }

        order.setDeliveryPartner(deliveryPartner);

        order.setStatus(
                Order.OrderStatus.OUT_FOR_DELIVERY
        );

        return ResponseEntity.ok(
                orderRepository.save(order)
        );
    }

    // MARK ORDER DELIVERED
    @PutMapping("/{orderId}/delivered/{deliveryPartnerId}")
    public ResponseEntity<?> markOrderDelivered(
            @PathVariable Long orderId,
            @PathVariable Long deliveryPartnerId) {

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        User deliveryPartner = userRepository
                .findById(deliveryPartnerId)
                .orElse(null);

        if (deliveryPartner == null) {
            return ResponseEntity.notFound().build();
        }

        if (deliveryPartner.getRole()
                != User.Role.DELIVERY_PARTNER) {

            return ResponseEntity.badRequest()
                    .body(
                            "Selected user is not a DELIVERY_PARTNER"
                    );
        }

        if (order.getStatus()
                != Order.OrderStatus.OUT_FOR_DELIVERY) {

            return ResponseEntity.badRequest()
                    .body(
                            "Order must be OUT_FOR_DELIVERY before marking it delivered"
                    );
        }

        if (order.getDeliveryPartner() == null ||
                !order.getDeliveryPartner()
                        .getId()
                        .equals(deliveryPartnerId)) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - this order is assigned to a different delivery partner"
                    );
        }

        order.setStatus(
                Order.OrderStatus.DELIVERED
        );

        return ResponseEntity.ok(
                orderRepository.save(order)
        );
    }

    // DELETE ORDER
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(
            @PathVariable Long id) {

        if (!orderRepository.existsById(id)) {

            return ResponseEntity.notFound().build();
        }

        orderRepository.deleteById(id);

        return ResponseEntity.ok(
                "Order deleted successfully"
        );
    }
}