package ua.dscorp.poessence.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.dscorp.poessence.Application;
import ua.dscorp.poessence.data.CurrencyDetail;
import ua.dscorp.poessence.data.Line;
import ua.dscorp.poessence.data.SimpleLine;
import ua.dscorp.poessence.data.containers.CurrencyDetailsContainer;
import ua.dscorp.poessence.data.containers.LinesContainer;
import ua.dscorp.poessence.util.ItemType;
import ua.dscorp.poessence.windows.FilterController;
import ua.dscorp.poessence.windows.MainWindowController;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ua.dscorp.poessence.Application.TOOL_NAME;
import static ua.dscorp.poessence.windows.MainWindowController.itemsStore;
import static ua.dscorp.poessence.windows.MainWindowController.task;

public final class PoeNinjaLoader {

    private static final String DIVINE_ORB = "divine-orb";

    public static ExecutorService executorService;

    private static float divValue;

    private static Map<String, Line> currencyStore = new HashMap<>();

    private final MainWindowController mainWindowController;

    private final boolean isExtendedLaunch;

    public PoeNinjaLoader(MainWindowController mainWindowController, boolean isExtendedLaunch) {
        this.mainWindowController = mainWindowController;
        this.isExtendedLaunch = isExtendedLaunch;
    }

    public void launchLoad() throws IOException, URISyntaxException {
        Platform.runLater(() -> mainWindowController.warnings.setText(""));

        mainWindowController.refreshButton.setDisable(true);
        mainWindowController.refreshButtonExt.setDisable(true);
        mainWindowController.refreshButtonAll.setDisable(true);
        // e.g. https://poe.ninja/api/data/itemoverview?league=Necropolis&type=Essence
        fetchData("https://poe.ninja/api/data/" + mainWindowController.itemType.getType() + "overview?league=" + mainWindowController.leagueChoiceBox.getValue() + "&type=" + mainWindowController.itemType.getName(), true);
        fetchData("https://poe.ninja/api/data/" + ItemType.CURRENCY.getType() + "overview?league=" + mainWindowController.leagueChoiceBox.getValue() + "&type=" + ItemType.CURRENCY.getName(), false);
        divValue = currencyStore.get(DIVINE_ORB).getChaosValue();
        itemsStore.forEach(line -> line.setDivineValue(line.getChaosValue() / divValue));

        ObservableList<Line> observableLineList = FXCollections.observableArrayList(itemsStore);
        mainWindowController.tableView.setItems(observableLineList);
    }


    public void fetchData(String urlString, boolean isPoeTradeRequest) throws IOException, URISyntaxException {
        URL url = new URI(urlString).toURL();
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            LinesContainer itemLines = objectMapper.readValue(json.toString(), LinesContainer.class);

            List<Line> items = itemLines.getLines();

            if (mainWindowController.itemType.getType().equals(ItemType.CURRENCY.getType())) {
                CurrencyDetailsContainer currencyDetails = objectMapper.readValue(json.toString(), CurrencyDetailsContainer.class);
                List<CurrencyDetail> currencyDetailsList = currencyDetails.getCurrencyDetails();
                items.forEach(line -> {
                    if (currencyDetailsList.stream().anyMatch(cd -> cd.getName().equals(line.getName())))
                        line.setIcon(currencyDetailsList.stream().filter(cd -> cd.getName().equals(line.getName())).findFirst().get().getIcon());
                });
            }

            if (isPoeTradeRequest) {
                List<Line> itemsFiltered = new ArrayList<>();
                for (Line line : items) {
                    try {
                        if (line.getMapTier() == null
                                || mainWindowController.minEssenceTier.getText().isEmpty()
                                || Integer.parseInt(line.getMapTier()) >= Integer.parseInt(mainWindowController.minEssenceTier.getText())) {
                            itemsFiltered.add(line);
                        }
                    } catch (NumberFormatException e) {
                        mainWindowController.warnings.setText("wrong format of essence tier: must be empty or number");
                        return;
                    }
                }

                File file = new File("./filter/" + mainWindowController.itemType.getName() + ".json");
                boolean isFirstLaunch = !file.exists();
                // In case of extended launch, window with poe.ninja data to be opened, list of items to load chosen and filtered, and saved for future usages.
                if (isExtendedLaunch || isFirstLaunch) {
                    FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("filter-view.fxml"));
                    Scene scene = new Scene(fxmlLoader.load(), 800, 600);
                    FilterController filterController = fxmlLoader.getController();
                    filterController.setItems(itemsFiltered);
                    filterController.setItemType(mainWindowController.itemType.getName());
                    filterController.loadFile();
                    Stage stage = new Stage();
                    stage.setTitle(TOOL_NAME + " - Filter");
                    stage.setScene(scene);
                    stage.setMaximized(false);
                    stage.showAndWait();

                    itemsFiltered = filterController.getFilteredList();
                    if (itemsFiltered == null) {
                        mainWindowController.refreshButton.setDisable(false);
                        mainWindowController.refreshButtonExt.setDisable(false);
                        mainWindowController.refreshButtonAll.setDisable(false);
                        return;
                    }
                }
                else {
                    itemsFiltered = loadPrevSettings(itemsFiltered, file);
                }

                if (task != null && (task.isRunning() || task.getState() == Worker.State.READY)) {
                    task.addItems(itemsFiltered);
                }
                else {
                    if (task != null) {
                        task.cancel();
                    }
                    task = new PoeTradeLoaderTask(mainWindowController, itemsFiltered);
                    executorService = Executors.newFixedThreadPool(2);
                    executorService.submit(task);
                }
                itemsStore.clear();
                itemsStore.addAll(itemsFiltered);
            } else {
                currencyStore.clear();
                currencyStore.putAll(items.stream().collect(Collectors.toMap(Line::getDetailsId, line -> line)));
            }
        }
    }

    private List<Line> loadPrevSettings(List<Line> itemsFiltered, File file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        List<SimpleLine> simpleLines = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, SimpleLine.class));
        return itemsFiltered.stream()
                .filter(line -> simpleLines.stream()
                        .anyMatch(simpleLine -> simpleLine.getDetailsId().equals(line.getDetailsId()) && simpleLine.getIsIncluded())).toList();
    }
}
