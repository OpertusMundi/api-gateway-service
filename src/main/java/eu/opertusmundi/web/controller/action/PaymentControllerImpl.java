package eu.opertusmundi.web.controller.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jayway.jsonpath.JsonPath;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.BpmServerFeignClient;
import eu.opertusmundi.web.model.order.CreatePaymentResult;
import eu.opertusmundi.web.model.order.OrderDto;
import eu.opertusmundi.web.model.order.OrderItemDto;

//@RestController
public class PaymentControllerImpl implements PaymentController {

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.cli.endpoint.secret}")
    private String cliEndpointKey;

    @PostConstruct
    private void postConstruct() {
        Stripe.apiKey = this.secretKey;
    }

    @Autowired
    BpmServerFeignClient bpmServerClient;

    @Override
    public RestResponse<?> create(UUID id, OrderDto order) {
        try {
        final PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
            .setCurrency("EUR")
            .setAmount(new Long(this.calculateOrderAmount(order.getItems())))
            .putMetadata("opertus-mundi-order", order.getId().toString())
            .build();

            // Create a PaymentIntent with the order amount and currency
            final PaymentIntent intent = PaymentIntent.create(createParams);

            // Send publishable key and PaymentIntent details to client
            final CreatePaymentResult result = new CreatePaymentResult(this.publishableKey, intent.getClientSecret());

            return RestResponse.result(result);
        } catch (final StripeException e) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "Operation has failed");
        }
    }

    @Override
    public RestResponse<?> handleEvent(HttpServletRequest request, HttpServletResponse response, String payload) {
        final String sigHeader = request.getHeader("Stripe-Signature");

        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, this.cliEndpointKey);
        } catch (final SignatureVerificationException e) {
            // Invalid signature
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return RestResponse.error(BasicMessageCode.BadRequest, "Payment failed");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" :
                // Fulfill any orders, e-mail receipts, etc
                // To cancel the payment you will need to issue a Refund
                // (https://stripe.com/docs/api/refunds)
                final String orderId = JsonPath.read(payload, "$.data.object.metadata.opertus-mundi-order");
                final String idempotencyKey = JsonPath.read(payload, "$.request.idempotency_key");
                System.out.println("💰Payment received! Order : " + orderId);

                // Update workflow
                final CorrelationMessageDto correlationMessage = new CorrelationMessageDto();
                correlationMessage.setMessageName("payment-received-message");
                correlationMessage.setBusinessKey(String.format("order-%s", orderId));

                // Set custom variables
                final Map<String, VariableValueDto> processVariables = new HashMap<String, VariableValueDto>();

                final VariableValueDto idempotencyKeyVariable = new VariableValueDto();
                idempotencyKeyVariable.setValue(idempotencyKey);
                idempotencyKeyVariable.setType("String");

                processVariables.put("idempotency-key", idempotencyKeyVariable);

                correlationMessage.setProcessVariables(processVariables);

                this.bpmServerClient.correlateMessage(correlationMessage);
                break;
            case "payment_intent.payment_failed" :
                System.out.println("❌ Payment failed.");
                break;
            default :
                // Unexpected event type
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
                return RestResponse.error(BasicMessageCode.BadRequest, "Event not supported");
        }

        return RestResponse.success();
    }

    private Long calculateOrderAmount(List<OrderItemDto> items) {
        return items.stream().mapToLong(i -> i.getPrice()).sum();
    }

}
