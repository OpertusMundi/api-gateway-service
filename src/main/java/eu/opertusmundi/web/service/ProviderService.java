package eu.opertusmundi.web.service;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;

public interface ProviderService {

    /**
     * Register or update provider data
     *
     * @param command The provider update command
     * @param submit True if the request should be submitted for review
     * @return
     */
    AccountDto updateProviderRegistration(AccountProfileProviderCommandDto command, boolean submit);

    /**
     * Accept provider registration request
     *
     * @param userId The Id of the user
     * @return
     */
    AccountDto acceptProviderRegistration(Integer userId);

    /**
     * Reject provider registration request
     *
     * @param userId The Id of the user
     * @return
     */
    AccountDto rejectProviderRegistration(Integer userId);

    /**
     * Cancel provider registration request for the user with the specified id
     *
     * @param userId The Id of the user to update
     * @return
     */
    AccountDto cancelProviderRegistration(Integer userId);

    /**
     * Complete provider registration request for the user with the specified id
     *
     * @param userId The Id of the user to update
     * @return
     */
    AccountDto completeProviderRegistration(Integer userId);

}
