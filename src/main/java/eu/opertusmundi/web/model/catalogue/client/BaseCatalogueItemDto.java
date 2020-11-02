package eu.opertusmundi.web.model.catalogue.client;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.web.model.openapi.schema.GeometryAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseCatalogueItemDto {

    protected BaseCatalogueItemDto() {

    }

    protected BaseCatalogueItemDto(CatalogueFeature feature) {
        final CatalogueFeatureProperties props = feature.getProperties();

        this.abstractText                = props.getAbstractText();
        this.additionalResources         = props.getAdditionalResources();
        this.conformity                  = props.getConformity();
        this.coupledResource             = props.getCoupledResource();
        this.creationDate                = props.getCreationDate();
        this.dateEnd                     = props.getDateEnd();
        this.dateStart                   = props.getDateStart();
        this.format                      = props.getFormat();
        this.keywords                    = props.getKeywords();
        this.language                    = props.getLanguage();
        this.license                     = props.getLicense();
        this.lineage                     = props.getLineage();
        this.metadataDate                = props.getMetadataDate();
        this.metadataLanguage            = props.getMetadataLanguage();
        this.metadataPointOfContactEmail = props.getMetadataPointOfContactEmail();
        this.metadataPointOfContactName  = props.getMetadataPointOfContactName();
        this.parentId                    = props.getParentId();
        this.publicAccessLimitations     = props.getPublicAccessLimitations();
        this.publicationDate             = props.getPublicationDate();
        this.publisherEmail              = props.getPublisherEmail();
        this.publisherName               = props.getPublisherName();
        this.referenceSystem             = props.getReferenceSystem();
        this.resourceLocator             = props.getResourceLocator();
        this.revisionDate                = props.getRevisionDate();
        this.scale                       = props.getScale();
        this.spatialDataServiceType      = props.getSpatialDataServiceType();
        this.spatialResolution           = props.getSpatialResolution();
        this.title                       = props.getTitle();
        this.topicCategory               = props.getTopicCategory();
        this.type                        = props.getType();
        this.version                     = props.getVersion();

        this.geometry = feature.getGeometry();

    }

    @Schema(description = "An abstract of the resource", example = "")
    private String abstractText;

    @Schema(description = "Auxiliary files or additional resources to the dataset", example = "")
    private String additionalResources;

    @Schema(description = "Degree of conformity with the implementing rules/standard of the metadata followed", example = "")
    private String conformity;

    @Schema(description = "Provides information about the datasets that the service operates on", example = "")
    private String coupledResource;

    @Schema(
        description = "A point or period of time associated with the creation event in the lifecycle of the resource",
        example = "2020-06-02"
    )
    private String creationDate;

    @Schema(description = "The temporal extent of the resource (end date)", example = "2020-06-02")
    private String dateEnd;

    @Schema(description = "The temporal extent of the resource (start date)", example = "2020-06-02")
    private String dateStart;

    @Schema(description = "The file format, physical medium, or dimensions of the resource", example = "")
    private String format;

    @Schema(description = "The topic of the resource", example = "")
    private List<String> keywords;

    @Schema(description = "A language of the resource", example = "")
    private String language;

    @Schema(description = "Information about resource licensing", example = "")
    private String license;

    @Schema(description = "General explanation of the data producer’s knowledge about the lineage of a dataset", example = "")
    private String lineage;

    @Schema(description = "The date which specifies when the metadata record was created or updated", example = "2020-06-02")
    private String metadataDate;

    @Schema(description = "The language in which the metadata elements are expressed", example = "")
    private String metadataLanguage;

    @Schema(
        description = "The email of the organization responsible for the creation and maintenance of the metadata"
    )
    private String metadataPointOfContactEmail;

    @Schema(
        description = "The name of the organization responsible for the creation and maintenance of the metadata"
    )
    private String metadataPointOfContactName;

    @Schema(description = "Provides the ID of a parent dataset", example = "")
    private String parentId;

    @Schema(description = "Information on the limitations and the reasons for them", example = "")
    private String publicAccessLimitations;

    @Schema(
        description = "A point or period of time associated with the publication even in the "
                    + "lifecycle of the resource",
        example = "2020-06-02"
    )
    private String publicationDate;

    @Schema(description = "Email of an entity responsible for making the resource available", example = "")
    private String publisherEmail;

    @Schema(description = "Name of an entity responsible for making the resource available", example = "")
    private String publisherName;

    @Schema(description = "Information about the reference system", example = "")
    private String referenceSystem;

    @Schema(
        description = "The ‘navigation section’ of a metadata record which point users to the location (URL) "
                    + "where the data can be downloaded, or to where additional information about the resource "
                    + "may be provided",
        example = ""
    )
    private String resourceLocator;


    @Schema(
        description = "A point or period of time associated with the revision event in the "
                    + "lifecycle of the resource",
        example = "2020-06-02"
    )
    private String revisionDate;

    @Schema(description = "Denominator of the scale of the data set", example = "")
    private String scale;

    @Schema(description = "The nature or genre of the service", example = "")
    private String spatialDataServiceType;


    @Schema(description = "Spatial resolution refers to the level of detail of the data set", example = "")
    private String spatialResolution;

    @Schema(description = "A name given to the resource", example = "")
    private String title;

    @Schema(
        description = "A high-level classification scheme to assist in the grouping and topic-based "
                    + "search of available spatial data resources",
        example = ""
    )
    private String topicCategory;

    @Schema(description = "The nature or genre of the resource", example = "")
    private String type;

    @Schema(description = "Version of the resource", example = "")
    private String version;

    @Schema(implementation = GeometryAsJson.class, description = "Geometry as GeoJSON")
    private Geometry geometry;

}
