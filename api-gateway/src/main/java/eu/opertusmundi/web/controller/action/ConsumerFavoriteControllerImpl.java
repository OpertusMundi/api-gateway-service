package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.favorite.EnumFavoriteSortField;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import eu.opertusmundi.common.service.FavoriteService;

@RestController
public class ConsumerFavoriteControllerImpl extends BaseController implements ConsumerFavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Override
    public RestResponse<?> findAll(
        EnumFavoriteType type, int pageIndex, int pageSize, EnumFavoriteSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final PageResultDto<FavoriteDto> result = this.favoriteService.findAll(
                this.currentUserId(), type, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> addFavorite(@Valid FavoriteCommandDto command, BindingResult validationResult) {
        command.setUserId(this.currentUserId());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
        }

        final FavoriteDto result = this.favoriteService.addFavorite(command);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse removeFavorite(UUID key) {
        this.favoriteService.removeFavorite(this.currentUserId(), key);

        return RestResponse.success();
    }

}
