package eu.opertusmundi.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.AccountProfileEntity;
import eu.opertusmundi.web.domain.AccountProfileHistoryEntity;

@Repository
@Transactional(readOnly = true)
public interface AccountProfileHistoryRepository extends JpaRepository<AccountProfileHistoryEntity, Integer> {

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p WHERE a.id = :id")
    Optional<AccountEntity> findAccountById(@Param("id") Integer id);

    @Transactional(readOnly = false)
    default void createSnapshot(Integer userId) {
        final AccountEntity        account = this.findAccountById(userId).orElse(null);
        final AccountProfileEntity profile = account.getProfile();

        if (profile == null) {
            return;
        }

        final AccountProfileHistoryEntity history = new AccountProfileHistoryEntity(profile);

        this.save(history);
    }

}
