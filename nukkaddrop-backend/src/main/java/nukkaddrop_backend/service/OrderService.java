package nukkaddrop_backend.service;

import jakarta.transaction.Transactional;
import nukkaddrop_backend.dto.OrderItemRequest;
import nukkaddrop_backend.dto.OrderRequest;
import nukkaddrop_backend.entity.Business;
import nukkaddrop_backend.entity.Order;
import nukkaddrop_backend.entity.OrderItem;
import nukkaddrop_backend.entity.Product;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.BusinessRepository;
import nukkaddrop_backend.repository.OrderRepository;
import nukkaddrop_backend.repository.ProductRepository;
import nukkaddrop_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            BusinessRepository businessRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {

        Business business = businessRepository
                .findById(request.getBusinessId())
                .orElse(null);

        User shopkeeper = userRepository
                .findById(request.getShopkeeperId())
                .orElse(null);

        if (business == null || shopkeeper == null) {
            return null;
        }

        if (shopkeeper.getRole() != User.Role.SHOPKEEPER) {
            return null;
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return null;
        }

        Order order = new Order();

        order.setBusiness(business);
        order.setShopkeeper(shopkeeper);
        order.setStatus(Order.OrderStatus.PENDING);

        double totalAmount = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElse(null);

            if (product == null) {
                return null;
            }

            Integer quantity = itemRequest.getQuantity();

            if (quantity == null || quantity <= 0) {
                return null;
            }

            if (product.getProductStock() < quantity) {
                return null;
            }

            if (!product.getBusiness().getId().equals(business.getId())) {
                return null;
            }

            double itemTotal =
                    product.getProductPrice() * quantity;

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getProductPrice());
            orderItem.setOrder(order);

            order.getItems().add(orderItem);

            totalAmount += itemTotal;

            product.setProductStock(
                    product.getProductStock() - quantity
            );

            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }
}