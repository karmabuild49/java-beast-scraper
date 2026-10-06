package com.scraper.auth;

import com.scraper.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class AuthenticationManager {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    public boolean authenticate(String authType, LoginCredentials credentials, SessionManager sessionManager) {
        switch (authType.toLowerCase(Locale.ROOT)) {
            case "form" -> {
                try {
                    return sessionManager.authenticateWithForm(
                            credentials.getLoginUrl(),
                            credentials.getUsername(),
                            credentials.getPassword(),
                            credentials.getFormData()
                    );
                } catch (Exception e) {
                    logger.error("Form auth failed", e);
                    return false;
                }
            }
            case "jwt" -> {
                try {
                    String token = sessionManager.authenticateWithJWT(
                            credentials.getLoginUrl(),
                            credentials.getUsername(),
                            credentials.getPassword()
                    );
                    return token != null && !token.isBlank();
                } catch (Exception e) {
                    logger.error("JWT auth failed", e);
                    return false;
                }
            }
            case "browser" -> {
                try {
                    return sessionManager.authenticateWithBrowser(
                            credentials.getLoginUrl(),
                            credentials.getUsername(),
                            credentials.getPassword(),
                            credentials.getUsernameSelector(),
                            credentials.getPasswordSelector(),
                            credentials.getSubmitSelector()
                    );
                } catch (Exception e) {
                    logger.error("Browser auth failed", e);
                    return false;
                }
            }
            default -> {
                logger.error("Unknown auth type: {}", authType);
                return false;
            }
        }
    }
}