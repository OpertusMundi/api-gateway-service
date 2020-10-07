package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumActivationTokenType;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ActivationToken")
@Table(schema = "web", name = "`activation_token`")
public class ActivationTokenEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.activation_token_id_seq", name = "activation_token_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "activation_token_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    Integer id;

    @Column(name = "`account`", nullable = false, updatable = false)
    @Getter
    @Setter
    private Integer account;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    @Getter
    @Setter
    String email;

    @NotNull
    @Column(name = "`type`", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    EnumActivationTokenType type;

    @NotNull
    @Column(name = "token", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID token = UUID.randomUUID();

    @Column(name = "`created_at`", updatable = false)
    @Getter
    ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "`redeemed_at`")
    @Getter
    @Setter
    ZonedDateTime redeemedAt;

    @Column(name = "`valid`")
    @Getter
    @Setter
    boolean valid;

    @Column(name = "`duration`")
    @Getter
    @Setter
    int duration;

    public boolean isExpired() {
        if (this.redeemedAt != null) {
            return true;
        }
        return this.createdAt.plusHours(this.duration).isBefore(ZonedDateTime.now());
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link ActivationTokenDto} instance
     */
    public ActivationTokenDto toDto() {
        final ActivationTokenDto o = new ActivationTokenDto();

        o.setAccount(this.account);
        o.setCreatedAt(this.createdAt);
        o.setDuration(this.duration);
        o.setEmail(this.email);
        o.setExpired(this.isExpired());
        o.setId(this.id);
        o.setRedeemedAt(this.redeemedAt);
        o.setToken(this.token);
        o.setType(this.type);
        o.setValid(this.isValid());

        return o;
    }

}
