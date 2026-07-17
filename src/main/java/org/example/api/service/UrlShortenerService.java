package org.example.api.service;

import org.example.api.repository.UrlRepository;
import org.example.api.exceptions.AliasAlreadyExistsException;
import org.example.api.exceptions.UrlNotFoundException;
import org.example.api.model.ShortUrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class UrlShortenerService {

    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final int RANDOM_ALIAS_LENGTH = 7;

    private static final Pattern ALIAS_PATTERN = Pattern.compile("[A-Za-z0-9_-]{3,32}");

    private static final Set<String> RESERVED_ALIASES = new HashSet<String>(Arrays.asList("urls"));

    private final UrlRepository repository;
    private final SecureRandom random;
    private final long expirationMinutes;


    public UrlShortenerService(
            UrlRepository repository,
            long expirationMinutes
    ) {
        this.repository = repository;
        this.expirationMinutes = expirationMinutes;
        this.random = new SecureRandom();
    }

    public ShortUrl shorten(
            String targetUrl,
            String requestedAlias
    ) throws SQLException {

        String validatedUrl = validateUrl(targetUrl);
        Instant createdAt = Instant.now();

        if (requestedAlias != null && !requestedAlias.trim().isEmpty()) {

            String alias = requestedAlias.trim();
            validateAlias(alias);

            if (!repository.insert(alias, validatedUrl, createdAt)) {
                throw new AliasAlreadyExistsException(alias);
            }

            return new ShortUrl(alias, validatedUrl, createdAt);
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            String alias = generateAlias();

            if (repository.insert(alias, validatedUrl, createdAt)) {
                return new ShortUrl(alias, validatedUrl, createdAt);
            }
        }

        throw new IllegalStateException(
                "Não foi possível gerar um alias disponível"
        );
    }

    public Optional<String> findTargetUrl(String alias)
            throws SQLException {

        return repository.findTargetUrl(alias);
    }

    private String validateUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo url é obrigatório");
        }

        String url = value.trim();

        if (url.length() > 2048) {
            throw new IllegalArgumentException("A URL deve possuir no máximo 2048 caracteres");
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();

            if (scheme == null ||
                    uri.getHost() == null ||
                    (!"http".equalsIgnoreCase(scheme) &&
                            !"https".equalsIgnoreCase(scheme))) {

                throw new IllegalArgumentException("A URL deve ser absoluta e utilizar HTTP ou HTTPS");
            }

            if (uri.getUserInfo() != null) {
                throw new IllegalArgumentException("URLs contendo usuário ou senha não são permitidas");
            }

            return url;

        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("A URL informada é inválida");
        }
    }

    private void validateAlias(String alias) {
        if (!ALIAS_PATTERN.matcher(alias).matches()) {
            throw new IllegalArgumentException(
                    "O alias deve possuir entre 3 e 32 caracteres " +
                            "e utilizar somente letras, números, _ ou -"
            );
        }

        if (RESERVED_ALIASES.contains(
                alias.toLowerCase(Locale.ROOT))) {

            throw new IllegalArgumentException("O alias informado é reservado");
        }
    }

    private String generateAlias() {
        StringBuilder builder =
                new StringBuilder(RANDOM_ALIAS_LENGTH);

        for (int index = 0;
             index < RANDOM_ALIAS_LENGTH;
             index++) {

            builder.append(
                    ALPHABET[random.nextInt(ALPHABET.length)]
            );
        }

        return builder.toString();
    }

    public List<ShortUrl> findAll() throws SQLException {
        return repository.findAll();
    }

    public ShortUrl update(
            String alias,
            String targetUrl
    ) throws SQLException {

        String validatedUrl = validateUrl(targetUrl);

        boolean updated = repository.updateTargetUrl(alias, validatedUrl);

        if (!updated) {
            throw new UrlNotFoundException(alias);
        }

        return repository
                .findByAlias(alias)
                .orElseThrow(() -> new UrlNotFoundException(alias));
    }

    public boolean delete(String alias) throws SQLException {
        return repository.deleteByAlias(alias);
    }

    public int deleteExpired() throws SQLException {
        Instant expirationThreshold = Instant
                .now()
                .minus(expirationMinutes, ChronoUnit.MINUTES);

        return repository.deleteCreatedBefore(expirationThreshold);
    }


    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
