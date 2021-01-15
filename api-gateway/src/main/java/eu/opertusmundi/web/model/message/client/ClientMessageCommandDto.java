package eu.opertusmundi.web.model.message.client;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ClientMessageCommandDto {

    @NotEmpty
    @Getter
    @Setter
    private String text;

}
