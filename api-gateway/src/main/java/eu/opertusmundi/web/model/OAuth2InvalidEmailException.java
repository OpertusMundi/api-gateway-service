package eu.opertusmundi.web.model;

public class OAuth2InvalidEmailException extends OAuth2AccountCreationException {

    private static final long serialVersionUID = 1L;

    public OAuth2InvalidEmailException(String msg) {
        super(msg);
    }

}
