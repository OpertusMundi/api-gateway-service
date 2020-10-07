package eu.opertusmundi.web.model.email;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MailActivationModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private UUID token;

    private String url;

}
