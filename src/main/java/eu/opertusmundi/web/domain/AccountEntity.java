package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;

import eu.opertusmundi.common.model.EnumActivationStatus;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.common.model.dto.PublisherDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Account")
@Table(schema = "web", name = "`account`", uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_username", columnNames = {"`username`"}),
        @UniqueConstraint(name = "uq_account_email", columnNames = {"`email`"}),
        @UniqueConstraint(name = "uq_account_key", columnNames = {"`key`"}),
    }
)
public class AccountEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_id_seq", name = "account_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @OneToOne(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private AccountProfileEntity profile;

    @NotNull
    @Column(name = "`username`", nullable = false, length = 120)
    @Getter
    @Setter
    String username;

    @Column(name = "`active`")
    @Getter
    @Setter
    boolean active = true;

    @Column(name = "`blocked`")
    @Getter
    @Setter
    boolean blocked = false;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    @Getter
    @Setter
    String email;

    @Column(name = "`email_verified`")
    @Getter
    @Setter
    boolean emailVerified = false;

    @Column(name = "`email_verified_at`")
    @Getter
    @Setter
    ZonedDateTime emailVerifiedAt;

    @Column(name = "`firstname`", length = 64)
    @Getter
    @Setter
    String firstName;

    @Column(name = "`lastname`", length = 64)
    @Getter
    @Setter
    String lastName;

    @NotNull
    @Pattern(regexp = "[a-z][a-z]")
    @Column(name = "`locale`")
    @Getter
    @Setter
    String locale;

    @Column(name = "`password`")
    @Getter
    @Setter
    String password;

    @Column(name = "`registered_at`", nullable = false)
    @Getter
    ZonedDateTime registeredAt = ZonedDateTime.now();

    @NotNull
    @Column(name = "`activation_status`", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    EnumActivationStatus activationStatus;

    @Column(name = "`activation_at`")
    @Getter
    @Setter
    ZonedDateTime activatedAt;

    @Column(name = "`idp_name`", length = 20)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    EnumAuthProvider idpName;

    @Column(name = "`idp_user_alias`", length = 120)
    @Getter
    @Setter
    String idpUserAlias;

    @Column(name = "`idp_user_image`")
    @Getter
    @Setter
    String idpUserImage;

    @OneToMany(
        targetEntity = AccountRoleEntity.class,
        mappedBy = "account",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    List<AccountRoleEntity> roles = new ArrayList<>();

    @Transient
    public String getFullName() {
        if (!StringUtils.isBlank(this.firstName)) {
            if (!StringUtils.isBlank(this.lastName)) {
                return this.firstName + " " + this.lastName;
            }
            return this.firstName;
        }
        return "";
    }

    public AccountEntity() {
    }

    public AccountEntity(int id) {
        this.id = id;
    }

    public AccountEntity(String username, String email) {
        this.username = username;
        this.email    = email;
    }

    public void setName(String firstname, String lastname) {
        this.firstName = firstname;
        this.lastName  = lastname;
    }

    public boolean hasRole(EnumRole role) {
        for (final AccountRoleEntity ar : this.roles) {
            if (role == ar.role) {
                return true;
            }
        }
        return false;
    }

    public void grant(EnumRole role, AccountEntity grantedBy) {
        if (!this.hasRole(role)) {
            this.roles.add(new AccountRoleEntity(this, role, null, grantedBy));
        }
    }

    public void revoke(EnumRole role) {
        AccountRoleEntity target = null;
        for (final AccountRoleEntity ar : this.roles) {
            if (role == ar.role) {
                target = ar;
                break;
            }
        }
        if (target != null) {
            this.roles.remove(target);
        }
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link AccountDto} instance
     */
    public AccountDto toDto() {
        final AccountDto a = new AccountDto();

        a.setActivatedAt(this.activatedAt);
        a.setActivationStatus(this.activationStatus);
        a.setEmail(this.email);
        a.setEmailVerified(this.emailVerified);
        a.setEmailVerifiedAt(this.emailVerifiedAt);
        a.setFirstName(this.firstName);
        a.setId(this.id);
        a.setLastName(this.lastName);
        a.setIdpName(this.idpName);
        a.setIdpUserAlias(this.idpUserAlias);
        a.setIdpUserImage(this.idpUserImage);
        a.setKey(this.key);
        a.setLocale(this.locale);
        a.setPassword(this.password);
        a.setRegisteredAt(this.registeredAt);
        a.setRoles(this.roles.stream().map(r -> r.getRole()).collect(Collectors.toSet()));

        if (this.profile != null) {
            a.setProfile(this.profile.toDto());

            // Set profile properties from account object
            a.getProfile().setFirstName(this.firstName);
            a.getProfile().setLastName(this.lastName);
        }

        return a;
    }

    /**
     * Convert to a publisher DTO object
     *
     * @return a new {@link PublisherDto} instance
     */
    public PublisherDto toPublisherDto() {
        final PublisherDto p = new PublisherDto();

        p.setJoinedAt(this.profile.getProviderVerifiedAt());
        p.setKey(this.key);
        p.setLogoImage(this.profile.getLogoImage());
        p.setLogoImageMimeType(this.profile.getLogoImageMimeType());
        p.setName(this.profile.company);
        p.setRating(this.profile.getRating());

        if (!this.profile.getAddresses().isEmpty()) {
            final AddressEntity address = this.profile.getAddresses().get(0);

            p.setCity(address.city);
            p.setCountry(address.country);
        }

        if(this.profile.isEmailVerified()) {
            p.setEmail(this.profile.email);
        }

        return p;
    }

    /**
     * Update from profile properties
     *
     * @param command The command object
     */
    public void update(AccountProfileCommandDto command) {
        if (!StringUtils.isBlank(command.getFirstName())) {
            this.firstName = command.getFirstName();
        }
        if (!StringUtils.isBlank(command.getLastName())) {
            this.lastName = command.getLastName();
        }
    }

}
