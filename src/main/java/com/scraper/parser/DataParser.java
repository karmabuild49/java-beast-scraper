package com.scraper.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DataParser {
    private static final Logger logger = LoggerFactory.getLogger(DataParser.class);
    private final ObjectMapper mapper;

    public DataParser() {
        this.mapper = new ObjectMapper();
    }

    public String parse(String html) {
        try {
            Document doc = Jsoup.parse(html);
            ObjectNode result = mapper.createObjectNode();

            String title = doc.title();
            result.put("title", title);
            logger.debug("Page title: {}", title);

            String text = doc.body().text();
            result.put("content", text.length() > 1000 ? text.substring(0, 1000) : text);

            ArrayNode links = mapper.createArrayNode();
            Elements linkElements = doc.select("a[href]");
            linkElements.stream().limit(20).forEach(link -> {
                ObjectNode linkNode = mapper.createObjectNode();
                linkNode.put("text", link.text());
                linkNode.put("href", link.attr("href"));
                links.add(linkNode);
            });
            result.set("links", links);

            ArrayNode images = mapper.createArrayNode();
            Elements imgElements = doc.select("img[src]");
            imgElements.stream().limit(10).forEach(img -> {
                ObjectNode imgNode = mapper.createObjectNode();
                imgNode.put("src", img.attr("src"));
                imgNode.put("alt", img.attr("alt"));
                images.add(imgNode);
            });
            result.set("images", images);

            result.put("status", "success");
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            logger.error("Parse error", e);
            ObjectNode error = mapper.createObjectNode();
            error.put("status", "error");
            error.put("message", e.getMessage());
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(error);
            } catch (Exception ex) {
                return "{}";
            }
        }
    }

    public String parseWithSelectors(String html, Map<String, String> selectors) {
        try {
            Document doc = Jsoup.parse(html);
            ObjectNode result = mapper.createObjectNode();

            selectors.forEach((key, selector) -> {
                Elements elements = doc.select(selector);
                ArrayNode arr = mapper.createArrayNode();
                elements.forEach(el -> arr.add(el.text()));
                result.set(key, arr);
            });

            result.put("status", "success");
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            logger.error("Parse error with selectors", e);
            ObjectNode error = mapper.createObjectNode();
            error.put("status", "error");
            error.put("message", e.getMessage());
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(error);
            } catch (Exception ex) {
                return "{}";
            }
        }
    }
}