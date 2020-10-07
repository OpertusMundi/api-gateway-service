package eu.opertusmundi.web.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.web.model.security.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtils {

    private static final String TOKEN_TYPE          = "JWT";
    private static final String TOKEN_ISSUER        = "opertus-mundi";
    private static final String TOKEN_AUDIENCE      = "api-gateway";

    @Value("${opertusmundi.feign.key-store.path}")
    private String keyStorePath;

    @Value("${opertusmundi.feign.key-store.password}")
    private String keyStorePassword;

    @Value("${opertusmundi.feign.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private ResourceLoader resourceLoader;

    public String createToken(String subject) throws Exception {
        return this.createToken(null, subject);
    }

    public String createToken(String alias, String subject) throws Exception {
        final List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

        return this.createToken(alias, subject, null, null, authorities);
    }

    public String createToken(String subject, Integer userId, UUID userKey) throws Exception {
        return this.createToken(null, subject, userId, userKey);
    }

    public String createToken(String alias, String subject, Integer userId, UUID userKey) throws Exception {
        final List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

        return this.createToken(alias, subject, userId, userKey, authorities);
    }

    public String createToken(User user) throws Exception {
        return this.createToken(null, user);
    }

    public String createToken(String alias, User user) throws Exception {
        final AccountDto account = user.getAccount();

        final List<GrantedAuthority> authorities = account.getRoles().stream()
            .map(r -> new SimpleGrantedAuthority(r.toString()))
            .collect(Collectors.toList());

        return this.createToken(alias, account.getEmail(), account.getId(), account.getKey(), authorities);
    }

    public String createToken(
        String alias, String subject, Integer userId, UUID userKey, List<GrantedAuthority> authorities
    ) throws Exception {

        final List<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        Key key = null;

        if (StringUtils.isBlank(alias)) {
            // If no alias is specified use the global secret
            final byte[] signingKey = this.jwtSecret.getBytes(StandardCharsets.UTF_8);

            key = Keys.hmacShaKeyFor(signingKey);
        } else {
            // Get the private key for the selected alias
            key = this.getPrivateKey(alias);
        }

        // TODO: Do not expire the token (it is created only once)
        final Date expireOn = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L);

        // TODO: Set default values for standard claim names

        final String token = Jwts.builder()
            .signWith(key)
            .setHeaderParam("typ", TOKEN_TYPE)
            .setIssuer(TOKEN_ISSUER)
            .setAudience(TOKEN_AUDIENCE)
            .setSubject(subject)
            .setExpiration(expireOn)
            .setIssuedAt(new Date())
            .setId(UUID.randomUUID().toString())
            .claim("roles", roles)
            .claim("uid", userId)
            .claim("ukey", userKey)
            .compact();

        return token;
    }

    /**
     * Get private key from the configured key store
     *
     * @param alias Key alias
     *
     * @return The {@link PrivateKey} for the specified alias
     *
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnrecoverableKeyException
     */
    private PrivateKey getPrivateKey(String alias) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
        final Resource resource = this.resourceLoader.getResource(this.keyStorePath);
        final char[]   password = this.keyStorePassword.toCharArray();

        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(resource.getFile()), password);

        final Key emailKey = keyStore.getKey(alias, password);

        return (PrivateKey) emailKey;
    }

}
