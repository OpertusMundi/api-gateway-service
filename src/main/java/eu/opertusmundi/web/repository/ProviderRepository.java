package eu.opertusmundi.web.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;

@Repository
@Transactional(readOnly = true)
public interface ProviderRepository extends JpaRepository<AccountEntity, Integer> {

    @Query("SELECT distinct a FROM Account a "
            + "LEFT OUTER JOIN FETCH a.profile p "
            + "WHERE a.key in :keys")
    List<AccountEntity> findAllByKey(@Param("keys") UUID[] keys);

    @Query("SELECT a FROM Account a "
            + "LEFT OUTER JOIN FETCH a.profile p "
            + "WHERE a.key = :key")
    AccountEntity findOneByKey(@Param("key") UUID key);

}
