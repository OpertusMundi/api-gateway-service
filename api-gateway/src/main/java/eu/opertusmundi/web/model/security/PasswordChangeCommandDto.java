package eu.opertusmundi.web.model.security;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private String userName;
    
    @NotEmpty
    @Schema(description = "Current password of the authenticated user")
    private String currentPassword;

    @NotEmpty
    @Schema(description = "New password")
    private String newPassword;

    @NotEmpty
    @Schema(description = "New password verification. Must match property `newPassword`")
    private String verifyNewPassword;

}
