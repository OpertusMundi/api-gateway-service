package eu.opertusmundi.web.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.opertusmundi.common.service.mangopay.MangoPayWebhookHelper;

/**
 * {@link ApplicationRunner} for configuring MANGOPAY web hooks
 */
@ConditionalOnProperty(name = "opertusmundi.payments.mangopay.web-hook.create-on-startup")
@Component
public class WebhookApplicationRunner implements ApplicationRunner {

    @Value("${opertus-mundi.base-url}")
    private String baseUrl;

    @Autowired
    private MangoPayWebhookHelper webhookHelper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.webhookHelper.enableAll(baseUrl);
    }

}
