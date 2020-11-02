package eu.opertusmundi.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.repository.AccountProfileHistoryRepository;
import eu.opertusmundi.web.repository.AccountRepository;

@Service
public class DefaultConsumerService implements ConsumerService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountProfileHistoryRepository accountProfileHistoryRepository;

    @Override
    @Transactional
    public AccountDto updateConsumer(AccountProfileConsumerCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");

        final AccountEntity accountEntity = this.accountRepository.findById(command.getId()).orElse(null);

        // Create history record
        this.accountProfileHistoryRepository.createSnapshot(accountEntity.getProfile().getId());

        // Update profile consumer data
        final AccountDto account = this.accountRepository.updateConsumer(command);

        return account;
    }

}
