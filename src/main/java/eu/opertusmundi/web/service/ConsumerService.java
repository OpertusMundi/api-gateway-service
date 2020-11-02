package eu.opertusmundi.web.service;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;

public interface ConsumerService {

    /**
     * Update consumer data
     *
     * @param command The consumer update command
     * @return
     */
    AccountDto updateConsumer(AccountProfileConsumerCommandDto command);

}
