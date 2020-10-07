package eu.opertusmundi.web.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.config.EmailServiceFeignClientConfiguration;
import eu.opertusmundi.web.model.email.MessageDto;

@FeignClient(
    name = "${opertusmundi.feign.email-service.name}",
    url = "${opertusmundi.feign.email-service.url}",
    configuration = EmailServiceFeignClientConfiguration.class
)
public interface EmailServiceFeignClient {

    /**
     * Send mail
     *
     * @param message Message configuration object
     *
     * @return An instance of {@link BaseResponse}
     */
    @PostMapping(value = "/v1/email/send")
    ResponseEntity<BaseResponse> sendMail(@RequestBody(required = true) MessageDto<?> message);

    /**
     * Renders an email template in text format without sending it
     *
     * @param message Message configuration object
     *
     * @return An instance of {@link RestResponse}
     */
    @PostMapping(value = "/v1/email/render/text")
    RestResponse<String> renderText(@RequestBody(required = true) MessageDto<?> message);

    /**
     * Renders an email template in HTML format without sending it
     *
     * @param message Message configuration object
     *
     * @return An instance of {@link RestResponse}
     */
    @PostMapping(value = "/v1/email/render/html")
    RestResponse<String> renderHtml(@RequestBody(required = true) MessageDto<?> message);

}
