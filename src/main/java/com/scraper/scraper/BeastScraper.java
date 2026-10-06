package com.scraper.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraper.auth.AuthenticationManager;
import com.scraper.auth.LoginCredentials;
import com.scraper.config.ScraperConfig;
import com.scraper.parser.DataParser;
import com.scraper.session.SessionManager;
import com.scraper.util.ProxyRotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BeastScraper {
    private static final Logger logger = LoggerFactory.getLogger(BeastScraper.class);

    private final AuthenticationManager authManager;
    private final SessionManager sessionManager;
    private final DataParser parser;
    private final ProxyRotator proxyRotator;
    private final ExecutorService executor;
    private final ObjectMapper mapper;

    public BeastScraper() {
        this.authManager = new AuthenticationManager();
        this.sessionManager = new SessionManager();
        this.parser = new DataParser();
        this.proxyRotator = new ProxyRotator();
        this.executor = Executors.newFixedThreadPool(5);
        this.mapper = new ObjectMapper();
    }

    public boolean authenticate(String authType, LoginCredentials credentials) {
        try {
            logger.info("🔐 Authenticating using {}", authType);
            return authManager.authenticate(authType, credentials, sessionManager);
        } catch (Exception e) {
            logger.error("❌ Authentication failed for {}", authType, e);
            return false;
        }
    }

    public String scrape(String url) {
        try {
            logger.info("🕷️  Scraping {}", url);
            String html = sessionManager.getPageContent(url, proxyRotator.getNextProxy());
            return parser.parse(html);
        } catch (Exception e) {
            logger.error("❌ Scraping error for {}", url, e);
            return "{}";
        }
    }

    public List<String> scrapeList(ScraperConfig config) {
        List<String> results = new ArrayList<>();
        List<CompletableFuture<String>> futures = new ArrayList<>();

        logger.info("📋 Starting batch scrape of {} URLs", config.getUrls().size());

        for (String url : config.getUrls()) {
            futures.add(CompletableFuture.supplyAsync(() -> scrapeWithRetry(url, 3), executor));
        }

        for (CompletableFuture<String> future : futures) {
            try {
                String result = future.get();
                if (result != null && !result.isEmpty()) {
                    results.add(result);
                }
            } catch (Exception e) {
                logger.error("Error retrieving scraped result", e);
            }
        }

        logger.info("✅ Batch scrape complete: {} results collected", results.size());
        return results;
    }

    private String scrapeWithRetry(String url, int retries) {
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                return scrape(url);
            } catch (Exception e) {
                logger.warn("⚠️  Attempt {} failed for {}: {}", attempt, url, e.getMessage());
                if (attempt < retries) {
                    try {
                        long delay = 1000L * attempt;
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return "{}";
    }

    public void shutdown() {
        logger.info("🛑 Shutting down Beast Scraper");
        sessionManager.close();
        executor.shutdown();
    }
}