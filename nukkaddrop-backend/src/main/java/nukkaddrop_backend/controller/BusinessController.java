package nukkaddrop_backend.controller;

import nukkaddrop_backend.entity.Business;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.BusinessRepository;
import nukkaddrop_backend.repository.UserRepository;
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
    public Business createBusiness(
            @RequestParam Long ownerId,
            @RequestBody Business business) {

        User owner = userRepository.findById(ownerId).orElse(null);

        if (owner == null) {
            return null;
        }

        business.setOwner(owner);

        return businessRepository.save(business);
    }

    @PutMapping("/{id}")
    public Business updateBusiness(
            @PathVariable Long id,
            @RequestBody Business updatedBusiness) {

        return businessRepository.findById(id)
                .map(business -> {

                    business.setBusinessName(updatedBusiness.getBusinessName());
                    business.setBusinessAddress(updatedBusiness.getBusinessAddress());
                    business.setBusinessPhone(updatedBusiness.getBusinessPhone());
                    business.setBusinessType(updatedBusiness.getBusinessType());

                    return businessRepository.save(business);
                })
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public String deleteBusiness(@PathVariable Long id) {

        if (businessRepository.existsById(id)) {
            businessRepository.deleteById(id);
            return "Business deleted successfully";
        }

        return "Business not found";
    }
}