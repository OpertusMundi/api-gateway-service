package eu.opertusmundi.web.controller.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.helpdesk.EnumMasterContractSortField;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractQueryDto;
import eu.opertusmundi.common.model.contract.provider.EnumProviderContractSortField;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractQuery;
import eu.opertusmundi.common.service.contract.MasterTemplateContractService;
import eu.opertusmundi.common.service.contract.ProviderTemplateContractService;

@RestController
public class ProviderContractControllerImpl extends BaseController implements ProviderContractController {

    @Autowired
    private MasterTemplateContractService   masterContractService;

    @Autowired
    private ProviderTemplateContractService templateContractService;

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
            .build();

        final PageResultDto<MasterContractDto> result = masterContractService.findAll(query);

        result.getItems().stream().forEach(MasterContractDto::removeHelpdeskData);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findOneMasterContract(UUID key) {
        final MasterContractDto result = masterContractService.findOneByKey(key).orElse(null);

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
    public RestResponse<?> createDraft(@Valid ProviderTemplateContractCommandDto command, BindingResult validationResult) {
        try {
            command.setUserId(this.currentUserId());

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final ProviderTemplateContractDto result = this.templateContractService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> updateDraft(UUID key, @Valid ProviderTemplateContractCommandDto command, BindingResult validationResult) {
        try {
            command.setDraftKey(key);
            command.setUserId(this.currentUserId());

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
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
            final ProviderTemplateContractDto result = this.templateContractService.publishDraft(this.currentUserId(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> findAllTemplates(int page, int size, EnumProviderContractSortField orderBy, EnumSortingOrder order) {
        final ProviderTemplateContractQuery query = ProviderTemplateContractQuery.builder()
            .page(page)
            .size(size)
            .order(order)
            .orderBy(orderBy)
            .providerKey(this.currentUserKey())
            .build();

        final PageResultDto<ProviderTemplateContractDto> result = templateContractService.findAll(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findOneTemplate(UUID key) {
        final ProviderTemplateContractDto result = this.templateContractService.findOneByKey(
            this.currentUserId(), key
        ).orElse(null);

        if (result == null) {
            return RestResponse.notFound();
        }

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> deactivate(UUID key) {
        try {
            final ProviderTemplateContractDto result = this.templateContractService.deactivate(this.currentUserId(), key);

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> createDraftForTemplate(UUID key) {
        try {
            final ProviderTemplateContractDto result = this.templateContractService.createForTemplate(
                this.currentUserId(), this.currentUserKey(), key
            );

            return RestResponse.result(result);
        } catch (final ApplicationException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }


}
