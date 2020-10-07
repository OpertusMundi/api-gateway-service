package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
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

    @OneToMany(
        targetEntity = AddressEntity.class,
        mappedBy = "profile",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    private final List<AddressEntity> addresses   = new ArrayList<>();

    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    @Getter
    @Setter
    String email;

    @Column(name = "`email_verified`")
    @Getter
    @Setter
    boolean emailVerified;

    @Column(name = "`email_verified_at`")
    @Getter
    @Setter
    ZonedDateTime emailVerifiedAt;

    @Column(name = "`mobile`", length = 15)
    @Getter
    @Setter
    String mobile;

    @Column(name = "image_binary")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] image;

    @Column(name = "image_mime_type")
    @Getter
    @Setter
    private String imageMimeType;

    @Column(name = "`company`", length = 80)
    @Getter
    @Setter
    String company;

    @Column(name = "`company_type`", length = 80)
    @Getter
    @Setter
    String companyType;

    @Column(name = "`vat`", length = 12)
    @Getter
    @Setter
    String vat;

    @Column(name = "`country`", length = 40)
    @Getter
    @Setter
    String country;

    @Column(name = "`country_phone_code`", length = 4)
    @Getter
    @Setter
    String countryPhoneCode;

    @Column(name = "`phone`", length = 20)
    @Getter
    @Setter
    String phone;

    @Column(name = "`additional_info`")
    @Getter
    @Setter
    String additionalInfo;

    @Column(name = "`bank_account_iban`", length = 40)
    @Getter
    @Setter
    String bankAccountIban;

    @Column(name = "`bank_account_swift`", length = 40)
    @Getter
    @Setter
    String bankAccountSwift;

    @Column(name = "`bank_account_holder_name`", length = 40)
    @Getter
    @Setter
    String bankAccountHolderName;

    @Column(name = "`bank_account_currency`", length = 15)
    @Getter
    @Setter
    String bankAccountCurrency;

    @Column(name = "`terms_accepted`")
    @Getter
    @Setter
    boolean termsAccepted;

    @Column(name = "`terms_accepted_at`")
    @Getter
    @Setter
    ZonedDateTime termsAcceptedAt;

    @Column(name = "`site_url`", length = 80)
    @Getter
    @Setter
    String siteUrl;

    @Column(name = "logo_image_binary")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] logoImage;

    @Column(name = "logo_image_mime_type")
    @Getter
    @Setter
    private String logoImageMimeType;

    @Column(name = "`provider_verified_at`", length = 15)
    @Getter
    @Setter
    ZonedDateTime providerVerifiedAt;

    @NotNull
    @Column(name = "`rating_count`")
    @Getter
    @Setter
    Integer ratingCount;

    @NotNull
    @Column(name = "`rating_total`")
    @Getter
    @Setter
    Integer ratingTotal;

    @Column(name = "`created_on`")
    @Getter
    ZonedDateTime createdOn = ZonedDateTime.now();

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    ZonedDateTime modifiedOn;

    /**
     * Update from a command DTO object
     *
     * @param command The command object
     */
    public void update(AccountProfileCommandDto command) {
        // Get current email value
        final String currentEmail = this.email;

        // If email is updated, request validation
        if (!StringUtils.isBlank(command.getEmail()) && !command.getEmail().equals(currentEmail)) {
            this.emailVerified   = false;
            this.emailVerifiedAt = null;
        }

        this.email         = command.getEmail();
        this.image         = command.getImage();
        this.imageMimeType = command.getImageMimeType();
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link AccountProfileDto} instance
     */
    public AccountProfileDto toDto() {
        final AccountProfileDto p = new AccountProfileDto();

        p.setAdditionalInfo(this.additionalInfo);
        p.setBankAccountCurrency(this.bankAccountCurrency);
        p.setBankAccountHolderName(this.bankAccountHolderName);
        p.setBankAccountIban(this.bankAccountIban);
        p.setBankAccountSwift(this.bankAccountSwift);
        p.setCompany(this.company);
        p.setCompanyType(this.companyType);
        p.setCountry(this.country);
        p.setCountryPhoneCode(this.countryPhoneCode);
        p.setCreatedOn(this.createdOn);
        p.setEmail(this.email);
        p.setEmailVerified(this.emailVerified);
        p.setEmailVerifiedAt(this.emailVerifiedAt);
        p.setImage(this.image);
        p.setImageMimeType(this.imageMimeType);
        p.setLogoImage(this.logoImage);
        p.setLogoImageMimeType(this.logoImageMimeType);
        p.setMobile(this.mobile);
        p.setModifiedOn(this.modifiedOn);
        p.setPhone(this.countryPhoneCode);
        p.setProviderVerifiedAt(this.providerVerifiedAt);
        p.setRating(this.getRating());
        p.setSiteUrl(this.siteUrl);
        p.setTermsAccepted(this.termsAccepted);
        p.setTermsAcceptedAt(this.termsAcceptedAt);
        p.setVat(this.vat);

        p.setAddresses(this.addresses.stream().map(AddressEntity::toDto).collect(Collectors.toList()));

        return p;
    }

    public Double getRating() {
        if(this.ratingCount == 0) {
            return null;
        }
        final double rating = (double) this.ratingTotal / (double) this.ratingCount;

        return Math.round(rating * 10) / 10.0;
    }
}
