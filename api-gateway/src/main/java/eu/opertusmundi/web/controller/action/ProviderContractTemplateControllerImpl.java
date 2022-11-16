package eu.opertusmundi.web.controller.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.helpdesk.EnumMasterContractSortField;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractQueryDto;
import eu.opertusmundi.common.model.contract.provider.EnumProviderContractSortField;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractQuery;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.service.contract.MasterTemplateContractService;
import eu.opertusmundi.common.service.contract.ProviderTemplateContractService;
import eu.opertusmundi.web.validation.ProviderTemplateContractValidator;

@RestController
public class ProviderContractTemplateControllerImpl extends BaseController implements ProviderContractTemplateController {

    private final ProviderTemplateContractValidator contractValidator;
    private final MasterTemplateContractService     masterContractService;
    private final ProviderTemplateContractService   templateContractService;

    @Autowired
    public ProviderContractTemplateControllerImpl(
        ProviderTemplateContractValidator contractValidator,
        MasterTemplateContractService     masterContractService,
        ProviderTemplateContractService   templateContractService
    ) {
        this.contractValidator       = contractValidator;
        this.masterContractService   = masterContractService;
        this.templateContractService = templateContractService;
    }

    @Override
    public RestResponse<?> findAllMasterContracts(
        int page,
        int size,
        String title,
        EnumMasterContractSortField orderBy,
        EnumSortingOrder order
    ) {
        final MasterContractQueryDto query = MasterContractQueryDto.builder()
            .page(page)
            .size(size)
            .title(title)
            .status(new HashSet<>(Arrays.asList(EnumContractStatus.ACTIVE)))
            .order(order)
            .orderBy(orderBy)
            .providerKey(this.currentUserParentKey())
            .build();


        final PageResultDto<MasterContractDto> result = masterContractService.findAll(query);

        result.getItems().stream().forEach(MasterContractDto::removeHelpdeskData);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findOneMasterContract(UUID contractKey) {
        final MasterContractDto result = masterContractService.findOneByKey(
            this.currentUserParentKey(), contractKey
        ).orElse(null);

        result.removeHelpdeskData();

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findAllDrafts(int page, int size, EnumProviderContractSortField orderBy, EnumSortingOrder order) {
        final PageResultDto<ProviderTemplateContractDto> result = templateContractService.findAllDrafts(
            this.currentUserKey(), page, size, orderBy, order
        );

        result.getItems().stream().forEach(ProviderTemplateContractDto::removeHelpdeskData);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findOneDraft(UUID key) {
        final ProviderTemplateContractDto result = templateContractService.findOneDraft(this.currentUserKey(), key);

        if (result == null) {
            return RestResponse.notFound();
        }

        result.removeHelpdeskData();

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> createDraft(ProviderTemplateContractCommandDto command, BindingResult validationResult) {
        try {
            command.setUserId(this.currentUserId());
            command.setUserKey(this.currentUserParentKey());

            this.contractValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final ProviderTemplateContractDto result = this.templateContractService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> updateDraft(UUID key, ProviderTemplateContractCommandDto command, BindingResult validationResult) {
        try {
            command.setDraftKey(key);
            command.setUserId(this.currentUserId());
            command.setUserKey(this.currentUserParentKey());

            this.contractValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final ProviderTemplateContractDto result = this.templateContractService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<ProviderTemplateContractDto> deleteDraft(UUID key) {
        try {
            final ProviderTemplateContractDto result = templateContractService.deleteDraft(this.currentUserId(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> publishDraft(UUID key) {
        try {
            final ProviderTemplateContractDto result = this.templateContractService.publishDraft(this.currentUserKey(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> findAllTemplates(int page, int size, EnumProviderContractSortField orderBy, EnumSortingOrder order) {
        // Get all publisher (vendor) templates
        final ProviderTemplateContractQuery query = ProviderTemplateContractQuery.builder()
            .page(page)
            .size(size)
            .order(order)
            .orderBy(orderBy)
            .providerKey(this.currentUserParentKey())
            .build();

        final PageResultDto<ProviderTemplateContractDto> result = templateContractService.findAll(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findOneTemplate(UUID key) {
        final ProviderTemplateContractDto result = this.templateContractService.findOneByKey(
            this.currentUserParentId(), key
        ).orElse(null);

        if (result == null) {
            return RestResponse.notFound();
        }

        return RestResponse.result(result);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> printTemplate(
        UUID templateKey, HttpServletResponse response
    ) {
        try {
            // Check if template exists
            final ProviderTemplateContractDto template = this.templateContractService.findOneByKey(
                this.currentUserParentId(), templateKey
            ).orElse(null);

            if (template == null) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }

            // Print template
            final ProviderContractCommand command = ProviderContractCommand.builder()
                .providerKey(this.currentUserParentKey())
                .contractKey(templateKey)
                .build();

            final byte[] result = this.templateContractService.print(command);

            response.setHeader("Content-Disposition", String.format("attachment; filename=%s.pdf", template.getTitle()));
            response.setHeader("Content-Type", MediaType.APPLICATION_PDF_VALUE);
            if (result.length < 1024 * 1024) {
                response.setHeader("Content-Length", Long.toString(result.length));
            }

            final StreamingResponseBody stream = out -> {
                    IOUtils.write(result, out);
            };

            return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
        } catch (final FileSystemException ex) {
            throw ex;
        }
    }

    @Override
    public RestResponse<?> deactivate(UUID key) {
        try {
            final ProviderTemplateContractDto result = this.templateContractService.deactivate(this.currentUserKey(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> createDraftFromTemplate(UUID key) {
        try {
            final ProviderTemplateContractDto result = this.templateContractService.createFromMasterContract(this.currentUserKey(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> acceptDefaultContract(UUID key) {
        try {
            final ProviderTemplateContractDto result = this.templateContractService.acceptDefaultContract(this.currentUserKey(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }
}
