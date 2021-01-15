package eu.opertusmundi.web.controller.action;

import java.util.Optional;

import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.web.model.recommender.client.RecommenderClientResponse;

@RestController
public class RecommenderControllerImpl extends BaseController implements RecommenderController {

    @Override
    public BaseResponse getRecommendedAssets(Optional<Integer> limit) {
        return new RecommenderClientResponse();
    }

}
