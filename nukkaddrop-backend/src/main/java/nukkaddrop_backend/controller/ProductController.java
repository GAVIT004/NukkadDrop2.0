package nukkaddrop_backend.controller;

import nukkaddrop_backend.entity.Business;
import nukkaddrop_backend.entity.Product;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.BusinessRepository;
import nukkaddrop_backend.repository.ProductRepository;
import nukkaddrop_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public ProductController(
            ProductRepository productRepository,
            BusinessRepository businessRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable Long id) {

        Product product = productRepository
                .findById(id)
                .orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam Long businessId,
            @RequestBody Product product,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (loggedInUser.getRole() != User.Role.BUSINESS_OWNER) {
            return ResponseEntity.status(403)
                    .body("Forbidden - only BUSINESS_OWNER can create products");
        }

        Business business = businessRepository
                .findById(businessId)
                .orElse(null);

        if (business == null) {
            return ResponseEntity.notFound().build();
        }

        if (business.getOwner() == null ||
                !business.getOwner().getId()
                        .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body("Forbidden - you can only create products for your own businesses");
        }

        product.setBusiness(business);

        Product savedProduct =
                productRepository.save(product);

        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct,
            Authentication authentication) {

        String email = authentication.getName();

        User loggedInUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (loggedInUser.getRole() != User.Role.BUSINESS_OWNER) {
            return ResponseEntity.status(403)
                    .body("Forbidden - only BUSINESS_OWNER can update products");
        }

        Product product = productRepository
                .findById(id)
                .orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        Business business = product.getBusiness();

        if (business == null ||
                business.getOwner() == null ||
                !business.getOwner().getId()
                        .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body("Forbidden - you can only update products from your own businesses");
        }

        product.setProductName(
                updatedProduct.getProductName()
        );

        product.setProductPrice(
                updatedProduct.getProductPrice()
        );

        product.setProductDescription(
                updatedProduct.getProductDescription()
        );

        product.setProductCategory(
                updatedProduct.getProductCategory()
        );

        product.setProductStock(
                updatedProduct.getProductStock()
        );

        Product savedProduct =
                productRepository.save(product);

        return ResponseEntity.ok(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
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

        if (loggedInUser.getRole() != User.Role.BUSINESS_OWNER) {
            return ResponseEntity.status(403)
                    .body("Forbidden - only BUSINESS_OWNER can delete products");
        }

        Product product = productRepository
                .findById(id)
                .orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        Business business = product.getBusiness();

        if (business == null ||
                business.getOwner() == null ||
                !business.getOwner().getId()
                        .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body("Forbidden - you can only delete products from your own businesses");
        }

        productRepository.delete(product);

        return ResponseEntity.ok(
                "Product deleted successfully"
        );
    }
}