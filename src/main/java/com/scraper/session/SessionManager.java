package com.scraper.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private final HttpClient httpClient;
    private final BasicCookieStore cookieStore;
    private final CookieManager cookieManager;
    private final ObjectMapper mapper;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String jwtToken;

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
    };

    public SessionManager() {
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();
        this.cookieManager = new CookieManager();
        this.mapper = new ObjectMapper();
    }

    public String getPageContent(String url, String proxy) throws Exception {
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", getRandomUserAgent());

        return httpClient.execute(request, response -> {
            if (response.getCode() == 200) {
                HttpEntity entity = response.getEntity();
                return entity != null ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8) : "";
            }
            logger.warn("HTTP {} from {}", response.getCode(), url);
            return "";
        });
    }

    public boolean authenticateWithForm(String loginUrl, String username, String password, Map<String, String> formData) throws Exception {
        logger.info("🔐 Form auth to: {}", loginUrl);
        HttpPost post = new HttpPost(loginUrl);
        post.setHeader("User-Agent", getRandomUserAgent());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        if (formData != null) {
            formData.forEach((k, v) -> params.add(new BasicNameValuePair(k, v)));
        }

        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        return httpClient.execute(post, response -> {
            boolean success = response.getCode() >= 200 && response.getCode() < 300;
            logger.info("Form auth response: HTTP {} {}", response.getCode(), success ? "✓" : "✗");
            if (success) {
                cookieManager.detectAndStoreCookies(cookieStore);
            }
            return success;
        });
    }

    public String authenticateWithJWT(String loginUrl, String username, String password) throws Exception {
        logger.info("🔐 JWT auth to: {}", loginUrl);
        HttpPost post = new HttpPost(loginUrl);
        post.setHeader("User-Agent", getRandomUserAgent());
        post.setHeader("Content-Type", "application/json");

        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        post.setEntity(new StringEntity(body));

        return httpClient.execute(post, response -> {
            try {
                if (response.getCode() == 200) {
                    String content = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    JsonNode jsonNode = mapper.readTree(content);
                    jwtToken = jsonNode.get("token").asText();
                    logger.info("✓ JWT token obtained");
                    cookieManager.storeJWTToken(jwtToken);
                    return jwtToken;
                }
                logger.warn("JWT auth failed: HTTP {}", response.getCode());
                return null;
            } catch (Exception e) {
                logger.error("Error parsing JWT response", e);
                return null;
            }
        });
    }

    public boolean authenticateWithBrowser(String loginUrl, String username, String password,
                                          String usernameSelector, String passwordSelector,
                                          String submitSelector) throws Exception {
        logger.info("🌐 Browser auth to: {}", loginUrl);
        try {
            if (browser == null) {
                browser = Playwright.create().chromium().launch();
                context = browser.newContext();
                page = context.newPage();
            }

            page.navigate(loginUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.fill(usernameSelector, username);
            page.fill(passwordSelector, password);
            page.click(submitSelector);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            List<Cookie> cookies = context.cookies();
            cookieManager.storeBrowserCookies(cookies);
            logger.info("✓ Browser auth successful, captured {} cookies", cookies.size());
            return true;
        } catch (Exception e) {
            logger.error("Browser auth failed", e);
            return false;
        }
    }

    public String getContentWithJWT(String url) throws Exception {
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new RuntimeException("JWT token not available. Authenticate first.");
        }

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", getRandomUserAgent());
        request.setHeader("Authorization", "Bearer " + jwtToken);

        return httpClient.execute(request, response -> {
            if (response.getCode() == 200) {
                HttpEntity entity = response.getEntity();
                return entity != null ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8) : "";
            }
            return "";
        });
    }

    private String getRandomUserAgent() {
        return USER_AGENTS[new Random().nextInt(USER_AGENTS.length)];
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void close() {
        try {
            if (page != null) page.close();
            if (context != null) context.close();
            if (browser != null) browser.close();
            logger.info("Browser resources closed");
        } catch (Exception e) {
            logger.warn("Error closing browser", e);
        }
    }
}
