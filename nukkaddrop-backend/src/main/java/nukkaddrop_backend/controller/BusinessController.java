package nukkaddrop_backend.controller;

import nukkaddrop_backend.entity.Business;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.BusinessRepository;
import nukkaddrop_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public BusinessController(
            BusinessRepository businessRepository,
            UserRepository userRepository) {

        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createBusiness(
            @RequestBody Business business,
            Authentication authentication) {

        String email = authentication.getName();

        User owner = userRepository
                .findByEmail(email)
                .orElse(null);

        if (owner == null) {
            return ResponseEntity.status(401)
                    .body("Unauthorized");
        }

        if (owner.getRole() != User.Role.BUSINESS_OWNER) {
            return ResponseEntity.status(403)
                    .body("Forbidden - only BUSINESS_OWNER can create businesses");
        }

        business.setOwner(owner);

        Business savedBusiness =
                businessRepository.save(business);

        return ResponseEntity.ok(savedBusiness);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBusiness(
            @PathVariable Long id,
            @RequestBody Business updatedBusiness,
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
                    .body("Forbidden - only BUSINESS_OWNER can update businesses");
        }

        Business business = businessRepository
                .findById(id)
                .orElse(null);

        if (business == null) {
            return ResponseEntity.notFound().build();
        }

        if (business.getOwner() == null ||
                !business.getOwner().getId()
                        .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body("Forbidden - you can only update your own businesses");
        }

        business.setBusinessName(
                updatedBusiness.getBusinessName()
        );

        business.setBusinessAddress(
                updatedBusiness.getBusinessAddress()
        );

        business.setBusinessPhone(
                updatedBusiness.getBusinessPhone()
        );

        business.setBusinessType(
                updatedBusiness.getBusinessType()
        );

        Business savedBusiness =
                businessRepository.save(business);

        return ResponseEntity.ok(savedBusiness);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBusiness(
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
                    .body("Forbidden - only BUSINESS_OWNER can delete businesses");
        }

        Business business = businessRepository
                .findById(id)
                .orElse(null);

        if (business == null) {
            return ResponseEntity.notFound().build();
        }

        if (business.getOwner() == null ||
                !business.getOwner().getId()
                        .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body("Forbidden - you can only delete your own businesses");
        }

        businessRepository.delete(business);

        return ResponseEntity.ok(
                "Business deleted successfully"
        );
    }

    @PostMapping("/{businessId}/shopkeepers/{shopkeeperId}")
    public ResponseEntity<?> addShopkeeper(
            @PathVariable Long businessId,
            @PathVariable Long shopkeeperId,
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
                    .body("Forbidden - only BUSINESS_OWNER can add shopkeepers");
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
                    .body("Forbidden - you can only manage shopkeepers for your own businesses");
        }

        User shopkeeper = userRepository
                .findById(shopkeeperId)
                .orElse(null);

        if (shopkeeper == null) {
            return ResponseEntity.notFound().build();
        }

        if (shopkeeper.getRole() != User.Role.SHOPKEEPER) {
            return ResponseEntity.status(400)
                    .body("Selected user is not a SHOPKEEPER");
        }

        if (business.getShopkeepers().contains(shopkeeper)) {
            return ResponseEntity.status(400)
                    .body("Shopkeeper is already associated with this business");
        }

        business.getShopkeepers().add(shopkeeper);

        Business savedBusiness =
                businessRepository.save(business);

        return ResponseEntity.ok(savedBusiness);
    }
}