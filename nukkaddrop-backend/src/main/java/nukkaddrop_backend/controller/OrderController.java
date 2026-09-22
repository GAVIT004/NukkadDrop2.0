package nukkaddrop_backend.controller;

import nukkaddrop_backend.dto.OrderRequest;
import nukkaddrop_backend.entity.Business;
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
    public ResponseEntity<?> getAllOrders(
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        List<Order> allOrders =
                orderRepository.findAll();

        if (loggedInUser.getRole() == User.Role.ADMIN) {
            return ResponseEntity.ok(allOrders);
        }

        if (loggedInUser.getRole()
                == User.Role.BUSINESS_OWNER) {

            List<Order> ownerOrders =
                    allOrders.stream()
                            .filter(order ->
                                    order.getBusiness() != null &&
                                            order.getBusiness().getOwner() != null &&
                                            order.getBusiness()
                                                    .getOwner()
                                                    .getId()
                                                    .equals(
                                                            loggedInUser.getId()
                                                    )
                            )
                            .toList();

            return ResponseEntity.ok(ownerOrders);
        }

        if (loggedInUser.getRole()
                == User.Role.SHOPKEEPER) {

            List<Order> shopkeeperOrders =
                    allOrders.stream()
                            .filter(order ->
                                    order.getShopkeeper() != null &&
                                            order.getShopkeeper()
                                                    .getId()
                                                    .equals(
                                                            loggedInUser.getId()
                                                    )
                            )
                            .toList();

            return ResponseEntity.ok(shopkeeperOrders);
        }

        if (loggedInUser.getRole()
                == User.Role.DELIVERY_PARTNER) {

            List<Order> deliveryOrders =
                    allOrders.stream()
                            .filter(order ->
                                    order.getDeliveryPartner() != null &&
                                            order.getDeliveryPartner()
                                                    .getId()
                                                    .equals(
                                                            loggedInUser.getId()
                                                    )
                            )
                            .toList();

            return ResponseEntity.ok(deliveryOrders);
        }

        return ResponseEntity.status(403)
                .body("Forbidden");
    }

    // GET ORDER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        if (loggedInUser.getRole() == User.Role.ADMIN) {
            return ResponseEntity.ok(order);
        }

        if (loggedInUser.getRole()
                == User.Role.BUSINESS_OWNER) {

            if (order.getBusiness() != null &&
                    order.getBusiness().getOwner() != null &&
                    order.getBusiness()
                            .getOwner()
                            .getId()
                            .equals(loggedInUser.getId())) {

                return ResponseEntity.ok(order);
            }

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only view orders belonging to your own business"
                    );
        }

        if (loggedInUser.getRole()
                == User.Role.SHOPKEEPER) {

            if (order.getShopkeeper() != null &&
                    order.getShopkeeper()
                            .getId()
                            .equals(loggedInUser.getId())) {

                return ResponseEntity.ok(order);
            }

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only view your own orders"
                    );
        }

        if (loggedInUser.getRole()
                == User.Role.DELIVERY_PARTNER) {

            if (order.getDeliveryPartner() != null &&
                    order.getDeliveryPartner()
                            .getId()
                            .equals(loggedInUser.getId())) {

                return ResponseEntity.ok(order);
            }

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only view orders assigned to you"
                    );
        }

        return ResponseEntity.status(403)
                .body("Forbidden");
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
            @RequestParam Order.OrderStatus status,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (loggedInUser.getRole()
                != User.Role.BUSINESS_OWNER) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - only BUSINESS_OWNER can update order status"
                    );
        }

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Business business = order.getBusiness();

        if (business == null) {
            return ResponseEntity.status(400)
                    .body(
                            "Order is not associated with a business"
                    );
        }

        if (business.getOwner() == null) {
            return ResponseEntity.status(400)
                    .body(
                            "Business does not have an owner"
                    );
        }

        if (!business.getOwner()
                .getId()
                .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only update orders belonging to your own business"
                    );
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

            if (status ==
                    Order.OrderStatus.READY_FOR_DELIVERY) {

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
            @PathVariable Long deliveryPartnerId,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (loggedInUser.getRole()
                != User.Role.BUSINESS_OWNER) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - only BUSINESS_OWNER can assign delivery partners"
                    );
        }

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Business business = order.getBusiness();

        if (business == null) {
            return ResponseEntity.status(400)
                    .body(
                            "Order is not associated with a business"
                    );
        }

        if (business.getOwner() == null) {
            return ResponseEntity.status(400)
                    .body(
                            "Business does not have an owner"
                    );
        }

        if (!business.getOwner()
                .getId()
                .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only assign delivery partners to orders belonging to your own business"
                    );
        }

        if (order.getStatus()
                != Order.OrderStatus.READY_FOR_DELIVERY) {

            return ResponseEntity.badRequest()
                    .body(
                            "Order must be READY_FOR_DELIVERY before assigning a delivery partner"
                    );
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
            @PathVariable Long deliveryPartnerId,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (loggedInUser.getRole()
                != User.Role.DELIVERY_PARTNER) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - only DELIVERY_PARTNER can mark orders delivered"
                    );
        }

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        if (order.getStatus()
                != Order.OrderStatus.OUT_FOR_DELIVERY) {

            return ResponseEntity.badRequest()
                    .body(
                            "Order must be OUT_FOR_DELIVERY before marking it delivered"
                    );
        }

        if (!loggedInUser.getId()
                .equals(deliveryPartnerId)) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only mark orders delivered for yourself"
                    );
        }

        if (order.getDeliveryPartner() == null ||
                !order.getDeliveryPartner()
                        .getId()
                        .equals(loggedInUser.getId())) {

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
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (loggedInUser.getRole()
                != User.Role.BUSINESS_OWNER) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - only BUSINESS_OWNER can manage business orders"
                    );
        }

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Business business = order.getBusiness();

        if (business == null ||
                business.getOwner() == null) {

            return ResponseEntity.status(400)
                    .body(
                            "Order is not associated with a valid business owner"
                    );
        }

        if (!business.getOwner()
                .getId()
                .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "Forbidden - you can only manage orders belonging to your own business"
                    );
        }

        // Orders should not be physically deleted once
        // they have entered the business workflow.
        return ResponseEntity.status(400)
                .body(
                        "Orders cannot be deleted. Use the allowed cancellation workflow instead."
                );
    }
}