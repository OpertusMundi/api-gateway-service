package eu.opertusmundi.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.kyc.CustomerVerificationException;
import eu.opertusmundi.common.model.kyc.CustomerVerificationMessageCode;
import eu.opertusmundi.common.model.kyc.KycDocumentCommand;
import eu.opertusmundi.common.model.kyc.KycDocumentCommandDto;
import eu.opertusmundi.common.model.kyc.KycDocumentDto;
import eu.opertusmundi.common.model.kyc.KycDocumentPageCommandDto;
import eu.opertusmundi.common.model.kyc.KycQueryCommand;
import eu.opertusmundi.common.service.CustomerVerificationService;

@RestController
public class KycDocumentControllerImpl extends BaseController implements KycDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(KycDocumentControllerImpl.class);

    private final List<String> allowedMimeTypes = Arrays.asList(new String[]{
        "application/pdf",
        "image/jpeg",
        "image/png"
    });

    @Autowired
    private CustomerVerificationService customerVerificationService;

    @Override
    public RestResponse<?> findAllDocuments(EnumCustomerType customerType, int pageIndex, int pageSize) {
        try {
            this.verifyRole(customerType);

            final KycQueryCommand command = KycQueryCommand.builder()
                .pageIndex(pageIndex < 0 ? 0 : pageIndex)
                .pageSize(pageSize < 1 ? 10 : pageSize)
                .customerKey(this.currentUserKey())
                .customerType(customerType)
                .build();

            final PageResultDto<KycDocumentDto> result  = this.customerVerificationService.findAllKycDocuments(command);

            return RestResponse.result(result);
        } catch (final Exception ex) {
            throw this.wrapException("Find KYC Documents", ex, null);
        }
    }

    @Override
    public RestResponse<KycDocumentDto> findOneDocument(String kycDocumentId, EnumCustomerType customerType) {
        KycDocumentCommand command = null;
        try {
            command = KycDocumentCommand.builder()
                .customerKey(this.currentUserKey())
                .kycDocumentId(kycDocumentId)
                .customerType(customerType)
                .build();

            verifyRole(customerType);

            final KycDocumentDto document = this.customerVerificationService.findOneKycDocument(command);

            if (document == null) {
                return RestResponse.notFound();
            }

            return RestResponse.result(document);
        } catch (final Exception ex) {
            throw this.wrapException("Find KYC Document", ex, command != null ? command : kycDocumentId);
        }
    }

    @Override
    public RestResponse<KycDocumentDto> createKycDocument(@Valid KycDocumentCommandDto command, BindingResult validationResult) {
        try {
            command.setUserKey(this.currentUserKey());
            verifyRole(command.getCustomerType());

            final KycDocumentDto result = this.customerVerificationService.createKycDocument(command);

            return RestResponse.result(result);
        } catch (final Exception ex) {
            throw this.wrapException("Create KYC Document", ex, command);
        }
    }

    @Override
    public BaseResponse addPage(String kycDocumentId, MultipartFile file, KycDocumentPageCommandDto command, BindingResult validationResult) {
        try {
            if (file == null || file.getSize() == 0) {
                return RestResponse.error(CustomerVerificationMessageCode.PAGE_FILE_MISSING, "A file is required");
            }
            if (file.getSize() > 7 * 1024 * 1024) {
                validationResult.reject("FileTooLarge", file.getOriginalFilename());
            }
            if (file.getSize() < 1024) {
                validationResult.reject("FileTooSmall", file.getOriginalFilename());
            }

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            verifyRole(command.getCustomerType());

            byte[] data;
            try (final InputStream input = new ByteArrayInputStream(file.getBytes())) {
                data = IOUtils.toByteArray(input);
            }

            final String mimeType = this.detectMimeType(data);

            if (mimeType == null) {
                validationResult.reject("FileTypeNotSupported", file.getOriginalFilename());
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            command.setUserKey(this.currentUserKey());
            command.setKycDocumentId(kycDocumentId);
            command.setFileName(file.getOriginalFilename());
            command.setFileSize(file.getSize());
            command.setFileType(mimeType);

            this.customerVerificationService.addPage(command, data);

            return RestResponse.success();
        } catch (final Exception ex) {
            throw this.wrapException("Add KYC Document Page", ex, command);
        }
    }

    @Override
    public RestResponse<KycDocumentDto> submitKycDocument(String kycDocumentId,KycDocumentCommandDto command, BindingResult validationResult) {
        try {
            command.setUserKey(this.currentUserKey());
            verifyRole(command.getCustomerType());

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final KycDocumentCommand serviceCommand = KycDocumentCommand.of(this.currentUserKey(), command.getCustomerType(), kycDocumentId);
            final KycDocumentDto     result         = this.customerVerificationService.submitKycDocument(serviceCommand);

            return RestResponse.result(result);
        } catch (final Exception ex) {
            throw this.wrapException("Submit KYC Document", ex, command);
        }
    }

    private String detectMimeType(byte[] data) {
        final Tika tika = new Tika();
        final String mimeType  = tika.detect(data);

        if(!allowedMimeTypes.contains(mimeType)) {
            return null;
        }

        return mimeType;
    }

    private void verifyRole(EnumCustomerType customerType) {
        switch (customerType) {
            case CONSUMER :
                if (!this.hasRole(EnumRole.ROLE_CONSUMER)) {
                    throw new AccessDeniedException("Access Denied");
                }
                break;
            case PROVIDER :
                if (!this.hasRole(EnumRole.ROLE_PROVIDER)) {
                    throw new AccessDeniedException("Access Denied");
                }
                break;
        }
    }

    /**
     * Wraps an exception with {@link CustomerVerificationException}
     *
     * @param operation
     * @param ex
     * @return
     */
    private CustomerVerificationException wrapException(String operation, Exception ex, Object command) {
        final String commandText = command == null ? "-" : command.toString();

        // Ignore controller/service exceptions
        if (ex instanceof CustomerVerificationException) {
            return (CustomerVerificationException) ex;
        }

        if (ex instanceof AccessDeniedException) {
            return new CustomerVerificationException(CustomerVerificationMessageCode.ACCESS_DENIED);
        }

        // Global exception handler
        final String message = String.format("Operation has failed. [operation=%, command=[%s]]", operation, commandText);

        logger.error(message, ex);

        return new CustomerVerificationException(CustomerVerificationMessageCode.UNKNOWN, message, ex, false);
    }

}
