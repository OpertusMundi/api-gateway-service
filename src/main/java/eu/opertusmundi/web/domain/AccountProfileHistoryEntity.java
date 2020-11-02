package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
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
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountProfileHistory")
@Table(schema = "web", name = "`account_profile_hist`")
public class AccountProfileHistoryEntity {

    public AccountProfileHistoryEntity() {

    }

    public AccountProfileHistoryEntity(AccountProfileEntity p) {
        this.consumer             = p.getConsumer();
        this.createdOn            = p.getCreatedOn();
        this.firstName            = p.getAccount().getFirstName();
        this.image                = p.getImage();
        this.imageMimeType        = p.getImageMimeType();
        this.lastName             = p.getAccount().getLastName();
        this.locale               = p.getAccount().getLocale();
        this.mobile               = p.getMobile();
        this.modifiedOn           = p.getModifiedOn();
        this.phone                = p.getPhone();
        this.profile              = p;
        this.provider             = p.getProvider().clone();
        this.providerRegistration = p.getProviderRegistration();
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_profile_hist_id_seq", name = "account_profile_hist_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_profile_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    int id;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`profile`", foreignKey = @ForeignKey(name = "fk_account_profile_hist_profile"))
    @Getter
    @Setter
    private AccountProfileEntity profile;

    @OneToMany(
        targetEntity = AddressEntity.class,
        mappedBy = "profile",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    private final List<AddressEntity> addresses   = new ArrayList<>();

    @OneToOne(
        optional = false, fetch = FetchType.EAGER
    )
    @JoinColumn(name = "`provider_registration`")
    @Getter
    @Setter
    private ProviderRegistrationEntity providerRegistration;

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
        @AttributeOverride(name = "billingAddressCity", column = @Column(name = "`consumer_billing_address_city`")),
        @AttributeOverride(name = "billingAddressCountry", column = @Column(name = "`consumer_billing_address_country`")),
        @AttributeOverride(name = "billingAddressNumber", column = @Column(name = "`consumer_billing_address_number`")),
        @AttributeOverride(name = "billingAddressPostal_code", column = @Column(name = "`consumer_billing_address_postal_code`")),
        @AttributeOverride(name = "billingAddressRegion", column = @Column(name = "`consumer_billing_address_region`")),
        @AttributeOverride(name = "billingAddressStreet", column = @Column(name = "`consumer_billing_address_street`")),
        @AttributeOverride(name = "vat", column = @Column(name = "`consumer_vat`")),
        @AttributeOverride(name = "registeredOn", column = @Column(name = "`consumer_registered_on`")),
        @AttributeOverride(name = "modifiedOn", column = @Column(name = "`consumer_modified_on`")),
    })
    @Getter
    @Setter
    private ProfileConsumerEmbeddable consumer;

    @Column(name = "`firstname`", length = 64)
    @Getter
    @Setter
    String firstName;

    @Column(name = "`lastname`", length = 64)
    @Getter
    @Setter
    String lastName;

    @Pattern(regexp = "[a-z][a-z]")
    @Column(name = "`locale`")
    @Getter
    @Setter
    String locale;

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

}
