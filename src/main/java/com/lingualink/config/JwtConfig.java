package com.lingualink.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecretKey secretKey() {
        // Use the secret directly as bytes - do not double-encode
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad or repeat the secret to meet minimum length requirement (256 bits for HS256)
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            for (int i = keyBytes.length; i < 32; i++) {
                paddedKey[i] = keyBytes[i % keyBytes.length];
            }
            return new SecretKeySpec(paddedKey, "HmacSHA256");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        JWK jwk = new OctetSequenceKey.Builder(secretKey.getEncoded()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Custom converter to extract authorities from the "authorities" claim (array format)
        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter = jwt -> {
            Object authoritiesClaim = jwt.getClaim("authorities");
            if (authoritiesClaim == null) {
                return List.of();
            }
            
            // Handle both array and single string formats
            if (authoritiesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) authoritiesClaim;
                return authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else if (authoritiesClaim instanceof String) {
                // Handle space-separated string format
                String authoritiesStr = (String) authoritiesClaim;
                return List.of(new SimpleGrantedAuthority(authoritiesStr));
            }
            
            return List.of();
        };
        
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        // Set the principal claim name to "sub" (subject) which is standard JWT
        authenticationConverter.setPrincipalClaimName("sub");
        
        return authenticationConverter;
    }
}

