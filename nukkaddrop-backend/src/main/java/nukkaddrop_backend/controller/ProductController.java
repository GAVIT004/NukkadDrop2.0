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

    public ProductController(ProductRepository productRepository,BusinessRepository businessRepository){
        this.productRepository=productRepository;
        this.businessRepository=businessRepository;
    }

    @GetMapping
    public List<Product>getAllProducts(){
        return productRepository.findAll();
    }

    @PostMapping
    public Product createProduct(@RequestParam Long businessId,@RequestBody Product product){

        Business business=businessRepository.findById(businessId).orElse(null);

        if (business==null){
            return null;
        }

        product.setBusiness(business);
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id,@RequestBody Product updateProduct){

        return productRepository.findById(id).map(product -> {

            product.setProductName(updateProduct.getProductName());
            product.setProductPrice(updateProduct.getProductPrice());
            product.setProductDescription(updateProduct.getProductDescription());
            product.setProductCategory(updateProduct.getProductCategory());
            product.setProductStock(updateProduct.getProductStock());

            return productRepository.save(product);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id){

        if (productRepository.existsById(id)){
            productRepository.deleteById(id);
            return "Product deleted successfully";
        }

        return "product not found";
    }
}
