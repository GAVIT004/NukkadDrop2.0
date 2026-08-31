package nukkaddrop_backend.controller;

import nukkaddrop_backend.entity.Business;
import nukkaddrop_backend.entity.Product;
import nukkaddrop_backend.repository.BusinessRepository;
import nukkaddrop_backend.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;

    public ProductController(
            ProductRepository productRepository,
            BusinessRepository businessRepository) {

        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElse(null);
    }

    @PostMapping
    public Product createProduct(
            @RequestParam Long businessId,
            @RequestBody Product product) {

        Business business = businessRepository.findById(businessId)
                .orElse(null);

        if (business == null) {
            return null;
        }

        product.setBusiness(business);

        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {

        return productRepository.findById(id)
                .map(product -> {

                    product.setProductName(
                            updatedProduct.getProductName());

                    product.setProductPrice(
                            updatedProduct.getProductPrice());

                    product.setProductDescription(
                            updatedProduct.getProductDescription());

                    product.setProductCategory(
                            updatedProduct.getProductCategory());

                    product.setProductStock(
                            updatedProduct.getProductStock());

                    return productRepository.save(product);
                })
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {

        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return "Product deleted successfully";
        }

        return "Product not found";
    }
}