package eu.opertusmundi.web.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.model.account.SimpleAccountDto;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountMapDto {

    @JsonIgnore
    private Integer id;

    private ZonedDateTime createdAt;
    private UUID          key;
    private String        mapUrl;
    private String        thumbnailUrl;
    private String        title;
    private ZonedDateTime updatedAt;

    @Hidden
    @JsonInclude(Include.NON_NULL)
    private SimpleAccountDto account;

    @Hidden
    @JsonInclude(Include.NON_NULL)
    private ObjectNode attributes;

}
