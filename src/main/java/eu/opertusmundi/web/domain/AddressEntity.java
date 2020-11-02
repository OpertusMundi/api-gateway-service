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
import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.model.EnumAddressType;
import eu.opertusmundi.common.model.dto.AddressCommandDto;
import eu.opertusmundi.common.model.dto.AddressDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Address")
@Table(schema = "web", name = "`address`")
public class AddressEntity {

    public AddressEntity() {
        final ZonedDateTime now = ZonedDateTime.now();

        this.createdOn  = now;
        this.modifiedOn = now;
    }

    public AddressEntity(AddressCommandDto a) {
        final ZonedDateTime now = ZonedDateTime.now();

        this.city           = a.getCity();
        this.country        = a.getCountry();
        this.createdOn      = now;
        this.floorApartment = a.getFloorApartment();
        this.modifiedOn     = now;
        this.main           = a.isMain();
        this.postalCode     = a.getPostalCode();
        this.region         = a.getRegion();
        this.streetName     = a.getStreetName();
        this.streetNumber   = a.getStreetNumber();
        this.type           = a.getType();
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.address_id_seq", name = "address_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "address_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private int id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile", nullable = false)
    @Getter
    @Setter
    private AccountProfileEntity profile;

    @Column(name = "street", length = 120)
    @Getter
    @Setter
    private String streetName;

    @Length(max = 10)
    @Column(name = "number", length = 10)
    @Getter
    @Setter
    private String streetNumber;

    @Column(name = "city", length = 120)
    @Getter
    @Setter
    private String city;

    @Column(name = "region", length = 80)
    @Getter
    @Setter
    private String region;

    @Column(name = "country", length = 40)
    @Getter
    @Setter
    private String country;

    @Column(name = "postal_code", length = 10)
    @Getter
    @Setter
    private String postalCode;

    @Column(name = "floor_apartment", length = 10)
    @Getter
    @Setter
    private String floorApartment;

    @Column(name = "`created_on`")
    @Getter
    private final ZonedDateTime createdOn;

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumAddressType type;


    @NotNull
    @Column(name = "`default`")
    @Getter
    @Setter
    private boolean main;

    /**
     * Update from a command DTO object
     *
     * @param command The command object
     */
    public void update(AddressCommandDto command) {
        this.city           = command.getCity();
        this.country        = command.getCountry();
        this.floorApartment = command.getFloorApartment();
        this.main           = command.isMain();
        this.postalCode     = command.getPostalCode();
        this.region         = command.getRegion();
        this.streetName     = command.getStreetName();
        this.streetNumber   = command.getStreetNumber();
        this.type           = command.getType();
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
        a.setKey(this.key);
        a.setMain(this.main);
        a.setModifiedOn(this.modifiedOn);
        a.setPostalCode(this.postalCode);
        a.setRegion(this.region);
        a.setStreetName(this.streetName);
        a.setStreetNumber(this.streetNumber);
        a.setType(this.type);

        return a;
    }
}
