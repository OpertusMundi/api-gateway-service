package eu.opertusmundi.test.support.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponsePayload {

    final String data;

    public static ResponsePayload from(String location) throws IOException {
        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource       resource       = resourceLoader.getResource(location);

        return ResponsePayload.from(resource);
    }

    public static ResponsePayload from(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new ResponsePayload(StreamUtils.copyToString(in, StandardCharsets.UTF_8));
        }
    }

    public JsonNode asJson() throws JsonMappingException, JsonProcessingException {
        return (new ObjectMapper()).readTree(this.data);
    }

}
