package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumProviderRegistrationFileStatus;
import eu.opertusmundi.common.model.dto.ProviderRegistrationFileDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ProviderRegistrationFile")
@Table(schema = "web", name = "`provider_registration_file`")
public class ProviderRegistrationFileEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.provider_registration_file_id_seq", name = "provider_registration_file_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "provider_registration_file_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private int id;

    @NotNull
    @ManyToOne(targetEntity = ProviderRegistrationEntity.class)
    @JoinColumn(name = "registration", nullable = false)
    @Getter
    @Setter
    private ProviderRegistrationEntity registration;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.EAGER
    )
    @JoinColumn(name = "`file`", foreignKey = @ForeignKey(name = "fk_provider_registration_file_file"))
    @Getter
    @Setter
    private FileUploadEntity file;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumProviderRegistrationFileStatus status = EnumProviderRegistrationFileStatus.PENDING;

    @Column(name = "`review`")
    @Getter
    @Setter
    private String review;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.EAGER
    )
    @JoinColumn(name = "`modified_by`", foreignKey = @ForeignKey(name = "fk_provider_registration_file_modified_by"))
    @Getter
    @Setter
    private AccountEntity modifiedBy;

    public ProviderRegistrationFileDto toDto() {
        final ProviderRegistrationFileDto f = new ProviderRegistrationFileDto();

        f.setCreatedOn(this.createdOn);
        f.setFile(this.file.toDto());
        f.setId(this.id);
        f.setModifiedBy(this.modifiedBy != null ? this.modifiedBy.toSimpleDto() : null);
        f.setModifiedOn(this.modifiedOn);
        f.setReview(this.review);
        f.setStatus(this.status);

        return f;
    }

}
