package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerBankwirePayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerFreePayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskBankwirePayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskFreePayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;

public class PaymentEndPoints {

    public static class CheckoutOrderResponse extends RestResponse<OrderDto> {

    }

    public static class CheckoutPayInResponse extends RestResponse<PayInDto> {

    }

    public static class CardCollectionResponse extends RestResponse<List<CardDto>> {

    }

    public static class CardRegistrationRequestResponse extends RestResponse<CardRegistrationDto> {

    }

    public static class CardRegistrationResponse extends RestResponse<CardDto> {

    }

    public static class ConsumerFreePayInResponse extends RestResponse<ConsumerFreePayInDto> {

    }

    public static class HelpdeskFreePayInResponse extends RestResponse<HelpdeskFreePayInDto> {

    }

    public static class ConsumerBankWirePayInResponse extends RestResponse<ConsumerBankwirePayInDto> {

    }

    public static class HelpdeskBankWirePayInResponse extends RestResponse<HelpdeskBankwirePayInDto> {

    }

    public static class ConsumerCardDirectPayInResponse extends RestResponse<ConsumerCardDirectPayInDto> {

    }

    public static class HelpdeskCardDirectPayInResponse extends RestResponse<HelpdeskCardDirectPayInDto> {

    }

    public static class ConsumerPayInCollectionResponse extends RestResponse<PageResultDto<ConsumerPayInDto>> {

    }

    public static class ProviderPayInItemResponse extends RestResponse<ProviderPayInItemDto> {

    }

    public static class ProviderPayInItemCollectionResponse extends RestResponse<PageResultDto<ProviderPayInItemDto>> {

    }

    public static class ConsumerOrderResponse extends RestResponse<ConsumerOrderDto> {

    }

    public static class ProviderOrderResponse extends RestResponse<ProviderOrderDto> {

    }

    public static class ConsumerOrderCollectionResponse extends RestResponse<PageResultDto<ConsumerOrderDto>> {

    }

    public static class ProviderOrderCollectionResponse extends RestResponse<PageResultDto<ProviderOrderDto>> {

    }

    public static class PayOutResponse extends RestResponse<PayOutDto> {

    }

    public static class PayOutCollectionResponse extends RestResponse<PageResultDto<PayOutDto>> {

    }

}
