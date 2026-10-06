# Java Beast Scraper 🕷️

Production-ready Java web scraper with advanced authentication, session management, cookie storage, and batch processing.

## Features

✨ **Multi-Authentication Support**
- Form-based login with auto-detection
- JWT/Bearer token authentication
- Browser-based login with Playwright (handles JavaScript)

🍪 **Cookie Management**
- Automatic cookie detection and storage
- Persistent cookie jar for session reuse
- Cookie reload from storage
- Export cookies to JSON

🌐 **Web Scraping**
- HTML parsing with JSoup
- Custom CSS selector support
- JSON output format
- Batch scraping with concurrency

🔄 **Advanced Capabilities**
- Proxy rotation (round-robin & random)
- User-Agent rotation
- Exponential backoff retry logic
- Session persistence
- Concurrent URL processing

📊 **Enterprise Ready**
- Comprehensive logging
- Error handling and recovery
- Configuration management
- CLI interface

## Requirements

- Java 21+
- Maven 3.8+

## Build

```bash
mvn clean package
```

Creates: `target/beast-scraper.jar`

## Usage

### Single URL Scraping

```bash
java -jar target/beast-scraper.jar scrape https://example.com --output result.json
```

### Form-Based Login

**credentials.json**
```json
{
  "loginUrl": "https://example.com/login",
  "username": "user@example.com",
  "password": "password123",
  "formData": {"rememberMe": "true"},
  "saveCookies": "cookies.json"
}
```

```bash
java -jar target/beast-scraper.jar scrape https://example.com/dashboard \
  --auth-type form \
  --credentials credentials.json \
  --output result.json
```

### JWT Authentication

**jwt_creds.json**
```json
{
  "loginUrl": "https://api.example.com/auth/login",
  "username": "user@example.com",
  "password": "password123",
  "jwtTokenEndpoint": "https://api.example.com/auth/token",
  "saveCookies": "jwt_cookies.json"
}
```

```bash
java -jar target/beast-scraper.jar scrape https://api.example.com/data \
  --auth-type jwt \
  --credentials jwt_creds.json \
  --output result.json
```

### Browser-Based Authentication

**browser_creds.json**
```json
{
  "loginUrl": "https://example.com/login",
  "username": "user@example.com",
  "password": "password123",
  "usernameSelector": "input[name='email']",
  "passwordSelector": "input[name='password']",
  "submitSelector": "button[type='submit']",
  "saveCookies": "browser_cookies.json"
}
```

```bash
java -jar target/beast-scraper.jar scrape https://example.com/protected \
  --auth-type browser \
  --credentials browser_creds.json \
  --output result.json
```

### Test Authentication

```bash
java -jar target/beast-scraper.jar login https://example.com/login form credentials.json
```

### Batch Scraping

**batch_config.json**
```json
{
  "urls": [
    "https://example.com/page1",
    "https://example.com/page2",
    "https://example.com/page3"
  ],
  "authType": "form",
  "credentials": {
    "loginUrl": "https://example.com/login",
    "username": "user",
    "password": "pass"
  },
  "maxConcurrent": 5,
  "timeoutSeconds": 30
}
```

```bash
java -jar target/beast-scraper.jar list batch_config.json --output results.json
```

## Cookie Management

### Save Cookies After Authentication

Add `saveCookies` field to credentials:
```json
{
  "loginUrl": "https://example.com/login",
  "username": "user",
  "password": "pass",
  "saveCookies": "saved_cookies.json"
}
```

### Load Cookies from Storage

Add `loadCookies` field to credentials:
```json
{
  "loadCookies": "saved_cookies.json"
}
```

### Export Stored Cookies

```bash
java -jar target/beast-scraper.jar cookies --load saved_cookies.json --export display.json
```

## Proxy Configuration

Set environment variable with comma-separated proxy list:

```bash
export SCRAPER_PROXIES="proxy1.com:8080,proxy2.com:8080,proxy3.com:8080"
java -jar target/beast-scraper.jar scrape https://example.com
```

## Architecture

```
src/main/java/com/scraper/
├── BeastScraperCLI.java              # CLI entry point
├── scraper/
│   └── BeastScraper.java             # Main orchestrator
├── auth/
│   ├── AuthenticationManager.java    # Auth routing
│   └── LoginCredentials.java         # Credential model
├── session/
│   ├── SessionManager.java           # HTTP & browser sessions
│   └── CookieManager.java            # Cookie storage & detection
├── parser/
│   └── DataParser.java               # HTML parsing
├── config/
│   └── ScraperConfig.java            # Configuration model
└── util/
    └── ProxyRotator.java             # Proxy management
```

## Advanced Features

### Automatic Cookie Detection

The scraper automatically detects and stores cookies from all authentication flows:
- Form login: Captures Set-Cookie headers
- JWT: Stores Bearer tokens
- Browser: Extracts Playwright context cookies

### Session Persistence

Cookies and tokens are stored in JSON format for easy inspection and reuse:
```json
{
  "cookies": [
    {"name": "JSESSIONID", "value": "abc123", "domain": "example.com"},
    {"name": "_ga", "value": "GA1.2.123", "domain": ".example.com"}
  ],
  "tokens": {
    "jwt": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

### Error Recovery

Automatic retry with exponential backoff:
- Attempt 1: immediate
- Attempt 2: 2 second delay
- Attempt 3: 4 second delay

## Troubleshooting

### Authentication Fails
1. Verify credentials.json format
2. Check CSS selectors are correct (browser mode)
3. Enable debug logging:
   ```bash
   java -Dlogback.level=DEBUG -jar target/beast-scraper.jar scrape ...
   ```

### Connection Issues
1. Add proxy configuration
2. Check network connectivity
3. Verify firewall rules

### Cookie Issues
1. Check saveCookies path is writable
2. Verify loadCookies file exists
3. Check cookie domain matches target URL

## License

MIT License