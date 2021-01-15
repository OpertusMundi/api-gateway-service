package eu.opertusmundi.web.model.security;

import org.springframework.security.web.csrf.CsrfToken;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

    private final CsrfToken token;

    public Token(CsrfToken token) {
        this.token = token;
    }

    @JsonProperty("csrfHeader")
    public String getHeader() {
        return this.token.getHeaderName();
    }

    @JsonProperty("csrfToken")
    public String getToken() {
        return this.token.getToken();
    }

}
