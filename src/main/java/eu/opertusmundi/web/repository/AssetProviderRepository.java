package eu.opertusmundi.web.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.web.domain.AccountEntity;

@Repository
@Transactional(readOnly = true)
public interface AssetProviderRepository extends JpaRepository<AccountEntity, Integer> {

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p LEFT OUTER JOIN FETCH p.addresses addr WHERE a.key in :keys")
    List<AccountEntity> findAllByKey(@Param("keys") UUID[] keys);

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p LEFT OUTER JOIN FETCH p.addresses addr WHERE a.key = :key")
    AccountEntity findOneByKey(@Param("key") UUID key);

}
