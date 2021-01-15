package eu.opertusmundi.web.model.order;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderItemDto {

    @Schema(
        description = "Asset unique Id",
        example = "9b907778-6142-41c9-bc5a-0aff2398acb2"
    )
    private UUID id;

    @Schema(
        description = "Index of the specific item in the order",
        example = "5"
    )
    private int index;

    @Schema(
        description = "Item price without decimal separator i.e the value 100,50 € is returned as 10050",
        example = "10050"
    )
    private long price;

}
