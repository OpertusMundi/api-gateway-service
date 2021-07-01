package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInDto;
import eu.opertusmundi.common.model.payment.CardDirectPayInDto;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PayOutDto;

public class PaymentEndPoints {

    public static class CheckoutOrderResponse extends RestResponse<OrderDto> {

    }

    public static class CardCollectionResponse extends RestResponse<List<CardDto>> {

    }

    public static class CardRegistrationRequestResponse extends RestResponse<CardRegistrationDto> {

    }

    public static class CardRegistrationResponse extends RestResponse<CardDto> {

    }

    public static class BankWirePayInResponse extends RestResponse<BankwirePayInDto> {

    }

    public static class CardDirectPayInResponse extends RestResponse<CardDirectPayInDto> {

    }

    public static class PayInCollectionResponse extends RestResponse<PageResultDto<PayInDto>> {

    }

    public static class PayInItemResponse extends RestResponse<PayInItemDto> {

    }

    public static class PayInItemCollectionResponse extends RestResponse<PageResultDto<PayInItemDto>> {

    }

    public static class OrderResponse extends RestResponse<OrderDto> {

    }

    public static class OrderCollectionResponse extends RestResponse<PageResultDto<OrderDto>> {

    }

    public static class PayOutResponse extends RestResponse<PayOutDto> {

    }

    public static class PayOutCollectionResponse extends RestResponse<PageResultDto<PayOutDto>> {

    }

}
