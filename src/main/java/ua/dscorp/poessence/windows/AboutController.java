package ua.dscorp.poessence.windows;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import ua.dscorp.poessence.Application;

import java.io.*;

public class AboutController {

    @FXML
    WebView webView;

    @FXML
    protected void initialize() {
        try {
            String content = readResourceAsString("/ua/dscorp/poessence/About.txt");
            String htmlContent = convertMarkdownToHtml(content);
            webView.getEngine().loadContent(htmlContent);
        } catch (IOException e) {
            webView.getEngine().loadContent("<p>Error reading the README.md file.</p>");
            e.printStackTrace();
        }
    }

    // Method to convert Markdown to HTML
    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(parser.parse(markdown));
    }

    public static String readResourceAsString(String resourcePath) throws IOException {
        // Use the ClassLoader to get the resource as an InputStream
        try (InputStream inputStream = Application.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Check if the resource was found
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            // Read the content of the file into a StringBuilder
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            return content.toString().trim(); // Trim to remove the last newline character
        }
    }
}