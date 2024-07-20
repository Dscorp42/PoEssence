package ua.dscorp.poessence.loader;

import javafx.application.Platform;
import javafx.concurrent.Task;
import ua.dscorp.poessence.windows.MainWindowController;
import ua.dscorp.poessence.data.BulkItem;
import ua.dscorp.poessence.data.Line;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;

/**
 * To update poe trade data on background
 */
public class PoeTradeLoaderTask extends Task<List<Line>> {

    private final MainWindowController mainWindowController;
    private ArrayDeque<Line> items;
    private int processed;
    // Needed and updated between requests to poe trade from header.
    public static float delayBetweenRequests = 1;
    private int total;

    public PoeTradeLoaderTask(MainWindowController mainWindowController, List<Line> items) {
        this.mainWindowController = mainWindowController;
        this.items = new ArrayDeque<>(items);
        processed = 1;
        this.total = items.size();
    }

    @Override
    protected List<Line> call() throws Exception {

        boolean isFirstUpdate = true;
        while (mainWindowController.constantUpdate.isSelected() || isFirstUpdate) {
            if (isCancelled()) {
                break;
            }
            while (items.peekFirst() != null) {
                if (processed >= total) {
                    isFirstUpdate = false;
                    processed = 1;
                }

                Line item = items.pollFirst();
                if (isCancelled()) {
                    break;
                }
                getBulkItems(item, false, isFirstUpdate);
                processed++;
                mainWindowController.tableView.refresh();
                if (mainWindowController.constantUpdate.isSelected()) {
                    items.addLast(item);
                }
            }
            try {
                mainWindowController.saveData();
            } catch (IOException e) {
                mainWindowController.warnings.setText("poe trade fetch succeeded, failed to save data");
            }
        }
        return null;
    }

    public void addItems(List<Line> newItems) {
        items.addAll(newItems);
        total = items.size();
    }

    @Override
    protected void succeeded() {
        mainWindowController.warnings.setText("poe trade fetch succeeded");
        mainWindowController.refreshButton.setDisable(false);
        mainWindowController.refreshButtonExt.setDisable(false);
        mainWindowController.refreshButtonAll.setDisable(false);
        MainWindowController.task = null;
    }

    @Override
    protected void cancelled() {
        mainWindowController.warnings.setText("poe trade fetch cancelled");
        mainWindowController.refreshButton.setDisable(false);
        mainWindowController.refreshButtonExt.setDisable(false);
        mainWindowController.refreshButtonAll.setDisable(false);
        MainWindowController.task = null;
    }

    @Override
    protected void failed() {
        mainWindowController.warnings.setText("poe trade fetch failed");
        mainWindowController.refreshButton.setDisable(false);
        mainWindowController.refreshButtonExt.setDisable(false);
        mainWindowController.refreshButtonAll.setDisable(false);
        MainWindowController.task = null;
    }

    private void getBulkItems(Line item, boolean isRetry, boolean isFirstUpdate) throws InterruptedException {
        String detailsId = item.getDetailsId();
        //double delay in case of repeated refresh.
        long totalDelay = (long) (isFirstUpdate ? (delayBetweenRequests * 1000) : (delayBetweenRequests * 2000));
        Thread.sleep(totalDelay);
        Platform.runLater(() -> mainWindowController.warnings.setText("Estimated time left (minutes): "
                + BigDecimal.valueOf(((total - processed) * delayBetweenRequests) / 60).setScale(0, RoundingMode.HALF_UP)
                + ". " + processed + " of " + total + " done." +
                " Delay:" + BigDecimal.valueOf(totalDelay / 1000).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "s."));

        String url = "https://www.pathofexile.com/api/trade/exchange/" + mainWindowController.leagueChoiceBox.getValue();

        String jsonInputString =
                "{\"query\":{\"status\":{\"option\":\"online\"},\"have\":[\"divine\"],\"want\":[\""
                        + detailsId
                        + "\"],\"stock\":{\"min\":null,\"max\":null}},\"sort\":{\"have\":\"asc\"},\"engine\":\"new\"}";

        try {
            HttpURLConnection connection = getHttpURLConnection(url, jsonInputString);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                String rules = connection.getHeaderField("X-Rate-Limit-Rules");
                for (String rule : rules.split(",")) {
                    String ruleValues = connection.getHeaderField("X-Rate-Limit-" + rule);
                    String currentValues = connection.getHeaderField("X-Rate-Limit-" + rule + "-State");
                    String[] ruleValuesArray = ruleValues.split(",");
                    for (String valuesPerTimeFrame : ruleValuesArray) {
                        String[] seconds = valuesPerTimeFrame.split(":");
                        float maxRequests = Float.parseFloat(seconds[0]);
                        float timeFrame = Float.parseFloat(seconds[1]);
                        float currentValue = timeFrame / maxRequests + 0.5f;
                        if (currentValue > delayBetweenRequests) {
                            delayBetweenRequests = currentValue;
                        }
                    }

                    System.out.println("Rule hit values: " + ruleValues);
                    System.out.println("Current hit values (state): " + currentValues);
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    List<BulkItem> bulkItems = PoeTradeParser.parseResult(response.toString(), mainWindowController.accountName.getText());
                    System.out.println("delayBetweenRequests: " + delayBetweenRequests);
                    item.setBulkItems(bulkItems);
                }
            }
            // too many requests
            else if (responseCode == 429) {
                delayBetweenRequests = Integer.parseInt(connection.getHeaderField("Retry-After"));
                Platform.runLater(() -> mainWindowController.warnings.setText(("too many requests, retry after " + delayBetweenRequests)));
                if (!isRetry) {
                    getBulkItems(item, true, isFirstUpdate);
                }
            } else {
                System.err.println("POST request not worked: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error in connection: " + e.getCause());
        }
    }

    private HttpURLConnection getHttpURLConnection(String url, String jsonInputString) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "gladushdima@gmail.com");
        connection.setRequestProperty("Cookie", "POESESSID=" + mainWindowController.POESESSID.getText());
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }
}
