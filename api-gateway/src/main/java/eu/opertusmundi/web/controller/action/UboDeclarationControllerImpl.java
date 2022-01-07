package eu.opertusmundi.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.kyc.CustomerVerificationException;
import eu.opertusmundi.common.model.kyc.UboCommandDto;
import eu.opertusmundi.common.model.kyc.UboDeclarationCommand;
import eu.opertusmundi.common.model.kyc.UboDeclarationDto;
import eu.opertusmundi.common.model.kyc.UboDto;
import eu.opertusmundi.common.model.kyc.UboQueryCommand;
import eu.opertusmundi.common.service.mangopay.CustomerVerificationService;

@RestController
public class UboDeclarationControllerImpl extends BaseController implements UboDeclarationController {

    private static final Logger logger = LoggerFactory.getLogger(UboDeclarationControllerImpl.class);

    @Autowired
    private CustomerVerificationService customerVerificationService;

    @Override
    public RestResponse<?> findAllDeclarations(int pageIndex, int pageSize, EnumCustomerType customerType) {
        try {
            final UboQueryCommand command = UboQueryCommand.builder()
                .pageIndex(pageIndex < 0 ? 0 : pageIndex)
                .pageSize(pageSize < 1 ? 10 : pageSize)
                .customerKey(this.currentUserKey())
                .customerType(customerType)
                .build();

            final PageResultDto<UboDeclarationDto> result  = this.customerVerificationService.findAllUboDeclarations(command);

            return RestResponse.result(result);
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<UboDeclarationDto> findOneDeclaration(String uboDeclarationId, EnumCustomerType customerType) {
        try {
            final UboDeclarationCommand command = UboDeclarationCommand.builder()
                .customerKey(this.currentUserKey())
                .customerType(customerType)
                .uboDeclarationId(uboDeclarationId)
                .build();

            final UboDeclarationDto declaration = this.customerVerificationService.findOneUboDeclaration(command);

            if (declaration == null) {
                return RestResponse.notFound();
            }

            return RestResponse.result(declaration);
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<UboDeclarationDto> createUboDeclaration(EnumCustomerType customerType) {
        try {
            final UboDeclarationCommand command = UboDeclarationCommand.builder()
                .customerKey(this.currentUserKey())
                .customerType(customerType)
                .build();

            final UboDeclarationDto result = this.customerVerificationService.createUboDeclaration(command);

            return RestResponse.result(result);
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<UboDto> addUbo(String uboDeclarationId, UboCommandDto command, BindingResult validationResult) {
        try {
            command.setCustomerKey(this.currentUserKey());
            command.setUboDeclarationId(uboDeclarationId);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final UboDto result = this.customerVerificationService.addUbo(command);

            return RestResponse.result(result);
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<UboDto> updateUbo(String uboDeclarationId, String uboId, UboCommandDto command, BindingResult validationResult) {
        try {
            command.setCustomerKey(this.currentUserKey());
            command.setUboDeclarationId(uboDeclarationId);
            command.setUboId(uboId);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final UboDto result = this.customerVerificationService.updateUbo(command);

            return RestResponse.result(result);
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse removeUbo(String uboDeclarationId, String uboId, EnumCustomerType customerType) {
        try {
            final UboCommandDto command = new UboCommandDto();
            command.setCustomerKey(this.currentUserKey());
            command.setCustomerType(customerType);
            command.setUboDeclarationId(uboDeclarationId);
            command.setUboId(uboId);

            this.customerVerificationService.removeUbo(command);

            return RestResponse.success();
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<UboDeclarationDto> submitUboDeclaration(String uboDeclarationId, EnumCustomerType customerType) {
        try {
            final UboDeclarationCommand command = UboDeclarationCommand.of(this.currentUserKey(), customerType, uboDeclarationId);
            final UboDeclarationDto     result  = this.customerVerificationService.submitUboDeclaration(command);

            return RestResponse.result(result);
        } catch (final CustomerVerificationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

}
