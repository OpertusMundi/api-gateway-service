package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.web.model.AccountMapDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountMap")
@Table(schema = "spatial", name = "`map`")
@TypeDef(
    typeClass      = JsonBinaryType.class,
    defaultForType = ObjectNode.class
)
@Getter
@Setter
public class AccountMapEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "spatial.map_id_seq", name = "map_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "map_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    private UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account", nullable = false)
    private AccountEntity account;

    @NotBlank
    @Column(name = "`title`")
    private String title;

    @Column(name = "`thumbnail_url`")
    private String thumbnailUrl;

    @NotBlank
    @Column(name = "`map_url`")
    private String mapUrl;

    @NotNull
    @Column(name = "`created_at`")
    private ZonedDateTime createdAt;

    @NotNull
    @Column(name = "`updated_at`")
    private ZonedDateTime updatedAt;

    @Column(name = "`attributes`")
    private ObjectNode attributes;

    public AccountMapDto toDto() {
        return this.toDto(false);
    }

    public AccountMapDto toDto(boolean includeHelpdeskDetails) {
        final AccountMapDto m = new AccountMapDto();

        m.setCreatedAt(createdAt);
        m.setId(id);
        m.setKey(key);
        m.setMapUrl(mapUrl);
        m.setThumbnailUrl(thumbnailUrl);
        m.setTitle(title);
        m.setUpdatedAt(updatedAt);

        if (includeHelpdeskDetails) {
            m.setAttributes(attributes);
            if (this.getAccount() != null) {
                m.setAccount(this.getAccount().toSimpleDto());
            }
        }

        return m;
    }

}
