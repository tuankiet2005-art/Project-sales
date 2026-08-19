package com.vehisales.platform.service;

import com.vehisales.platform.config.AdminAuthProperties;
import com.vehisales.platform.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String HMAC = "HmacSHA256";

    private final AdminAuthProperties properties;

    public String login(String username, String password) {
        if (!same(username, properties.getUsername()) || !same(password, properties.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        return issueToken();
    }

    public boolean isAuthorized(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        return same(authorizationHeader.substring("Bearer ".length()).trim(), issueToken());
    }

    String issueToken() {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(properties.getTokenSecret().getBytes(StandardCharsets.UTF_8), HMAC));
            byte[] digest = mac.doFinal(
                    (properties.getUsername() + ":" + properties.getPassword()).getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot issue admin token", ex);
        }
    }

    private static boolean same(String left, String right) {
        byte[] a = (left == null ? "" : left).getBytes(StandardCharsets.UTF_8);
        byte[] b = (right == null ? "" : right).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
