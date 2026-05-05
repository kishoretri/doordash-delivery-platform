package com.delivery.user.service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import com.delivery.user.domain.User;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())           // user's email
            .issuedAt(new Date())          // current time → new Date()
            .expiration(new Date(System.currentTimeMillis()+ jwtExpiration))        // new Date(System.currentTimeMillis() + jwtExpiration)
            .signWith(getSigningKey())   // we will create this helper method next
                .compact();           // builds the final token string
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token) // parse the token
                .getPayload()         // get the payload
                .getSubject();
    }

    public boolean isTokenValid(String token, User user) {
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(user.getEmail()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }



}
