package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.feign.client.RatingServiceFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.rating.client.ClientAssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.client.ClientProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.client.ClientRatingDto;
import eu.opertusmundi.common.model.rating.server.ServerAssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.server.ServerProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.server.ServerRatingDto;
import eu.opertusmundi.web.validation.AssetRatingValidator;
import eu.opertusmundi.web.validation.ProviderRatingValidator;

@RestController
public class RatingControllerImpl extends BaseController implements RatingController {

    @Autowired
    private ObjectProvider<RatingServiceFeignClient> ratingClient;

    @Autowired
    private AssetRatingValidator assetRatingValidator;

    @Autowired
    private ProviderRatingValidator providerRatingValidator;

    @Override
    public RestResponse<List<ClientRatingDto>> getAssetRatings(String id) {
        final RestResponse<List<ServerRatingDto>> serviceResponse = this.ratingClient.getObject().getAssetRatings(id).getBody();

        if (!serviceResponse.getSuccess()) {
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
        final RestResponse<List<ServerRatingDto>> serviceResponse = this.ratingClient.getObject().getProviderRatings(id).getBody();

        if (!serviceResponse.getSuccess()) {
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
    public BaseResponse addAssetRating(String id, ClientAssetRatingCommandDto command, BindingResult validationResult) {
        command.setAccount(this.currentUserKey());
        command.setAsset(id);

        final ServerAssetRatingCommandDto c = new ServerAssetRatingCommandDto(command);
        c.setAccount(this.currentUserKey());

        this.assetRatingValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final ResponseEntity<BaseResponse> response = this.ratingClient.getObject().addAssetRating(id, c);

        if (response.getBody().getSuccess()) {
            return RestResponse.success();
        }

        return RestResponse.error(BasicMessageCode.InternalServerError, "Record creation failed");
    }

    @Override
    public BaseResponse addProviderRating(UUID id, ClientProviderRatingCommandDto command, BindingResult validationResult) {
        command.setAccount(this.currentUserKey());
        command.setProvider(id);

        final ServerProviderRatingCommandDto c = new ServerProviderRatingCommandDto(command);
        c.setAccount(this.currentUserKey());

        this.providerRatingValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final ResponseEntity<BaseResponse> response = this.ratingClient.getObject().addProviderRating(id, c);

        if (response.getBody().getSuccess()) {
            return RestResponse.success();
        }

        return RestResponse.error(BasicMessageCode.InternalServerError, "Record creation failed");
    }

}