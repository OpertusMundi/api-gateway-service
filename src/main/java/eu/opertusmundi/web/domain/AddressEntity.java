package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;

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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.model.dto.AddressCommandDto;
import eu.opertusmundi.common.model.dto.AddressDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Address")
@Table(schema = "web", name = "`address`")
public class AddressEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.address_id_seq", name = "address_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "address_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile", nullable = false)
    AccountProfileEntity profile;

    @Column(name = "street", length = 120)
    @Getter
    @Setter
    String streetName;

    @Length(max = 10)
    @Column(name = "number", length = 10)
    @Getter
    @Setter
    String streetNumber;

    @Column(name = "city", length = 120)
    @Getter
    @Setter
    String city;

    @Column(name = "region", length = 80)
    @Getter
    @Setter
    String region;

    @Column(name = "country", length = 40)
    @Getter
    @Setter
    String country;

    @Column(name = "postal_code", length = 10)
    @Getter
    @Setter
    String postalCode;

    @Column(name = "floor_apartment", length = 10)
    @Getter
    @Setter
    String floorApartment;

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
    public void update(AddressCommandDto command) {
        this.city           = command.getCity();
        this.country        = command.getCountry();
        this.floorApartment = command.getFloorApartment();
        this.postalCode     = command.getPostalCode();
        this.region         = command.getRegion();
        this.streetName     = command.getStreetName();
        this.streetNumber   = command.getStreetNumber();
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link AddressDto} instance
     */
    public AddressDto toDto() {
        final AddressDto a = new AddressDto();

        a.setCity(this.city);
        a.setCountry(this.country);
        a.setCreatedOn(this.createdOn);
        a.setFloorApartment(this.floorApartment);
        a.setModifiedOn(this.modifiedOn);
        a.setPostalCode(this.postalCode);
        a.setRegion(this.region);
        a.setStreetName(this.streetName);
        a.setStreetNumber(this.streetNumber);

        return a;
    }
}
