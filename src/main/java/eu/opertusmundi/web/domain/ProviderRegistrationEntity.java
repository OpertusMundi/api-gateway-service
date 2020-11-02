package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.EnumProviderRegistrationStatus;
import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderDraftDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ProviderRegistration")
@Table(schema = "web", name = "`provider_registration`")
public class ProviderRegistrationEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.provider_registration_id_seq", name = "provider_registration_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "provider_registration_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private int id;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`account`", foreignKey = @ForeignKey(name = "fk_account_provider_registration_account"))
    @Getter
    @Setter
    private AccountEntity account;


    @OneToMany(
        targetEntity = ProviderRegistrationFileEntity.class,
        mappedBy = "registration",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    private final List<ProviderRegistrationFileEntity> files = new ArrayList<>();

    @Column(name = "`additional_info`")
    @Getter
    @Setter
    private String additionalInfo;

    @Column(name = "`bank_account_currency`", length = 4)
    @Getter
    @Setter
    private String bankAccountCurrency;

    @Column(name = "`bank_account_holder_name`", length = 40)
    @Getter
    @Setter
    private String bankAccountHolderName;

    @Column(name = "`bank_account_iban`", length = 40)
    @Getter
    @Setter
    private String bankAccountIban;

    @Column(name = "`bank_account_swift`", length = 40)
    @Getter
    @Setter
    private String bankAccountSwift;

    @Column(name = "`company`", length = 80)
    @Getter
    @Setter
    private String company;

    @Column(name = "`company_type`", length = 80)
    @Getter
    @Setter
    private String companyType;

    @Column(name = "`contract`", columnDefinition = "uuid")
    @Getter
    private UUID contract;

    @Column(name = "`country`", length = 40)
    @Getter
    @Setter
    private String country;

    @Column(name = "`country_phone_code`", length = 4)
    @Getter
    @Setter
    private String countryPhoneCode;

    @Email
    @Column(name = "`email`", length = 120)
    @Getter
    @Setter
    private String email;

    @Column(name = "`logo_image_binary`")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] logoImage;

    @Column(name = "`logo_image_mime_type`")
    @Getter
    @Setter
    private String logoImageMimeType;

    @Column(name = "`phone`", length = 20)
    @Getter
    @Setter
    private String phone;

    @Column(name = "`site_url`", length = 80)
    @Getter
    @Setter
    private String siteUrl;

    @Column(name = "`vat`", length = 12)
    @Getter
    @Setter
    private String vat;

    @Column(name = "`created_on`")
    @Getter
    private ZonedDateTime createdOn;

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumProviderRegistrationStatus status = EnumProviderRegistrationStatus.DRAFT;

    @Transient
    public boolean isProcessed() {
        return this.status == EnumProviderRegistrationStatus.CANCELLED ||
               this.status == EnumProviderRegistrationStatus.COMPLETED;
    }

    public void update(AccountProfileProviderCommandDto command) {
        this.additionalInfo        = command.getAdditionalInfo();
        this.bankAccountCurrency   = command.getBankAccountCurrency();
        this.bankAccountHolderName = command.getBankAccountHolderName();
        this.bankAccountIban       = command.getBankAccountIban();
        this.bankAccountSwift      = command.getBankAccountSwift();
        this.company               = command.getCompany();
        this.companyType           = command.getCompanyType();
        this.country               = command.getCountry();
        this.countryPhoneCode      = command.getCountryPhoneCode();
        this.email                 = command.getEmail();
        this.logoImage             = command.getLogoImage();
        this.logoImageMimeType     = command.getLogoImageMimeType();
        this.phone                 = command.getPhone();
        this.siteUrl               = command.getSiteUrl();
        this.vat                   = command.getVat();

        this.setModifiedOn(ZonedDateTime.now());
        if (this.createdOn == null) {
            this.createdOn = this.modifiedOn;
        }
    }

    public AccountProfileProviderDraftDto toDto() {
        final AccountProfileProviderDraftDto d = new AccountProfileProviderDraftDto();

        d.setAdditionalInfo(this.additionalInfo);
        d.setBankAccountCurrency(this.bankAccountCurrency);
        d.setBankAccountHolderName(this.bankAccountHolderName);
        d.setBankAccountIban(this.bankAccountIban);
        d.setBankAccountSwift(this.bankAccountSwift);
        d.setCompany(this.company);
        d.setCompanyType(this.companyType);
        d.setContract(this.contract);
        d.setCountry(this.country);
        d.setCountryPhoneCode(this.countryPhoneCode);
        d.setCreatedOn(this.createdOn);
        d.setEmail(this.email);
        d.setLogoImage(this.logoImage);
        d.setLogoImageMimeType(this.logoImageMimeType);
        d.setModifiedOn(this.modifiedOn);
        d.setPhone(this.phone);
        d.setSiteUrl(this.siteUrl);
        d.setStatus(this.status);
        d.setVat(this.vat);

        return d;
    }

}
