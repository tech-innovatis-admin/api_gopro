package br.com.gopro.api.config;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.auth.jwt.secret}")
    private String jwtSecret;

    @Value("${app.auth.jwt.expiration-seconds:43200}")
    private long jwtExpirationSeconds;

    @Value("${app.auth.jwt.issuer:api-da-gopro}")
    private String jwtIssuer;

    @PostConstruct
    void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("app.auth.jwt.secret nao pode ser vazio");
        }
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.auth.jwt.secret deve ter pelo menos 32 bytes");
        }
    }

    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtExpirationSeconds);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtIssuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .signWith(signingKey())
                .compact();
    }

    public AuthenticatedUserPrincipal parseAccessToken(String token) {
        Claims claims = parseClaims(token);

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Token sem subject");
        }

        Object emailClaim = claims.get("email");
        Object roleClaim = claims.get("role");
        if (!(emailClaim instanceof String email) || !(roleClaim instanceof String roleText)) {
            throw new IllegalArgumentException("Claims obrigatorias ausentes no token");
        }

        UserRoleEnum role = UserRoleEnum.valueOf(roleText);
        return new AuthenticatedUserPrincipal(Long.parseLong(subject), email, role);
    }

    public Instant extractIssuedAt(String token) {
        Date issuedAt = parseClaims(token).getIssuedAt();
        if (issuedAt == null) {
            throw new IllegalArgumentException("Token sem issuedAt");
        }
        return issuedAt.toInstant();
    }

    public long getJwtExpirationSeconds() {
        return jwtExpirationSeconds;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
