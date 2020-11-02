package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Embeddable
public class ProfileConsumerEmbeddable {

    public ProfileConsumerEmbeddable(AccountProfileConsumerCommandDto c) {
        this.vat                      = c.getVat();

        this.registeredOn = ZonedDateTime.now();
        this.modifiedOn   = this.registeredOn;
    }

    @OneToOne(
        optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`consumer_billing_address`", foreignKey = @ForeignKey(name = "fk_account_profile_consumer_billing_address"))
    @Getter
    @Setter
    private AddressEntity billingAddress;

    @OneToOne(
        optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`consumer_shipping_address`", foreignKey = @ForeignKey(name = "fk_account_profile_consumer_shipping_address"))
    @Getter
    @Setter
    private AddressEntity shippingAddress;

    @Column(name = "`consumer_vat`", length = 12)
    @Getter
    @Setter
    private String vat;

    @Column(name = "`consumer_registered_on`")
    @Getter
    @Setter
    private ZonedDateTime registeredOn;

    @Column(name = "`consumer_modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    public void update(AccountProfileConsumerCommandDto c) {
        final ZonedDateTime now = ZonedDateTime.now();

        this.vat                      = c.getVat();

        this.modifiedOn = now;
        if (this.registeredOn == null) {
            this.registeredOn = now;
        }
    }

}
