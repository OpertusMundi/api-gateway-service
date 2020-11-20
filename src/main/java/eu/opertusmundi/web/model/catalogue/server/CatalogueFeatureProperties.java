package eu.opertusmundi.web.model.catalogue.server;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.web.model.catalogue.client.CatalogueItemStoreStatistics;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueFeatureProperties {

    @JsonProperty("abstract")
    private String abstractText;

    @JsonProperty("additional_resources")
    private String additionalResources;

    private String conformity;

    @JsonProperty("coupled_resource")
    private String coupledResource;

    @JsonProperty("creation_date")
    private String creationDate;

    @JsonProperty("date_end")
    private String dateEnd;

    @JsonProperty("date_start")
    private String dateStart;

    private String format;

    private List<String> keywords;

    private String language;

    private String license;

    private String lineage;

    @JsonProperty("metadata_date")
    private String metadataDate;

    @JsonProperty("metadata_language")
    private String metadataLanguage;

    @JsonProperty("metadata_point_of_contact_email")
    private String metadataPointOfContactEmail;

    @JsonProperty("metadata_point_of_contact_name")
    private String metadataPointOfContactName;

    @JsonProperty("parent_id")
    private String parentId;

    @JsonProperty("pricing_models")
    private List<BasePricingModelCommandDto> pricingModels;

    @JsonProperty("public_access_limitations")
    private String publicAccessLimitations;

    @JsonProperty("publication_date")
    private String publicationDate;

    @JsonProperty("publisher_email")
    private String publisherEmail;

    @JsonProperty("publisher_id")
    private UUID publisherId;

    @JsonProperty("publisher_name")
    private String publisherName;

    @JsonProperty("reference_system")
    private String referenceSystem;

    @JsonProperty("resource_locator")
    private String resourceLocator;

    @JsonProperty("revision_date")
    private String revisionDate;

    private String scale;

    private String status;

    @JsonProperty("spatial_data_service_type")
    private String spatialDataServiceType;

    @JsonProperty("spatial_resolution")
    private String spatialResolution;

    @JsonProperty("store_statistics")
    private CatalogueItemStoreStatistics storeStatistics;

    private String title;

    @JsonProperty("topic_category")
    private String topicCategory;

    private String type;

    private String version;

}
