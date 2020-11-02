package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.dto.AccountProfileConsumerDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderDraftDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.common.model.dto.AddressCommandDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountProfile")
@Table(schema = "web", name = "`account_profile`")
public class AccountProfileEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_profile_id_seq", name = "account_profile_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_profile_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    int id;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`account`", foreignKey = @ForeignKey(name = "fk_account_profile_account"))
    @Getter
    @Setter
    private AccountEntity account;

    @OneToOne(
        optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`provider_registration`", foreignKey = @ForeignKey(name = "fk_account_profile_provider_registration"))
    @Getter
    @Setter
    private ProviderRegistrationEntity providerRegistration;

    @OneToMany(
        targetEntity = AddressEntity.class,
        mappedBy = "profile",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    private final List<AddressEntity> addresses   = new ArrayList<>();

    @Embedded
    @AssociationOverride(
        name = "files", joinColumns = @JoinColumn(name = "profile")
    )
    @AttributeOverrides({
        @AttributeOverride(name = "additionalInfo", column = @Column(name = "`provider_additional_info`")),
        @AttributeOverride(name = "bankAccountCurrency", column = @Column(name = "`provider_bank_account_currency`")),
        @AttributeOverride(name = "bankAccountHolderName", column = @Column(name = "`provider_bank_account_holder_name`")),
        @AttributeOverride(name = "bankAccountIban", column = @Column(name = "`provider_bank_account_iban`")),
        @AttributeOverride(name = "bankAccountSwift", column = @Column(name = "`provider_bank_account_swift`")),
        @AttributeOverride(name = "company", column = @Column(name = "`provider_company`")),
        @AttributeOverride(name = "companyType", column = @Column(name = "`provider_company_type`")),
        @AttributeOverride(name = "contract", column = @Column(name = "`provider_contract`")),
        @AttributeOverride(name = "country", column = @Column(name = "`provider_country`")),
        @AttributeOverride(name = "countryPhoneCode", column = @Column(name = "`provider_country_phone_code`")),
        @AttributeOverride(name = "email", column = @Column(name = "`provider_email`")),
        @AttributeOverride(name = "emailVerified", column = @Column(name = "`provider_email_verified`")),
        @AttributeOverride(name = "emailVerifiedAt", column = @Column(name = "`provider_email_verified_at`")),
        @AttributeOverride(name = "logoImage", column = @Column(name = "`provider_logo_image_binary`")),
        @AttributeOverride(name = "logoImageMimeType", column = @Column(name = "`provider_logo_image_mime_type`")),
        @AttributeOverride(name = "phone", column = @Column(name = "`provider_phone`")),
        @AttributeOverride(name = "ratingCount", column = @Column(name = "`provider_rating_count`")),
        @AttributeOverride(name = "ratingTotal", column = @Column(name = "`provider_rating_total`")),
        @AttributeOverride(name = "siteUrl", column = @Column(name = "`provider_site_url`")),
        @AttributeOverride(name = "termsAccepted", column = @Column(name = "`provider_terms_accepted`")),
        @AttributeOverride(name = "termsAcceptedAt", column = @Column(name = "`provider_terms_accepted_at`")),
        @AttributeOverride(name = "vat", column = @Column(name = "`provider_vat`")),
        @AttributeOverride(name = "registeredOn", column = @Column(name = "`provider_registered_on`")),
        @AttributeOverride(name = "modifiedOn", column = @Column(name = "`provider_modified_on`")),
    })
    @Getter
    @Setter
    private ProfileProviderEmbeddable provider;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "billingAddress", column = @Column(name = "`consumer_billing_address`")),
        @AttributeOverride(name = "shippingAddress", column = @Column(name = "`consumer_shipping_address`")),
        @AttributeOverride(name = "vat", column = @Column(name = "`consumer_vat`")),
        @AttributeOverride(name = "registeredOn", column = @Column(name = "`consumer_registered_on`")),
        @AttributeOverride(name = "modifiedOn", column = @Column(name = "`consumer_modified_on`")),
    })
    @Getter
    @Setter
    private ProfileConsumerEmbeddable consumer;

    @Column(name = "`phone`", length = 15)
    @Getter
    @Setter
    private String phone;

    @Column(name = "`mobile`", length = 15)
    @Getter
    @Setter
    private String mobile;

    @Column(name = "image_binary")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] image;

    @Column(name = "image_mime_type")
    @Getter
    @Setter
    private String imageMimeType;

    @Column(name = "`created_on`", updatable = false)
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    /**
     * Update from a command DTO object
     *
     * @param command The command object
     */
    public void update(AccountProfileUpdateCommandDto command) {
        this.phone         = command.getPhone();
        this.mobile        = command.getMobile();
        this.image         = command.getImage();
        this.imageMimeType = command.getImageMimeType();
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link AccountProfileDto} instance
     */
    public AccountProfileDto toDto() {
        final AccountProfileDto profile = new AccountProfileDto();

        // Set provider data
        if (this.provider != null && this.provider.getRegisteredOn() != null) {
            final AccountProfileProviderDto p = new AccountProfileProviderDto();

            p.setAdditionalInfo(this.provider.getAdditionalInfo());
            p.setBankAccountCurrency(this.provider.getBankAccountCurrency());
            p.setBankAccountHolderName(this.provider.getBankAccountHolderName());
            p.setBankAccountIban(this.provider.getBankAccountIban());
            p.setBankAccountSwift(this.provider.getBankAccountSwift());
            p.setCompany(this.provider.getCompany());
            p.setCompanyType(this.provider.getCompanyType());
            p.setContract(this.provider.getContract());
            p.setCountry(this.provider.getCountry());
            p.setCountryPhoneCode(this.provider.getCountryPhoneCode());
            p.setEmail(this.provider.getEmail());
            p.setEmailVerified(this.provider.isEmailVerified());
            p.setEmailVerifiedAt(this.provider.getEmailVerifiedAt());
            p.setLogoImage(this.provider.getLogoImage());
            p.setLogoImageMimeType(this.provider.getLogoImageMimeType());
            p.setModifiedOn(this.provider.getModifiedOn());
            p.setPhone(this.provider.getPhone());
            p.setRating(this.provider.getRating());
            p.setRegisteredOn(this.provider.getRegisteredOn());
            p.setSiteUrl(this.provider.getSiteUrl());
            p.setTermsAccepted(this.provider.isTermsAccepted());
            p.setTermsAcceptedAt(this.provider.getTermsAcceptedAt());
            p.setVat(this.provider.getVat());

            // Include uploaded files
            this.getProvider().getFiles().stream().map(AccountProfileProviderFileEntity::toDto).forEach(p.getFiles()::add);

            profile.getProvider().setCurrent(p);
        }

        // Set provider draft data
        if (this.getProviderRegistration() != null && !this.getProviderRegistration().isProcessed()) {
            final AccountProfileProviderDraftDto d = new AccountProfileProviderDraftDto();
            final ProviderRegistrationEntity     r = this.getProviderRegistration();

            d.setAdditionalInfo(r.getAdditionalInfo());
            d.setBankAccountCurrency(r.getBankAccountCurrency());
            d.setBankAccountHolderName(r.getBankAccountHolderName());
            d.setBankAccountIban(r.getBankAccountIban());
            d.setBankAccountSwift(r.getBankAccountSwift());
            d.setCompany(r.getCompany());
            d.setCompanyType(r.getCompanyType());
            d.setContract(r.getContract());
            d.setCountry(r.getCountry());
            d.setCountryPhoneCode(r.getCountryPhoneCode());
            d.setEmail(r.getEmail());
            d.setLogoImage(r.getLogoImage());
            d.setLogoImageMimeType(r.getLogoImageMimeType());
            d.setModifiedOn(r.getModifiedOn());
            d.setPhone(r.getPhone());
            d.setCreatedOn(r.getCreatedOn());
            d.setSiteUrl(r.getSiteUrl());
            d.setStatus(r.getStatus());
            d.setVat(r.getVat());

            // Include draft uploaded files
            r.getFiles().stream().map(ProviderRegistrationFileEntity::toDto).forEach(d.getFiles()::add);

            profile.getProvider().setDraft(d);
        }

        // Set consumer data
        final AccountProfileConsumerDto c = new AccountProfileConsumerDto();

        if (this.consumer != null && this.consumer.getRegisteredOn() != null) {
            c.setBillingAddress(
                this.consumer.getBillingAddress()!=null ? this.consumer.getBillingAddress().toDto() : null
            );
            c.setModifiedOn(this.consumer.getModifiedOn());
            c.setRegisteredOn(this.consumer.getRegisteredOn());
            c.setShippingAddress(
                this.consumer.getShippingAddress() != null ? this.consumer.getShippingAddress().toDto() : null
            );
            c.setVat(this.consumer.getVat());

            profile.getConsumer().setCurrent(c);
        }

        // Set profile data
        profile.setCreatedOn(this.createdOn);
        profile.setFirstName(this.account.getFirstName());
        profile.setImage(this.image);
        profile.setImageMimeType(this.imageMimeType);
        profile.setLastName(this.account.getLastName());
        profile.setLocale(this.account.getLocale());
        profile.setMobile(this.mobile);
        profile.setModifiedOn(this.modifiedOn);
        profile.setPhone(this.phone);

        profile.setAddresses(this.addresses.stream().map(AddressEntity::toDto).collect(Collectors.toList()));

        return profile;
    }

    public void addAddress(AddressCommandDto command) {
        final AddressEntity a = new AddressEntity(command);

        a.setProfile(this);

        this.getAddresses().add(a);
    }

    public Optional<AddressEntity> getAddressByKey(UUID key) {
        return this.addresses.stream()
            .filter(a -> a.getKey().equals(key))
            .findFirst();
    }

    public void removeAddress(UUID key) {
        final AddressEntity address = this.getAddressByKey(key).orElse(null);

        if (address == null) {
            throw new EntityNotFoundException();
        }

        // Remove address
        this.addresses.remove(address);

        // Remove any reference from the consumer registration
        if (this.consumer.getShippingAddress() != null &&
            this.consumer.getShippingAddress().getKey().equals(key)) {
            this.consumer.setShippingAddress(null);
        }
        if (this.consumer.getBillingAddress() != null &&
            this.consumer.getBillingAddress().getKey().equals(key)) {
            this.consumer.setBillingAddress(null);
        }
    }

    public void setAddresses(List<AddressCommandDto> addresses) {
        this.addresses.clear();

        if (addresses == null) {
            return;
        }

        addresses.stream().forEach(a -> {
            final AddressEntity e = new AddressEntity(a);

            e.setProfile(this);

            this.getAddresses().add(e);
        });
    }

}
