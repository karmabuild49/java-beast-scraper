package com.scraper.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class LoginCredentials {
    @JsonProperty
    private String loginUrl;
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty
    private Map<String, String> formData;
    @JsonProperty
    private String usernameSelector;
    @JsonProperty
    private String passwordSelector;
    @JsonProperty
    private String submitSelector;
    @JsonProperty
    private String jwtTokenEndpoint;
    @JsonProperty
    private String saveCookies;
    @JsonProperty
    private String loadCookies;

    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Map<String, String> getFormData() { return formData; }
    public void setFormData(Map<String, String> formData) { this.formData = formData; }

    public String getUsernameSelector() { return usernameSelector; }
    public void setUsernameSelector(String usernameSelector) { this.usernameSelector = usernameSelector; }

    public String getPasswordSelector() { return passwordSelector; }
    public void setPasswordSelector(String passwordSelector) { this.passwordSelector = passwordSelector; }

    public String getSubmitSelector() { return submitSelector; }
    public void setSubmitSelector(String submitSelector) { this.submitSelector = submitSelector; }

    public String getJwtTokenEndpoint() { return jwtTokenEndpoint; }
    public void setJwtTokenEndpoint(String jwtTokenEndpoint) { this.jwtTokenEndpoint = jwtTokenEndpoint; }

    public String getSaveCookies() { return saveCookies; }
    public void setSaveCookies(String saveCookies) { this.saveCookies = saveCookies; }

    public String getLoadCookies() { return loadCookies; }
    public void setLoadCookies(String loadCookies) { this.loadCookies = loadCookies; }
}