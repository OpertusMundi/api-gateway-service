package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.payment.consumer.ConsumerSubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.provider.ProviderSubscriptionBillingDto;

public class SubscriptionBillingEndPoints {

    public static class ConsumerSubscriptionBillingResponse extends RestResponse<ConsumerSubscriptionBillingDto> {

    }

    public static class ProviderSubscriptionBillingResponse extends RestResponse<ProviderSubscriptionBillingDto> {

    }

}
