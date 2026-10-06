# Java Beast Scraper

A production-ready Java scraper that can:
- scrape normal pages,
- handle form login, JWT/session-cookie login, and browser-driven login,
- scrape list output efficiently,
- retry and rotate proxies/user agents,
- export structured JSON.

## Features

- Form login support
- JWT/session-cookie auth support
- Browser automation via Playwright
- Concurrent list scraping
- Proxy rotation and retry logic
- JSON output generation
- CLI runner

## Requirements

- Java 21+
- Maven 3.8+

## Build

```bash
mvn clean package
```

## Example usage

```bash
java -jar target/beast-scraper.jar scrape https://example.com --output out.json
java -jar target/beast-scraper.jar scrape https://example.com --auth-type form --credentials credentials.json
java -jar target/beast-scraper.jar login https://example.com/login form credentials.json
java -jar target/beast-scraper.jar list scrape_config.json --output results.json
```

## Credentials file example

```json
{
  "loginUrl": "https://example.com/login",
  "username": "demo",
  "password": "secret",
  "formData": {
    "rememberMe": "true"
  },
  "usernameSelector": "input[name='email']",
  "passwordSelector": "input[name='password']",
  "submitSelector": "button[type='submit']"
}
```

## License

MIT
