package nukkaddrop_backend.dto;

import java.util.List;

public class OrderRequest {

    private Long businessId;
    private List<OrderItemRequest> items;

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}