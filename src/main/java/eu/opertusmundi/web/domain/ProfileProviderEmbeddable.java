package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Embeddable
public class ProfileProviderEmbeddable {

    @OneToMany(
        targetEntity = AccountProfileProviderFileEntity.class,
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        mappedBy = "profile"
    )
    @Getter
    private final List<AccountProfileProviderFileEntity> files = new ArrayList<>();

    @Column
    @Getter
    @Setter
    private String additionalInfo;

    @Column(length = 4)
    @Getter
    @Setter
    private String bankAccountCurrency;

    @Column(length = 40)
    @Getter
    @Setter
    private String bankAccountHolderName;

    @Column(length = 40)
    @Getter
    @Setter
    private String bankAccountIban;

    @Column(length = 40)
    @Getter
    @Setter
    private String bankAccountSwift;

    @Column(length = 80)
    @Getter
    @Setter
    private String company;

    @Column(length = 80)
    @Getter
    @Setter
    private String companyType;

    @Column(columnDefinition = "uuid")
    @Getter
    private UUID contract;

    @Column(length = 40)
    @Getter
    @Setter
    private String country;

    @Column(length = 4)
    @Getter
    @Setter
    private String countryPhoneCode;

    @Email
    @Column(length = 120)
    @Getter
    @Setter
    private String email;

    @Column
    @Getter
    @Setter
    boolean emailVerified;

    @Column
    @Getter
    @Setter
    private ZonedDateTime emailVerifiedAt;

    @Column
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] logoImage;

    @Column
    @Getter
    @Setter
    private String logoImageMimeType;

    @Column(length = 20)
    @Getter
    @Setter
    private String phone;

    @NotNull
    @Column
    @Getter
    @Setter
    private Integer ratingCount = 0;

    @NotNull
    @Column
    @Getter
    @Setter
    private Integer ratingTotal = 0;

    @Column(length = 80)
    @Getter
    @Setter
    private String siteUrl;

    @Column
    @Getter
    @Setter
    private boolean termsAccepted;

    @Column
    @Getter
    @Setter
    private ZonedDateTime termsAcceptedAt;

    @Column(length = 12)
    @Getter
    @Setter
    private String vat;

    @Column
    @Getter
    @Setter
    private ZonedDateTime registeredOn;

    @Column
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    @Transient
    public Double getRating() {
        if(this.ratingCount == 0) {
            return null;
        }
        final double rating = (double) this.ratingTotal / (double) this.ratingCount;

        return Math.round(rating * 10) / 10.0;
    }

    @Override
    public ProfileProviderEmbeddable clone() {
        final ProfileProviderEmbeddable e = new ProfileProviderEmbeddable();

        e.additionalInfo        = this.additionalInfo;
        e.bankAccountCurrency   = this.bankAccountCurrency;
        e.bankAccountHolderName = this.bankAccountHolderName;
        e.bankAccountIban       = this.bankAccountIban;
        e.bankAccountSwift      = this.bankAccountSwift;
        e.company               = this.company;
        e.companyType           = this.companyType;
        e.contract              = this.contract;
        e.country               = this.country;
        e.countryPhoneCode      = this.countryPhoneCode;
        e.email                 = this.email;
        e.emailVerified         = this.emailVerified;
        e.emailVerifiedAt       = this.emailVerifiedAt;
        e.logoImage             = this.logoImage;
        e.logoImageMimeType     = this.logoImageMimeType;
        e.modifiedOn            = this.modifiedOn;
        e.phone                 = this.phone;
        e.ratingCount           = this.ratingCount;
        e.ratingTotal           = this.ratingTotal;
        e.registeredOn          = this.registeredOn;
        e.siteUrl               = this.siteUrl;
        e.termsAccepted         = this.termsAccepted;
        e.termsAcceptedAt       = this.getTermsAcceptedAt();
        e.vat                   = this.vat;

        // NOTE: property `files` cloning is not supported

        return e;
    }

    public void update(ProviderRegistrationEntity r) {
        this.additionalInfo        = r.getAdditionalInfo();
        this.bankAccountCurrency   = r.getBankAccountCurrency();
        this.bankAccountHolderName = r.getBankAccountHolderName();
        this.bankAccountIban       = r.getBankAccountIban();
        this.bankAccountSwift      = r.getBankAccountSwift();
        this.company               = r.getCompany();
        this.companyType           = r.getCompanyType();
        this.contract              = r.getContract();
        this.country               = r.getCountry();
        this.countryPhoneCode      = r.getCountryPhoneCode();
        if (!StringUtils.isBlank(r.getEmail()) && !r.getEmail().equals(this.email)) {
            this.emailVerified   = false;
            this.emailVerifiedAt = null;
        }
        this.email             = r.getEmail();
        this.logoImage         = r.getLogoImage();
        this.logoImageMimeType = r.getLogoImageMimeType();
        this.phone             = r.getPhone();
        this.siteUrl           = r.getSiteUrl();
        this.vat               = r.getVat();

    }

}
