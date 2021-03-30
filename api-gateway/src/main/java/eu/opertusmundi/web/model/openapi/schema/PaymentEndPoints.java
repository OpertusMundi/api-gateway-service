package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInDto;
import eu.opertusmundi.common.model.payment.CardDirectPayInDto;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;

public class PaymentEndPoints {

    public static class CheckoutOrderResponse extends RestResponse<OrderDto> {

    }

    public static class BankWirePayInResponse extends RestResponse<BankwirePayInDto> {

    }

    public static class CardCollectionResponse extends RestResponse<List<CardDto>> {

    }

    public static class CardRegistrationRequestResponse extends RestResponse<CardRegistrationDto> {

    }

    public static class CardRegistrationResponse extends RestResponse<CardDto> {

    }

    public static class CardDirectPayInResponse extends RestResponse<CardDirectPayInDto> {

    }

}
