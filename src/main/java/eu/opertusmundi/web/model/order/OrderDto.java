package eu.opertusmundi.web.model.order;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderDto {

    @Schema(
        description = "Order unique Id",
        example = "53dd19d0-7498-40bc-8632-7ab125c73808"
    )
    private UUID id;

    private List<OrderItemDto> items;

}
