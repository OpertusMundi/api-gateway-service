package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.service.ProviderAssetService;
import feign.FeignException;

@RestController
public class ProviderAssetControllerImpl extends BaseController implements ProviderAssetController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAssetController.class);

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired
    private ProviderAssetService providerAssetService;
    
    @Override
    public ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        String pid, UUID resourceKey, HttpServletResponse response
    ) throws IOException {
        final Path path = this.providerAssetService.resolveAssetAdditionalResource(pid, resourceKey);
        final File file = path.toFile();
       
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length", Long.toString(file.length()));

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }
    
    public ResponseEntity<StreamingResponseBody> getMetadataProperty(
        String pid, UUID resourceKey, String propertyName, HttpServletResponse response
    ) throws IOException {
        final MetadataProperty property = this.providerAssetService.resolveAssetMetadataProperty(
            pid, resourceKey, propertyName
        );
        
        final File file = property.getPath().toFile();
       
        String contentType = Files.probeContentType(property.getPath());
        if (contentType == null) {
            contentType = property.getType().getMediaType();
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length", Long.toString(file.length()));

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }
    
    @Override
    public BaseResponse deleteAsset(UUID id) {
        try {
            this.catalogueClient.getObject().deletePublished(id);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

}
