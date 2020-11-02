package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.EnumOwningEntityType;
import eu.opertusmundi.common.model.dto.FileUploadDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "FileUpload")
@Table(schema = "`file`", name = "`file_upload`")
public class FileUploadEntity {

    protected FileUploadEntity() {

    }

    public FileUploadEntity(UUID owningEntityKey) {
        this.owningEntityKey = owningEntityKey;
    }

    @Id
    @SequenceGenerator(sequenceName = "`file.file_upload_id_seq`", name = "file_upload_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "file_upload_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @NaturalId
    @Getter
    private final UUID key = UUID.randomUUID();

    @NotNull
    @Column(name = "owning_entity_key", updatable = false, columnDefinition = "uuid")
    @Getter
    private UUID owningEntityKey;


    @NotNull
    @Column(name = "`owning_entity_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumOwningEntityType owningEntityType;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`uploaded_by`")
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @Column(name = "`file_name`")
    @Getter
    @Setter
    private String fileName;

    @NotNull
    @Column(name = "`relative_path`")
    @Getter
    @Setter
    private String relativePath;

    @Column(name = "`size`")
    @Getter
    @Setter
    private Long size;

    @Column(name = "`comment`")
    @Getter
    @Setter
    private String comment;

    public FileUploadDto toDto() {
        final FileUploadDto f = new FileUploadDto();

        f.setComment(this.comment);
        f.setCreatedOn(this.createdOn);
        f.setFileName(this.fileName);
        f.setId(this.id);
        f.setKey(this.key);
        f.setOwningEntityKey(this.owningEntityKey);
        f.setOwningEntityType(this.owningEntityType);
        f.setRelativePath(this.relativePath);
        f.setSize(this.size);

        return f;
    }

}