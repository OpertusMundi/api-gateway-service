package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.rating.AssetRatingCommandDto;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;

@Component
public class AssetRatingValidator implements Validator {

    private final AccountAssetRepository assetRepository;

    private final AccountSubscriptionRepository subscriptionRepository;

    @Autowired
    public AssetRatingValidator(AccountAssetRepository assetRepository, AccountSubscriptionRepository subscriptionRepository) {
        this.assetRepository        = assetRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AssetRatingCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final AssetRatingCommandDto c = (AssetRatingCommandDto) o;

        final boolean ownedAsset        = this.assetRepository.checkOwnershipByAsset(c.getAccount(), c.getAsset());
        final boolean ownedSubscription = this.subscriptionRepository.subscriptionExists(c.getAccount(), c.getAsset(), false);

        if (!ownedAsset && !ownedSubscription) {
            e.rejectValue("asset", EnumValidatorError.ReferenceNotFound.name());
        }
    }

}
