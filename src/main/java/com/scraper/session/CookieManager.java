package com.scraper.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CookieManager {
    private static final Logger logger = LoggerFactory.getLogger(CookieManager.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectNode cookieStorage = mapper.createObjectNode();

    public CookieManager() {
        cookieStorage.putArray("cookies");
        cookieStorage.put("jwtToken", "");
    }

    public void detectAndStoreCookies(BasicCookieStore cookieStore) {
        try {
            ArrayNode cookiesArray = (ArrayNode) cookieStorage.get("cookies");
            for (Cookie cookie : cookieStore.getCookies()) {
                ObjectNode cookieNode = mapper.createObjectNode();
                cookieNode.put("name", cookie.getName());
                cookieNode.put("value", cookie.getValue());
                cookieNode.put("domain", cookie.getDomain());
                cookieNode.put("path", cookie.getPath() != null ? cookie.getPath() : "/");
                cookieNode.put("secure", cookie.isSecure());
                cookieNode.put("httpOnly", cookie.isHttpOnly());
                cookiesArray.add(cookieNode);
            }
            logger.info("🍪 Detected and stored {} cookies", cookieStore.getCookies().size());
        } catch (Exception e) {
            logger.warn("Error detecting cookies", e);
        }
    }

    public void storeJWTToken(String token) {
        cookieStorage.put("jwtToken", token);
        logger.info("🔐 JWT token stored");
    }

    public void storeCookies(List<Cookie> cookies) {
        try {
            ArrayNode cookiesArray = (ArrayNode) cookieStorage.get("cookies");
            for (Cookie cookie : cookies) {
                ObjectNode cookieNode = mapper.createObjectNode();
                cookieNode.put("name", cookie.getName());
                cookieNode.put("value", cookie.getValue());
                cookieNode.put("domain", cookie.getDomain());
                cookieNode.put("path", cookie.getPath() != null ? cookie.getPath() : "/");
                cookieNode.put("secure", cookie.isSecure());
                cookieNode.put("httpOnly", cookie.isHttpOnly());
                cookiesArray.add(cookieNode);
            }
            logger.info("🍪 Stored {} cookies", cookies.size());
        } catch (Exception e) {
            logger.warn("Error storing cookies", e);
        }
    }

    public void saveCookies(String filename) throws Exception {
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cookieStorage);
        Files.writeString(Paths.get(filename), json);
        logger.info("💾 Cookies saved to {}", filename);
    }

    public void loadCookies(String filename) throws Exception {
        String json = Files.readString(Paths.get(filename));
        ObjectNode loaded = mapper.readValue(json, ObjectNode.class);
        cookieStorage.setAll(loaded);
        logger.info("📂 Cookies loaded from {}", filename);
    }

    public String exportCookies() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cookieStorage);
        } catch (Exception e) {
            logger.error("Error exporting cookies", e);
            return "{}";
        }
    }

    public ObjectNode getCookieStorage() {
        return cookieStorage;
    }
}
