package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerServiceBillingDto;
import eu.opertusmundi.common.model.payment.provider.ProviderServiceBillingDto;

public class ServiceBillingEndPoints {

    public static class ConsumerServiceBillingResponse extends RestResponse<ConsumerServiceBillingDto> {

    }

    public static class ProviderServiceBillingResponse extends RestResponse<ProviderServiceBillingDto> {

    }

    public static class ServiceUseStats extends RestResponse<ServiceUseStatsDto> {

    }

}
