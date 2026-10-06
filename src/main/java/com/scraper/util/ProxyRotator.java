package com.scraper.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rotates through proxy servers for load balancing and anti-detection
 */
public class ProxyRotator {
    private static final Logger logger = LoggerFactory.getLogger(ProxyRotator.class);
    private final List<String> proxies;
    private int currentIndex = 0;

    public ProxyRotator() {
        this.proxies = new ArrayList<>();
        loadDefaultProxies();
    }

    private void loadDefaultProxies() {
        String proxyList = System.getenv("SCRAPER_PROXIES");
        if (proxyList != null && !proxyList.isEmpty()) {
            for (String proxy : proxyList.split(",")) {
                proxies.add(proxy.trim());
            }
            logger.info("Loaded {} proxies from SCRAPER_PROXIES environment variable", proxies.size());
        }
    }

    public String getNextProxy() {
        if (proxies.isEmpty()) {
            logger.debug("No proxies configured, using direct connection");
            return null;
        }
        String proxy = proxies.get(currentIndex);
        currentIndex = (currentIndex + 1) % proxies.size();
        return proxy;
    }

    public String getRandomProxy() {
        if (proxies.isEmpty()) {
            return null;
        }
        return proxies.get(new Random().nextInt(proxies.size()));
    }

    public void addProxy(String proxy) {
        proxies.add(proxy);
        logger.info("Added proxy: {}", proxy);
    }

    public void addProxies(List<String> proxyList) {
        proxies.addAll(proxyList);
        logger.info("Added {} proxies (total: {})", proxyList.size(), proxies.size());
    }

    public int getProxyCount() {
        return proxies.size();
    }
}
