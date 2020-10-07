package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumRole;

@Entity(name = "AccountRole")
@Table(
    schema = "web", name = "`account_role`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_role", columnNames = {"`account`", "`role`"})
    })
public class AccountRoleEntity {

    @Id()
    @Column(name = "`id`")
    @SequenceGenerator(sequenceName = "web.account_role_id_seq", name = "account_role_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_role_id_seq", strategy = GenerationType.SEQUENCE)
    int           id;

    @NotNull
    @ManyToOne(targetEntity=AccountEntity.class)
    @JoinColumn(name = "account", nullable = false)
    AccountEntity account;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`role`", nullable = false)
    EnumRole      role;

    @Column(name = "granted_at", insertable = false)
    ZonedDateTime grantedAt;

    @ManyToOne(targetEntity=AccountEntity.class,fetch = FetchType.LAZY)
    @JoinColumn(name = "`granted_by`")
    AccountEntity grantedBy;

    AccountRoleEntity() {
    }

    public AccountRoleEntity(AccountEntity account, EnumRole role) {
        this(account, role, null, null);
    }

    public AccountRoleEntity(AccountEntity account, EnumRole role, ZonedDateTime grantedAt, AccountEntity grantedBy) {
        this.account = account;
        this.role = role;
        this.grantedAt = grantedAt;
        this.grantedBy = grantedBy;
    }

    public AccountEntity getAccount() {
        return this.account;
    }

    public EnumRole getRole() {
        return this.role;
    }

    public ZonedDateTime getGrantedAt() {
        return this.grantedAt;
    }

    public AccountEntity getGrantedBy() {
        return this.grantedBy;
    }
}
