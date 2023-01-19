package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.AccountMapDto;
import eu.opertusmundi.web.model.EnumAccountMapSortField;
import eu.opertusmundi.web.repository.AccountMapRepository;

@RestController
public class TopioMapsControllerImpl extends BaseController implements TopioMapsController {

    private final AccountMapRepository accountMapRepository;

    @Autowired
    public TopioMapsControllerImpl(AccountMapRepository accountMapRepository) {
        this.accountMapRepository = accountMapRepository;
    }

    @Override
    public BaseResponse findAll(int pageIndex, int pageSize, EnumAccountMapSortField orderBy, EnumSortingOrder order) {
        final Direction           direction   = Direction.DESC;
        final PageRequest         pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));
        final Page<AccountMapDto> page        = this.accountMapRepository
            .findAll(this.currentUserKey(), pageRequest)
            .map(e -> e.toDto(false));

        final var result = PageResultDto.of(pageIndex, pageSize, page.getContent(), page.getTotalElements());

        return RestResponse.result(result);
    }

}
