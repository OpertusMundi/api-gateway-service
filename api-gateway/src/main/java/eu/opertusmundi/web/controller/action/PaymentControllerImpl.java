package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.CartConstants;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommandDto;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.UserCommand;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
import eu.opertusmundi.common.service.CartService;
import eu.opertusmundi.common.service.PaymentService;

@RestController
public class PaymentControllerImpl extends BaseController implements PaymentController {
         
    @Autowired
    private CartService cartService;
    
    @Autowired
    private PaymentService paymentService;

    @Override
    public RestResponse<?> checkout(HttpSession session) {
        // Get current cart
        final UUID cartKey = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);
        CartDto    cart    = this.cartService.getCart(cartKey);
        
        // Create order
        final OrderDto order = this.paymentService.createOrderFromCart(cart);
       
        return RestResponse.result(order);
    }

    @Override
    public RestResponse<?> createBankwirePayIn(UUID orderKey, HttpSession session) {
        final BankwirePayInCommand payInCommand = BankwirePayInCommand.builder()
            .userKey(this.currentUserKey())
            .orderKey(orderKey)
            .build();

        final PayInDto result = this.paymentService.createPayInBankwireForOrder(payInCommand);

        // If payment creation was successful, reset cart
        final CartDto cart = this.cartService.getCart();
        session.setAttribute(CartConstants.CART_SESSION_KEY, cart.getKey());
        
        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> getCards(Integer page, Integer size) {
        final UserPaginationCommand command = UserPaginationCommand.builder()
            .userKey(this.currentUserKey())
            .page(page == null ? 1 : page + 1)
            .size(size == null ? 10 : size)
            .build();

        final List<CardDto> result = this.paymentService.getRegisteredCards(command);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> createCardRegistration() {
        final UserCommand command = UserCommand.of(this.currentUserKey());

        final CardRegistrationDto result = this.paymentService.createCardRegistration(command);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> completeCardRegistration(CardRegistrationCommandDto command, BindingResult validationResult) {
        command.setUserKey(this.currentUserKey());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }
        
        final String cardId = this.paymentService.registerCard(command);

        return RestResponse.result(cardId);
    }

    @Override
    public RestResponse<?> createCardDirectPayIn(
        UUID orderKey, CardDirectPayInCommandDto command, BindingResult validationResult, HttpSession session
    ) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final CardDirectPayInCommand payInCommand = CardDirectPayInCommand.builder()
            .userKey(this.currentUserKey())
            .orderKey(orderKey)
            .cardId(command.getCardId())
            .build();

        final PayInDto result = this.paymentService.createPayInCardDirectForOrder(payInCommand);

        // If payment has been executed successfully, reset cart. If 3-D Secure
        // validation is required or the transaction has failed, the cart should
        // not be reseted.
        if (result.getExecutedOn() != null && result.getStatus() == EnumTransactionStatus.SUCCEEDED) {
            final CartDto cart = this.cartService.getCart();
            session.setAttribute(CartConstants.CART_SESSION_KEY, cart.getKey());
        }
        
        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> getPayIn(UUID payInKey) {
        final PayInDto result = this.paymentService.getPayIn(this.currentUserId(), payInKey);

        return RestResponse.result(result);
    }

}
