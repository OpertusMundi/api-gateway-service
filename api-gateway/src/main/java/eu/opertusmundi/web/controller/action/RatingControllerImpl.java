package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.RatingServiceFeignClient;
import eu.opertusmundi.web.model.rating.client.ClientRatingCommandDto;
import eu.opertusmundi.web.model.rating.client.ClientRatingDto;
import eu.opertusmundi.web.model.rating.server.ServerAssetRatingCommandDto;
import eu.opertusmundi.web.model.rating.server.ServerProviderRatingCommandDto;
import eu.opertusmundi.web.model.rating.server.ServerRatingDto;
import eu.opertusmundi.web.validation.AssetRatingValidator;
import eu.opertusmundi.web.validation.ProviderRatingValidator;
import feign.FeignException;

@RestController
public class RatingControllerImpl extends BaseController implements RatingController {

    @Autowired
    private ObjectProvider<RatingServiceFeignClient> ratingClient;

    @Autowired
    private AssetRatingValidator assetRatingValidator;

    @Autowired
    private ProviderRatingValidator providerRatingValidator;

    @Override
    public RestResponse<List<ClientRatingDto>> getAssetRatings(UUID id) {
        ResponseEntity<RestResponse<List<ServerRatingDto>>> e;

        try {
            e = this.ratingClient.getObject().getAssetRatings(id);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final RestResponse<List<ServerRatingDto>> serviceResponse = e.getBody();

        if(!serviceResponse.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        final List<ClientRatingDto> result = serviceResponse.getResult().stream()
            .map(r -> {
                final ClientRatingDto dto = new ClientRatingDto(r);

                return dto;
            })
            .collect(Collectors.toList());

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<List<ClientRatingDto>> getProviderRatings(UUID id) {
        ResponseEntity<RestResponse<List<ServerRatingDto>>> e;

        try {
            e = this.ratingClient.getObject().getProviderRatings(id);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final RestResponse<List<ServerRatingDto>> serviceResponse = e.getBody();

        if(!serviceResponse.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        final List<ClientRatingDto> result = serviceResponse.getResult().stream()
            .map(r -> {
                final ClientRatingDto dto = new ClientRatingDto(r);

                return dto;
            })
            .collect(Collectors.toList());

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse addAssetRating(UUID id, ClientRatingCommandDto command, BindingResult validationResult) {
        final ServerAssetRatingCommandDto c = new ServerAssetRatingCommandDto(command);
        c.setAccount(this.currentUserKey());

        // TODO: Check if consumer owns the data asset
        this.assetRatingValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }


        try {
            final ResponseEntity<BaseResponse> response = this.ratingClient.getObject().addAssetRating(id, c);

            if (response.getBody().getSuccess()) {
                return RestResponse.success();
            }

            // TODO: Map service errors to API gateway error codes ...

            return RestResponse.error(BasicMessageCode.InternalServerError, "Record creation failed");
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public BaseResponse addProviderRating(UUID id, ClientRatingCommandDto command, BindingResult validationResult) {
        final ServerProviderRatingCommandDto c = new ServerProviderRatingCommandDto(command);
        c.setAccount(this.currentUserKey());

        // TODO: Check if consumer has purchased a data asset/service from the provider
        this.providerRatingValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final ResponseEntity<BaseResponse> response = this.ratingClient.getObject().addProviderRating(id, c);

            if (response.getBody().getSuccess()) {
                return RestResponse.success();
            }

            // TODO: Map service errors to API gateway error codes ...

            return RestResponse.error(BasicMessageCode.InternalServerError, "Record creation failed");
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

}