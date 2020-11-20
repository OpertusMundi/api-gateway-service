package eu.opertusmundi.web.model.catalogue.server;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.web.model.catalogue.client.CatalogueAddItemCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemStoreStatistics;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueFeature {

    public CatalogueFeature(CatalogueAddItemCommandDto command) {
        this.id       = command.getId() == null ? "" : command.getId().toString();
        this.type     = "Feature";
        this.geometry = command.getGeometry();

        this.properties = new CatalogueFeatureProperties();

        this.properties.setAbstractText(command.getAbstractText());
        this.properties.setAdditionalResources(command.getAdditionalResources());
        this.properties.setConformity(command.getConformity());
        this.properties.setCoupledResource(command.getCoupledResource());
        this.properties.setCreationDate(command.getCreationDate());
        this.properties.setDateEnd(command.getDateEnd());
        this.properties.setDateStart(command.getDateStart());
        this.properties.setFormat(command.getFormat());
        this.properties.setKeywords(command.getKeywords());
        this.properties.setLanguage(command.getLanguage());
        this.properties.setLicense(command.getLicense());
        this.properties.setLineage(command.getLineage());
        this.properties.setMetadataDate(command.getMetadataDate());
        this.properties.setMetadataLanguage(command.getMetadataLanguage());
        this.properties.setMetadataPointOfContactEmail(command.getMetadataPointOfContactEmail());
        this.properties.setMetadataPointOfContactName(command.getMetadataPointOfContactName());
        this.properties.setParentId(command.getParentId());
        this.properties.setPublicAccessLimitations(command.getPublicAccessLimitations());
        this.properties.setPublicationDate(command.getPublicationDate());
        this.properties.setPublisherEmail(command.getPublisherEmail());
        this.properties.setPublisherId(command.getPublisherId());
        this.properties.setPublisherName(command.getPublisherName());
        this.properties.setReferenceSystem(command.getReferenceSystem());
        this.properties.setResourceLocator(command.getResourceLocator());
        this.properties.setRevisionDate(command.getRevisionDate());
        this.properties.setScale(command.getScale());
        this.properties.setSpatialDataServiceType(command.getSpatialDataServiceType());
        this.properties.setSpatialResolution(command.getSpatialResolution());
        this.properties.setTitle(command.getTitle());
        this.properties.setTopicCategory(command.getTopicCategory());
        this.properties.setType(command.getType());
        this.properties.setVersion(command.getVersion());

        // Initialize with an empty statistics object
        this.properties.setStoreStatistics(new CatalogueItemStoreStatistics());

        // Initialize with an empty collection. Caller must compute the
        // effective pricing models
        this.properties.setPricingModels(new ArrayList<BasePricingModelCommandDto>());
    }

    String id;

    String type;

    Geometry geometry;

    CatalogueFeatureProperties properties;

}
