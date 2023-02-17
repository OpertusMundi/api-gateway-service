package eu.opertusmundi.test.support.utils;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BinaryResponsePayload {

    final byte[] data;

    public static BinaryResponsePayload from(String location) throws IOException {
        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource       resource       = resourceLoader.getResource(location);

        return BinaryResponsePayload.from(resource);
    }

    public static BinaryResponsePayload from(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new BinaryResponsePayload(in.readAllBytes());
        }
    }

}
