package com.commerce.customer.core.domain.service.jwt;

import com.commerce.customer.core.domain.exception.ExpiredJwtTokenException;
import com.commerce.customer.core.domain.exception.InvalidJwtTokenException;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.jwt.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final String ISSUER = "customer-service";
    private static final String AUDIENCE = "customer-app";
    private static final String ACCOUNT_ID_CLAIM = "accountId";
    private static final String EMAIL_CLAIM = "email";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    
    // 블랙리스트 (실제 운영환경에서는 Redis 등을 사용)
    private final Map<String, LocalDateTime> tokenBlacklist = new ConcurrentHashMap<>();

    public JwtTokenServiceImpl() {
        // 실제 운영환경에서는 외부 설정에서 키를 로드해야 함
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build();
    }

    @Override
    public TokenPair generateTokenPair(CustomerId customerId, AccountId accountId, Email email) {
        JwtToken accessToken = generateToken(customerId, accountId, email, JwtTokenType.ACCESS);
        JwtToken refreshToken = generateToken(customerId, accountId, email, JwtTokenType.REFRESH);
        
        return TokenPair.of(accessToken, refreshToken);
    }

    @Override
    public JwtToken refreshAccessToken(JwtToken refreshToken) {
        Optional<JwtClaims> claimsOpt = validateToken(refreshToken);
        if (claimsOpt.isEmpty()) {
            throw new InvalidJwtTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        JwtClaims claims = claimsOpt.get();
        if (claims.getTokenType() != JwtTokenType.REFRESH) {
            throw new InvalidJwtTokenException("리프레시 토큰이 아닙니다.");
        }

        return generateToken(
            claims.getCustomerId(),
            claims.getAccountIdObject(),
            claims.getEmailObject(),
            JwtTokenType.ACCESS
        );
    }

    @Override
    public Optional<JwtClaims> validateToken(JwtToken token) {
        try {
            if (isTokenBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다: {}", token.getValue().substring(0, 10));
                return Optional.empty();
            }

            Claims claims = jwtParser.parseSignedClaims(token.getValue()).getPayload();
            
            JwtClaims jwtClaims = JwtClaims.of(
                claims.getSubject(),
                claims.get(ACCOUNT_ID_CLAIM, String.class),
                claims.get(EMAIL_CLAIM, String.class),
                claims.getIssuer(),
                claims.getAudience().iterator().next(), // Set에서 첫 번째 audience 가져오기
                convertToLocalDateTime(claims.getIssuedAt()),
                convertToLocalDateTime(claims.getExpiration()),
                JwtTokenType.valueOf(claims.get(TOKEN_TYPE_CLAIM, String.class))
            );

            return Optional.of(jwtClaims);
            
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
            throw new ExpiredJwtTokenException("만료된 토큰입니다.", e);
        } catch (JwtException e) {
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw new InvalidJwtTokenException("유효하지 않은 토큰입니다.", e);
        }
    }

    @Override
    public Optional<JwtToken> parseToken(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            // Bearer 접두사 제거
            String token = tokenString.startsWith("Bearer ") ? 
                tokenString.substring(7) : tokenString;

            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            
            JwtTokenType tokenType = JwtTokenType.valueOf(claims.get(TOKEN_TYPE_CLAIM, String.class));
            LocalDateTime issuedAt = convertToLocalDateTime(claims.getIssuedAt());
            LocalDateTime expiresAt = convertToLocalDateTime(claims.getExpiration());

            return Optional.of(JwtToken.of(token, tokenType, issuedAt, expiresAt));
            
        } catch (JwtException e) {
            log.warn("토큰 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void invalidateToken(JwtToken token) {
        tokenBlacklist.put(token.getValue(), token.getExpiresAt());
        log.info("토큰을 블랙리스트에 추가했습니다.");
    }

    @Override
    public boolean isTokenBlacklisted(JwtToken token) {
        LocalDateTime expiredAt = tokenBlacklist.get(token.getValue());
        if (expiredAt == null) {
            return false;
        }
        
        // 만료된 블랙리스트 항목 정리
        if (LocalDateTime.now().isAfter(expiredAt)) {
            tokenBlacklist.remove(token.getValue());
            return false;
        }
        
        return true;
    }

    @Override
    public void invalidateAllUserTokens(CustomerId customerId) {
        // 실제 구현에서는 데이터베이스나 Redis에서 해당 사용자의 모든 토큰을 조회하여 무효화
        log.info("사용자 {}의 모든 토큰을 무효화합니다.", customerId.getValue());
        // 현재는 로그만 남김 (향후 구현 예정)
    }

    private JwtToken generateToken(CustomerId customerId, AccountId accountId, Email email, JwtTokenType tokenType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(tokenType.getExpirationMinutes());

        Map<String, Object> claims = new HashMap<>();
        claims.put(ACCOUNT_ID_CLAIM, accountId.getValue().toString());
        claims.put(EMAIL_CLAIM, email.getValue());
        claims.put(TOKEN_TYPE_CLAIM, tokenType.name());

        String tokenValue = Jwts.builder()
                .claims(claims)
                .subject(customerId.getValue().toString())
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .issuedAt(convertToDate(now))
                .expiration(convertToDate(expiresAt))
                .signWith(secretKey)
                .compact();

        return JwtToken.of(tokenValue, tokenType, now, expiresAt);
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}