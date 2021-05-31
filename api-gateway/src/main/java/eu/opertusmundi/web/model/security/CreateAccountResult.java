package eu.opertusmundi.web.model.security;

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class CreateAccountResult {

    private final AccountDto account;

    private final ActivationTokenDto token;

}
