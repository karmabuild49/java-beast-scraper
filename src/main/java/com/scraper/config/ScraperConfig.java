package com.scraper.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ScraperConfig {
    @JsonProperty
    private List<String> urls;
    @JsonProperty
    private String authType;
    @JsonProperty
    private Map<String, String> credentials;
    @JsonProperty
    private Map<String, String> selectors;
    @JsonProperty
    private int maxConcurrent = 5;
    @JsonProperty
    private int timeoutSeconds = 30;

    public List<String> getUrls() { return urls; }
    public void setUrls(List<String> urls) { this.urls = urls; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public Map<String, String> getCredentials() { return credentials; }
    public void setCredentials(Map<String, String> credentials) { this.credentials = credentials; }

    public Map<String, String> getSelectors() { return selectors; }
    public void setSelectors(Map<String, String> selectors) { this.selectors = selectors; }

    public int getMaxConcurrent() { return maxConcurrent; }
    public void setMaxConcurrent(int maxConcurrent) { this.maxConcurrent = maxConcurrent; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}