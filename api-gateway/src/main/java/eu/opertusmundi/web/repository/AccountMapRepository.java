package eu.opertusmundi.web.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.web.domain.AccountMapEntity;
import eu.opertusmundi.web.model.AccountMapCommand;
import eu.opertusmundi.web.model.AccountMapDto;

@Repository
@Transactional(readOnly = true)
public interface AccountMapRepository extends JpaRepository<AccountMapEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("""
        SELECT  m
        FROM    AccountMap m LEFT OUTER JOIN m.account a
        WHERE   (cast(:ownerKey as org.hibernate.type.UUIDCharType) IS NULL or a.key = :ownerKey)
    """)
    Page<AccountMapEntity> findAll(UUID ownerKey, Pageable pageable);

    @Query("""
        SELECT  m
        FROM    AccountMap m
        WHERE   (m.account.key = :ownerKey) and (m.key = :mapKey)
    """ )
    Optional<AccountMapEntity> findOneByKey(UUID ownerKey, UUID mapKey);


    @Query("""
        SELECT  m
        FROM    AccountMap m
        WHERE   (m.account.key = :ownerKey) and (m.mapUrl = :mapUrl)
    """ )
    Optional<AccountMapEntity> findOneByUrl(UUID ownerKey, String mapUrl);

    @Transactional(readOnly = false)
    default AccountMapDto create(AccountMapCommand command) {
        final var owner = this.findAccountByKey(command.getUserKey()).get();
        final var map   = this.findOneByUrl(command.getUserKey(), command.getMapUrl()).orElse(new AccountMapEntity());

        map.setAccount(owner);
        map.setAttributes(command.getAttributes());
        if (map.getCreatedAt() == null) {
            map.setCreatedAt(command.getCreatedAt());
        }
        map.setMapUrl(command.getMapUrl());
        map.setThumbnailUrl(command.getThumbnailUrl());
        map.setTitle(command.getTitle());
        map.setUpdatedAt(command.getCreatedAt());

        this.saveAndFlush(map);

        return map.toDto();
    }

}
