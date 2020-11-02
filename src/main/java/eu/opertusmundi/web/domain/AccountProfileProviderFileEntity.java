package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.dto.AccountProfileProviderFileDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountProfileProviderFile")
@Table(schema = "web", name = "`account_profile_provider_file`")
public class AccountProfileProviderFileEntity {

    public AccountProfileProviderFileEntity() {

    }

    public AccountProfileProviderFileEntity(ProviderRegistrationFileEntity f) {
        this.addedBy = f.getModifiedBy();
        this.addedOn = f.getModifiedOn();
        this.file    = f.getFile();
        this.profile = f.getRegistration().getAccount().getProfile();
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_profile_provider_file_id_seq", name = "account_profile_provider_file_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_profile_provider_file_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private int id;

    @NotNull
    @ManyToOne(targetEntity = AccountProfileEntity.class)
    @JoinColumn(name = "profile", nullable = false)
    @Getter
    @Setter
    AccountProfileEntity profile;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.EAGER
    )
    @JoinColumn(name = "`file`", foreignKey = @ForeignKey(name = "fk_account_profile_provider_file_file"))
    @Getter
    @Setter
    private FileUploadEntity file;

    @NotNull
    @Column(name = "`added_on`")
    @Getter
    @Setter
    private ZonedDateTime addedOn;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.EAGER
    )
    @JoinColumn(name = "`added_by`", foreignKey = @ForeignKey(name = "fk_provider_registration_file_modified_by"))
    @Getter
    @Setter
    private AccountEntity addedBy;

    public AccountProfileProviderFileDto toDto() {
        final AccountProfileProviderFileDto f = new AccountProfileProviderFileDto();

        f.setCreatedAt(this.addedOn);
        f.setCreatedBy(this.addedBy.toSimpleDto());
        f.setFile(this.file.toDto());
        f.setId(this.id);

        return f;
    }

}
