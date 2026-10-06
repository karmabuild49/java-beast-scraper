package com.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraper.auth.LoginCredentials;
import com.scraper.config.ScraperConfig;
import com.scraper.scraper.BeastScraper;
import com.scraper.session.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BeastScraperCLI {
    private static final Logger logger = LoggerFactory.getLogger(BeastScraperCLI.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        try {
            switch (args[0]) {
                case "scrape" -> handleScrape(args);
                case "login" -> handleLogin(args);
                case "list" -> handleList(args);
                case "cookies" -> handleCookies(args);
                default -> {
                    System.err.println("Unknown command: " + args[0]);
                    printUsage();
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            logger.error("Fatal scraper error", e);
            System.exit(1);
        }
    }

    private static void handleScrape(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: scrape <url> [--auth-type <type>] [--credentials <file>] [--output <file>]");
            System.exit(1);
        }

        String url = args[1];
        String authType = getOption(args, "--auth-type");
        String credentialsFile = getOption(args, "--credentials");
        String outputFile = getOption(args, "--output", "output.json");

        BeastScraper scraper = new BeastScraper();
        if (authType != null && credentialsFile != null) {
            LoginCredentials credentials = mapper.readValue(Paths.get(credentialsFile).toFile(), LoginCredentials.class);
            scraper.authenticate(authType, credentials);
        }

        String resultJson = scraper.scrape(url);
        Files.writeString(Paths.get(outputFile), resultJson);
        logger.info("✓ Scrape complete; wrote output to {}", outputFile);
        scraper.shutdown();
    }

    private static void handleLogin(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: login <url> <auth-type> <credentials-file>");
            System.exit(1);
        }

        String url = args[1];
        String authType = args[2];
        String credentialsFile = args[3];

        LoginCredentials credentials = mapper.readValue(Paths.get(credentialsFile).toFile(), LoginCredentials.class);
        BeastScraper scraper = new BeastScraper();
        boolean ok = scraper.authenticate(authType, credentials);
        System.out.println(ok ? "✓ Authentication successful" : "✗ Authentication failed");
        scraper.shutdown();
    }

    private static void handleList(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: list <config-file> [--output <out.json>]");
            System.exit(1);
        }

        String configFile = args[1];
        String outputFile = getOption(args, "--output", "output_list.json");

        ScraperConfig config = mapper.readValue(Paths.get(configFile).toFile(), ScraperConfig.class);
        BeastScraper scraper = new BeastScraper();
        List<String> results = scraper.scrapeList(config);
        Files.writeString(Paths.get(outputFile), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
        logger.info("✓ List scrape complete; wrote {} results to {}", results.size(), outputFile);
        scraper.shutdown();
    }

    private static void handleCookies(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: cookies --load <file> [--export <output>]");
            System.exit(1);
        }

        String loadFile = getOption(args, "--load");
        String exportFile = getOption(args, "--export", "cookies_export.json");

        if (loadFile == null) {
            System.err.println("--load file is required");
            System.exit(1);
        }

        CookieManager cookieManager = new CookieManager();
        cookieManager.loadCookies(loadFile);
        String exportJson = cookieManager.exportCookies();
        Files.writeString(Paths.get(exportFile), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(exportJson)));
        logger.info("✓ Cookies exported to {}", exportFile);
    }

    private static String getOption(String[] args, String name) {
        return getOption(args, name, null);
    }

    private static String getOption(String[] args, String name, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (name.equals(args[i])) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }

    private static void printUsage() {
        System.out.println("""
            ╔═══════════════════════════════════════════════════════╗
            ║      Java Beast Scraper - Production Edition          ║
            ╚═══════════════════════════════════════════════════════╝
            
            COMMANDS:
              scrape <url>
                --auth-type <form|jwt|browser>
                --credentials <file>
                --output <file>
                
              login <url> <auth-type> <credentials-file>
                
              list <config-file> [--output <file>]
              
              cookies --load <file> [--export <output>]
            
            EXAMPLES:
              java -jar target/beast-scraper.jar scrape https://example.com
              java -jar target/beast-scraper.jar scrape https://example.com \\
                --auth-type form --credentials credentials.json
              java -jar target/beast-scraper.jar list batch_config.json
              java -jar target/beast-scraper.jar cookies --load cookies.json --export out.json
            
            AUTH TYPES:
              form    - Submit HTML form with username/password
              jwt     - POST JSON credentials to get JWT token
              browser - Use Playwright to automate browser login
            
            COOKIE FEATURES:
              - Auto-detection and storage after authentication
              - Persistent cookie jar for session reuse
              - Export cookies to JSON for inspection
            """);
    }
}