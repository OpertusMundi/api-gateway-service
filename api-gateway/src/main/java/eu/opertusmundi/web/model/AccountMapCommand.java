package eu.opertusmundi.web.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonIgnoreType
public class AccountMapCommand {

    private ObjectNode    attributes;
    private ZonedDateTime createdAt;
    private String        mapUrl;
    private String        thumbnailUrl;
    private String        title;
    private UUID          userKey;

}
