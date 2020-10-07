package eu.opertusmundi.web.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.EnumActivationTokenType;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.ActivationTokenEntity;

@Repository
@Transactional(readOnly = true)
public interface ActivationTokenRepository extends JpaRepository<ActivationTokenEntity, Integer> {

    @Query("SELECT t FROM ActivationToken t WHERE t.token = :token and t.valid = true")
    Optional<ActivationTokenEntity> findOneByToken(UUID token);

    @Modifying
    @Query("UPDATE ActivationToken t SET t.valid = false WHERE t.redeemedAt IS NULL and t.email = :email")
    void invalidateAllTokensForEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Optional<AccountEntity> findAccountByEmail(@Param("email") String email);

    @Transactional(readOnly = false)
    default ActivationTokenDto create(Integer accountId, String email, int duration, EnumActivationTokenType type) {
        // Invalidate existing tokens
        this.invalidateAllTokensForEmail(email);

        // Create new token
        final ActivationTokenEntity token   = new ActivationTokenEntity();

        token.setAccount(accountId);
        token.setDuration(duration);
        token.setEmail(email);
        token.setType(type);
        token.setValid(true);

        this.saveAndFlush(token);

        return token.toDto();
    }

    @Transactional(readOnly = false)
    default void redeem(ActivationTokenEntity token) {
        token.setRedeemedAt(ZonedDateTime.now());

        this.saveAndFlush(token);
    }
}
