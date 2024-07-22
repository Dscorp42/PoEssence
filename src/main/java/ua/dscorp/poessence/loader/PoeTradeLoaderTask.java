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
import java.util.*;

/**
 * To update poe trade data on background
 */
public class PoeTradeLoaderTask extends Task<List<Line>> {

    private final MainWindowController mainWindowController;
    private ArrayDeque<Line> items;
    private int processed;
    // Needed and updated between requests to poe trade from header.
    public float delayBetweenRequests = 1;
    private int total;
    private boolean firstFetch;
    private boolean noAutoSave = false;
    private boolean fastAvailable = true;

    public PoeTradeLoaderTask(MainWindowController mainWindowController, List<Line> items) {
        this.mainWindowController = mainWindowController;
        this.items = new ArrayDeque<>(items);
        processed = 0;
        this.total = items.size();
        this.firstFetch = true;
    }

    @Override
    protected List<Line> call() throws Exception {
        Platform.runLater(() -> mainWindowController.warnings2.setText(""));
        int result = 0;
        boolean isFirstUpdate = true;
        if (mainWindowController.constantUpdate.isSelected()) {
            Platform.runLater(() -> mainWindowController.warnings2.setText("Constant updates checked, no autosave on end."));
            noAutoSave = true;
            fastAvailable =false;
        }
        while (mainWindowController.constantUpdate.isSelected() || isFirstUpdate) {
            if (isCancelled() || result != 0 || items.isEmpty()) {
                break;
            }
            while (items.peekFirst() != null) {
                if (processed >= total) {
                    isFirstUpdate = false;
                    processed = 0;
                }

                Line item = items.pollFirst();
                if (isCancelled()) {
                    break;
                }
                result = getBulkItems(item, false, isFirstUpdate);
                if (result == -1) {
                    Platform.runLater(() -> mainWindowController.warnings.setText("Abnormal result, quit loading."));
                    cancel();
                    break;
                }
                processed++;
                mainWindowController.tableView.refresh();
                if (mainWindowController.constantUpdate.isSelected()) {
                    items.addLast(item);
                }
            }
            try {
                if (!noAutoSave) {
                    mainWindowController.saveData();
                }
            } catch (IOException e) {
                Platform.runLater(() -> mainWindowController.warnings.setText("poe trade fetch succeeded, failed to save data"));
            }
        }
        return null;
    }

    public void addItems(List<Line> newItems) {
        Platform.runLater(() -> mainWindowController.warnings2.setText("Additional items added, no autosave on end."));
        noAutoSave = true;
        fastAvailable = false;
        items.addAll(newItems);
        total = items.size();
    }

    @Override
    protected void succeeded() {
        mainWindowController.warnings.setText("poe trade fetch succeeded");
        mainWindowController.refreshButton.setDisable(false);
        mainWindowController.refreshButtonExt.setDisable(false);
        mainWindowController.refreshButtonAll.setDisable(false);
        mainWindowController.constantUpdate.setDisable(false);
        mainWindowController.fastUpdate.setDisable(false);
        MainWindowController.task = null;
    }

    @Override
    protected void cancelled() {
        mainWindowController.warnings.setText("poe trade fetch cancelled");
        mainWindowController.refreshButton.setDisable(false);
        mainWindowController.refreshButtonExt.setDisable(false);
        mainWindowController.refreshButtonAll.setDisable(false);
        mainWindowController.constantUpdate.setDisable(false);
        mainWindowController.fastUpdate.setDisable(false);
        MainWindowController.task = null;
    }

    @Override
    protected void failed() {
        mainWindowController.warnings.setText("poe trade fetch failed");
        mainWindowController.refreshButton.setDisable(false);
        mainWindowController.refreshButtonExt.setDisable(false);
        mainWindowController.refreshButtonAll.setDisable(false);
        mainWindowController.constantUpdate.setDisable(false);
        mainWindowController.fastUpdate.setDisable(false);
        MainWindowController.task = null;
    }

    private int getBulkItems(Line item, boolean isRetry, boolean isFirstUpdate) throws InterruptedException {
        String detailsId = item.getDetailsId();
        //double delay in case of repeated refresh.
        long totalDelay = (long) (isFirstUpdate ? (delayBetweenRequests * 1000) : (delayBetweenRequests * 1300));
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

                TreeMap<Integer, Integer> rulesMap = new TreeMap<>();
                Map<Integer, Integer> currentMap = new HashMap<>();
                for (String rule : rules.split(",")) {
                    String ruleValues = connection.getHeaderField("X-Rate-Limit-" + rule);
                    String currentValues = connection.getHeaderField("X-Rate-Limit-" + rule + "-State");
                    String[] ruleValuesArray = ruleValues.split(",");
                    for (String valuesPerTimeFrame : ruleValuesArray) {
                        String[] seconds = valuesPerTimeFrame.split(":");
                        int maxRequests = Integer.parseInt(seconds[0]);
                        int timeFrame = Integer.parseInt(seconds[1]);
                        if (!rulesMap.containsKey(timeFrame) || rulesMap.get(timeFrame) < maxRequests) {
                            rulesMap.put(timeFrame, maxRequests);
                        }
                    }
                    String[] currentValuesArray = currentValues.split(",");
                    for (String valuesPerTimeFrame : currentValuesArray) {
                        String[] seconds = valuesPerTimeFrame.split(":");
                        int maxRequests = Integer.parseInt(seconds[0]);
                        int timeFrame = Integer.parseInt(seconds[1]);
                        if (!currentMap.containsKey(timeFrame) || currentMap.get(timeFrame) < maxRequests) {
                            currentMap.put(timeFrame, maxRequests);
                        }
                    }
                    System.out.println("Rule hit values: " + ruleValues);
                    System.out.println("Current hit values (state): " + currentValues);
                }

                if (Objects.equals(currentMap.get(rulesMap.lastEntry().getKey()), rulesMap.lastEntry().getValue())) {
                    Platform.runLater(() -> mainWindowController.warnings2.setText("Largest timeframe for hits to POE trade reached limit:"
                            + currentMap.get(rulesMap.lastEntry().getKey()) + " hits reached during last " + rulesMap.lastEntry().getValue() +
                            " seconds. Request aborted to avoid timeout."));

                    return -1;
                }

                int maxAmount = getMaxAmountForFastSearch(rulesMap);
                if (mainWindowController.fastUpdate.isSelected() && total < maxAmount && fastAvailable) {
                    if (firstFetch && isOnCooldown(currentMap)) {
                        Platform.runLater(() -> mainWindowController.warnings2.setText("Limits are still not cooled off from last fast fetch usage. Please wait or use regular load."));

                        return -1;
                    }
                    firstFetch = false;
                    getFastDelay(rulesMap, currentMap);
                }
                else {
                    getUsualDelay(rulesMap);
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    PoeTradeParser.parseResult(response.toString(), mainWindowController.accountName.getText(), item);
                    System.out.println("delayBetweenRequests: " + delayBetweenRequests);
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
        } catch (IOException e) {
            System.out.println("Error in connection: " + e.getMessage());
        }
        return 0;
    }

    private boolean isOnCooldown(Map<Integer, Integer> currentMap) {
        return currentMap.entrySet().stream().anyMatch(entry -> entry.getValue() > 1);
    }

    private int getMaxAmountForFastSearch(TreeMap<Integer, Integer> rulesMap) {
        if (rulesMap.size() < 2) {
            return 10;
        }
        Iterator<Map.Entry<Integer, Integer>> iterator = rulesMap.entrySet().iterator();

        // Skip the first entry
        iterator.next();

        // Return the second entry
        return iterator.next().getValue();
    }

    private void getUsualDelay(TreeMap<Integer, Integer> rulesMap) {
        for (Map.Entry<Integer, Integer> rule : rulesMap.entrySet()) {
            float maxRequests = rule.getValue();
            float timeFrame = rule.getKey();
            float currentValue = timeFrame / maxRequests + 0.5f;
            if (currentValue > delayBetweenRequests) {
                delayBetweenRequests = currentValue;
            }
        }
    }

    private void getFastDelay(TreeMap<Integer, Integer> rulesMap, Map<Integer, Integer> currentMap) {
        for (Map.Entry<Integer, Integer> rule : rulesMap.entrySet()) {
            if (isNotFullOnThisAndBiggerTimeFrames(rule, rulesMap, currentMap)) {
                delayBetweenRequests = (float) rule.getKey() / rule.getValue();
                return;
            }
        }
        delayBetweenRequests = (float) rulesMap.lastEntry().getKey() / rulesMap.lastEntry().getValue() + 1;
    }

    private static boolean isNotFullOnThisAndBiggerTimeFrames(Map.Entry<Integer, Integer> rule,
                                                              TreeMap<Integer, Integer> rulesMap,
                                                              Map<Integer, Integer> currentMap) {
        return rulesMap.entrySet().stream().filter(ruleEntry -> ruleEntry.getKey() >= rule.getKey()).allMatch(ruleEntry -> {
            Integer currentRequests = currentMap.get(ruleEntry.getKey());
            Integer totalRequests = ruleEntry.getValue();
            return currentRequests + 1 < totalRequests;
        });
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
