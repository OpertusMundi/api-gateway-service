package eu.opertusmundi.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.iban4j.BicFormatException;
import org.iban4j.BicUtil;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.account.BankAccountCommandDto;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.util.TextUtils;

@Component
public class ProviderValidator implements Validator {

    private final AccountRepository accountRepository;

    private final CustomerRepository customerRepository;

    public ProviderValidator(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository  = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ProviderProfessionalCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final ProviderProfessionalCommandDto c           = (ProviderProfessionalCommandDto) o;
        final BankAccountCommandDto          bankAccount = c.getBankAccount();

        // Email must be unique for all providers
        final CustomerEntity customerWithSameMail = this.customerRepository
            .findProviderByEmailAndAccountIdNot(c.getEmail(), c.getUserId())
            .orElse(null);

        if (customerWithSameMail != null) {
            e.rejectValue("email", EnumValidatorError.NotUnique.name());
        }

        // Name cannot be updated
        final AccountEntity              account  = accountRepository.findById(c.getUserId()).get();
        final CustomerProfessionalEntity provider = account.getProvider();
        if (provider != null && !provider.getName().equalsIgnoreCase(c.getName())) {
            e.rejectValue("name", EnumValidatorError.NotUpdatable.name());
        }

        // Name must be unique
        final CustomerEntity customerWithSameNamespace = this.customerRepository
            .findProviderByNamespaceAndAccountIdNot(TextUtils.slugify(c.getName()), c.getUserId())
            .orElse(null);

        if (customerWithSameNamespace != null) {
            e.rejectValue("name", EnumValidatorError.NotUnique.name());
        }

        // IBAN validation
        if (bankAccount != null && !StringUtils.isBlank(bankAccount.getIban())) {
            try {
                IbanUtil.validate(bankAccount.getIban());
            } catch (final IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException ex) {
                e.rejectValue("bankAccount.iban", EnumValidatorError.NotValid.name());
            }
        }
        // BIC validation
        if (bankAccount != null && !StringUtils.isBlank(bankAccount.getBic())) {
            try {
                BicUtil.validate(bankAccount.getBic());
            } catch (final BicFormatException | UnsupportedCountryException ex) {
                e.rejectValue("bankAccount.bic", EnumValidatorError.NotValid.name());
            }
        }
    }

}
