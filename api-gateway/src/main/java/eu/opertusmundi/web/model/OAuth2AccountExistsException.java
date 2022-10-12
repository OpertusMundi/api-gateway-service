package eu.opertusmundi.web.model;

public class OAuth2AccountExistsException extends OAuth2AccountCreationException {

    private static final long serialVersionUID = 1L;

    public OAuth2AccountExistsException(String msg) {
        super(msg);
    }

}
