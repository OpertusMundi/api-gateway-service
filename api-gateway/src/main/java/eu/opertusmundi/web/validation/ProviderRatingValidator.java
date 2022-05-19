package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.rating.ProviderRatingCommandDto;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;

@Component
public class ProviderRatingValidator implements Validator {

    private final AccountAssetRepository assetRepository;

    private final AccountSubscriptionRepository subscriptionRepository;

    @Autowired
    public ProviderRatingValidator(AccountAssetRepository assetRepository, AccountSubscriptionRepository subscriptionRepository) {
        this.assetRepository        = assetRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ProviderRatingCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final ProviderRatingCommandDto c = (ProviderRatingCommandDto) o;

        final boolean ownedAsset        = this.assetRepository.checkPurchaseByProvider(c.getAccount(), c.getProvider());
        final boolean ownedSubscription = this.subscriptionRepository.providerSubscriptionExists(c.getAccount(), c.getProvider(), false);

        if (!ownedAsset && !ownedSubscription) {
            e.rejectValue("asset", EnumValidatorError.ReferenceNotFound.name());
        }
    }

}
