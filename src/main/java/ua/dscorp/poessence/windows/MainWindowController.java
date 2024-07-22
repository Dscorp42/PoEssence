package ua.dscorp.poessence.windows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ua.dscorp.poessence.Application;
import ua.dscorp.poessence.data.Line;
import ua.dscorp.poessence.loader.PoeNinjaLoader;
import ua.dscorp.poessence.loader.PoeTradeLoaderTask;
import ua.dscorp.poessence.util.ItemType;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ua.dscorp.poessence.Application.*;
import static ua.dscorp.poessence.util.PersistenceHandler.loadPersistenceSettings;
import static ua.dscorp.poessence.util.UtilClass.*;
import static ua.dscorp.poessence.util.TableConfigurator.*;

public final class MainWindowController {

    public TableView<Line> tableView;
    @FXML
    public TableView<Line> tableViewEssences;
    @FXML
    public TableView<Line> tableViewFossils;
    @FXML
    public TableView<Line> tableViewFragments;
    @FXML
    public TableView<Line> tableViewCurrency;
    @FXML
    public ChoiceBox<String> leagueChoiceBox;
    @FXML
    public ChoiceBox<String> snapshotChoiceBox;
    @FXML
    public TextField POESESSID;
    @FXML
    public TextField accountName;
    @FXML
    public TextField threshold;
    @FXML
    public TextField ninjaPriceMultiplier;
    @FXML
    public TextField minEssenceTier;
    @FXML
    public CheckBox constantUpdate;
    @FXML
    public CheckBox fastUpdate;
    @FXML
    public CheckBox hourlyUpdate;
    @FXML
    public Button refreshButton;
    @FXML
    public Button refreshButtonExt;
    @FXML
    public Button refreshButtonAll;
    @FXML
    public VBox mainWindow;
    @FXML
    public Label warnings;
    @FXML
    public Label warnings2;
    @FXML
    public Label lastUpdated;
    @FXML
    public TabPane tabPane;
    @FXML
    public AnchorPane anchorPaneEssences;
    @FXML
    public AnchorPane anchorPaneFossils;
    @FXML
    public AnchorPane anchorPaneFragments;
    @FXML
    public AnchorPane anchorPaneCurrency;

    public ItemType itemType = ItemType.ESSENCE;

    public List<ItemType> preloadedTabs = new ArrayList<>();

    public static List<Line> itemsStore;
    public static List<Line> itemsStoreEssences = new ArrayList<>();
    public static List<Line> itemsStoreFossils = new ArrayList<>();
    public static List<Line> itemsStoreFragments = new ArrayList<>();
    public static List<Line> itemsStoreCurrency = new ArrayList<>();

    // Fetch objects to shut down at finish.
    public static volatile PoeTradeLoaderTask task;

    // Used to switch between snapshots with buttons.
    private AtomicInteger lastSize;
    private AtomicInteger lastSizeEssences = new AtomicInteger();
    private AtomicInteger lastSizeFossils = new AtomicInteger();
    private AtomicInteger lastSizeFragments = new AtomicInteger();
    private AtomicInteger lastSizeCurrency = new AtomicInteger();

    Map<ItemType, LocalDateTime> timeUpdated = new HashMap<>();
    Map<ItemType, String> snapshotName = new HashMap<>();

    @FXML
    public void initialize() throws IOException {

        tableView = tableViewEssences;
        itemsStore = itemsStoreEssences;
        lastSize = lastSizeEssences;

        loadPersistenceSettings(this);

        perTableActivities();

        accountName.textProperty().addListener((observableValue, oldVal, newVal) -> {
            tableView.getItems().stream().filter(line -> line.getBulkItems() != null).forEach(line -> line.getBulkItems().forEach(bulkItem -> bulkItem.setYou(bulkItem.getName().equals(newVal))));
            tableView.refresh();
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                try {
                    onTabSwitch(newTab);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void perTableActivities() throws IOException {

        loadSnapshotChoices(itemType);
        tableView.refresh();
        if (preloadedTabs.contains(itemType)) {
           return;
        }
        preloadedTabs.add(itemType);

        configureTable(tableView, threshold.getText());

        try {
            prefillTable();
        }
        catch (Exception e) {
            warnings.setText(e.toString());
            }
        listenersForRealTimeTableUpdate();

        tableView.prefHeightProperty().bind(((AnchorPane) tabPane.getTabs().getFirst().getContent()).heightProperty());
        tableView.prefWidthProperty().bind(((AnchorPane) tabPane.getTabs().getFirst().getContent()).widthProperty());
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        try {
        if (code == KeyCode.LEFT || code == KeyCode.KP_LEFT || event.isAltDown() && code == KeyCode.Z) {
            List<String> files = getTableContentFileNames(itemType);
            lastSize.set(changeIndexIfPossible(files, lastSize.get(), -1));
            String lastFile = chooseFile(files, lastSize.get());
            loadSnapshot(lastFile);
        }
        else if (code == KeyCode.RIGHT || code == KeyCode.KP_RIGHT || event.isAltDown() && code == KeyCode.C) {
            List<String> files = getTableContentFileNames(itemType);
            lastSize.set(changeIndexIfPossible(files, lastSize.get(), 1));
            String lastFile = chooseFile(files, lastSize.get());
            loadSnapshot(lastFile);
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int changeIndexIfPossible(List<String> files, int lastSize, int increment) {
        return (0 < (lastSize + increment) && (lastSize + increment) <= files.size()) ? lastSize + increment : lastSize;
    }

    private void prefillTable() throws IOException {
        List<String> files = getTableContentFileNames(itemType);
        if (files.isEmpty()) {
            // No prev saved data.
            return;
        }
        lastSize.set(files.size());

        String lastFile = chooseFile(files, lastSize.get());
        loadSnapshot(lastFile);
    }

    private static String chooseFile(List<String> files, int size) {
        return files.get(size - 1);
    }


    private void listenersForRealTimeTableUpdate() {
        ninjaPriceMultiplier.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int i = Integer.parseInt(newValue);
                itemsStore.forEach(line -> line.setNinjaPriceMultiplier(i));
                tableView.refresh();
            } catch (NumberFormatException e) {
                itemsStore.forEach(line -> line.setNinjaPriceMultiplier(100));
                tableView.refresh();
            }
        });
    }

    private void onTabSwitch(Tab newTab) throws IOException {
        if (newTab.getContent() == anchorPaneEssences) {
            itemType = ItemType.ESSENCE;
            tableView = tableViewEssences;
            itemsStore = itemsStoreEssences;
            lastSize = lastSizeEssences;
            perTableActivities();
            if (timeUpdated.containsKey(itemType))
                updateLastUpdated(timeUpdated.get(itemType));
            if (snapshotName.containsKey(itemType))
                snapshotChoiceBox.setValue(snapshotName.get(itemType));

        } else if (newTab.getContent() == anchorPaneFossils) {
            itemType = ItemType.FOSSIL;
            tableView = tableViewFossils;
            itemsStore = itemsStoreFossils;
            lastSize = lastSizeFossils;
            perTableActivities();
            if (timeUpdated.containsKey(itemType))
                updateLastUpdated(timeUpdated.get(itemType));
            if (snapshotName.containsKey(itemType))
                snapshotChoiceBox.setValue(snapshotName.get(itemType));
        }
        else if (newTab.getContent() == anchorPaneFragments) {
            itemType = ItemType.FRAGMENT;
            tableView = tableViewFragments;
            itemsStore = itemsStoreFragments;
            lastSize = lastSizeFragments;
            perTableActivities();
            if (timeUpdated.containsKey(itemType))
                updateLastUpdated(timeUpdated.get(itemType));
            if (snapshotName.containsKey(itemType))
                snapshotChoiceBox.setValue(snapshotName.get(itemType));
        }
        else if (newTab.getContent() == anchorPaneCurrency) {
            itemType = ItemType.CURRENCY;
            tableView = tableViewCurrency;
            itemsStore = itemsStoreCurrency;
            lastSize = lastSizeCurrency;
            perTableActivities();
            if (timeUpdated.containsKey(itemType))
                updateLastUpdated(timeUpdated.get(itemType));
            if (snapshotName.containsKey(itemType))
                snapshotChoiceBox.setValue(snapshotName.get(itemType));
        }

    }

    @FXML
    public void onRefreshButtonClick() throws IOException, URISyntaxException {
        PoeNinjaLoader ninjaLoader = new PoeNinjaLoader(this, false);
        ninjaLoader.launchLoad();
    }

    @FXML
    public void onRefreshExtButtonClick() throws IOException, URISyntaxException {
        PoeNinjaLoader ninjaLoader = new PoeNinjaLoader(this, true);
        ninjaLoader.launchLoad();
    }

    @FXML
    public void onRefreshAllButtonClick() throws IOException, URISyntaxException {

        ItemType itemTypePrev = itemType;
        TableView<Line> tableViewPrev = tableView;
        List<Line> itemsStorePrev = itemsStore;
        AtomicInteger lastSizePrev = lastSize;

        itemType = ItemType.ESSENCE;
        tableView = tableViewEssences;
        itemsStore = itemsStoreEssences;
        lastSize = lastSizeEssences;
        refreshInactiveTab();

        itemType = ItemType.FOSSIL;
        tableView = tableViewFossils;
        itemsStore = itemsStoreFossils;
        lastSize = lastSizeFossils;
        refreshInactiveTab();

        itemType = ItemType.FRAGMENT;
        tableView = tableViewFragments;
        itemsStore = itemsStoreFragments;
        lastSize = lastSizeFragments;
        refreshInactiveTab();

        itemType = ItemType.CURRENCY;
        tableView = tableViewCurrency;
        itemsStore = itemsStoreCurrency;
        lastSize = lastSizeCurrency;
        refreshInactiveTab();

        itemType = itemTypePrev;
        tableView = tableViewPrev;
        itemsStore = itemsStorePrev;
        lastSize = lastSizePrev;

    }

    private void refreshInactiveTab() throws IOException, URISyntaxException {
        perTableActivities();
        PoeNinjaLoader ninjaLoader = new PoeNinjaLoader(this, false);
        ninjaLoader.launchLoad();
    }

    @FXML
    public void onStopButtonClick() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        refreshButton.setDisable(false);
        refreshButtonExt.setDisable(false);
        constantUpdate.setDisable(false);
        fastUpdate.setDisable(false);
    }

    @FXML
    public void onSaveButtonClick() throws IOException {
        saveData();
    }

    public void saveData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        LocalDateTime dateTime = LocalDateTime.now();
        String pathname = generateFileName(itemType.getName(), dateTime);
        File file = new File(pathname);
        mapper.writeValue(file, getTableContent());
        loadSnapshotChoices(itemType);
        timeUpdated.put(itemType, dateTime);
        snapshotName.put(itemType, file.getName());
        updateLastUpdated(dateTime);
    }

    private void updateLastUpdated(LocalDateTime now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Platform.runLater(() -> lastUpdated.setText("Lat updated: " + now.format(formatter)));
    }

    @FXML
    public void onLoadButtonClick() throws IOException {

        String fileName = snapshotChoiceBox.getValue();
        if (fileName == null || fileName.isEmpty()) {
            warnings.setText("please select snapshot from list");
            return;
        }
        loadSnapshot(fileName);
    }


    @FXML
    public void onStyleButtonClick() {

        if (!isStyleApplied) {
            tableView.getScene().getStylesheets().add(getClass().getResource("/ua/dscorp/poessence/styles.css").toExternalForm());
            isStyleApplied = true;
        }
        else {
            tableView.getScene().getStylesheets().remove(getClass().getResource("/ua/dscorp/poessence/styles.css").toExternalForm());
            isStyleApplied = false;
        }
    }

    private void loadSnapshot(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + fileName);
        try {
            List<Line> lines = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Line.class));
            lines.forEach(line -> line.setNinjaPriceMultiplier(100));
            itemsStore.clear();
            itemsStore.addAll(lines);
            ObservableList<Line> items = FXCollections.observableArrayList(lines);
            tableView.setItems(items);
            LocalDateTime dateTime = extractDateFromName(fileName);
            timeUpdated.put(itemType, dateTime);
            snapshotName.put(itemType, file.getName());
            updateLastUpdated(dateTime);
            snapshotChoiceBox.setValue(fileName);
        }
        catch (MismatchedInputException e) {
            warnings.setText("File is corrupted, Invalidate it: " + fileName);
        }
    }

    @FXML
    public void onInvalidateButtonClick() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Invalidate saves? It will move them to old folder", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            List<String> files = getTableContentFileNames(itemType);
            Files.createDirectories(Path.of(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + "old/"));
            for (String file : files) {
                Files.move(Path.of(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + file), Path.of(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + "old/" + file), StandardCopyOption.REPLACE_EXISTING);
            }
            loadSnapshotChoices(itemType);
        }
    }

    @FXML
    public void onInvalidateSelectedButtonClick() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Invalidate selected save? It will move it to old folder", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            String file = snapshotChoiceBox.getValue();
            Files.createDirectories(Path.of(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + "old/"));
            Files.move(Path.of(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + file), Path.of(APP_DATA_FOLDER, SNAPSHOTS_FOLDER + "old/" + file), StandardCopyOption.REPLACE_EXISTING);
            loadSnapshotChoices(itemType);
        }
    }

    @FXML
    public void onGraphsClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("graphs-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        if (isStyleApplied) {
            scene.getStylesheets().add(getClass().getResource("/ua/dscorp/poessence/styles.css").toExternalForm());
        }
        Stage stage = new Stage();
        stage.setTitle(TOOL_NAME + " - Graphs");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    @FXML
    public void onAboutClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("about-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        if (isStyleApplied) {
            scene.getStylesheets().add(getClass().getResource("/ua/dscorp/poessence/styles.css").toExternalForm());
        }
        Stage stage = new Stage();
        stage.setTitle(TOOL_NAME + " - About");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    private void loadSnapshotChoices(ItemType itemType) {
        List<String> files = getTableContentFileNames(itemType);
        Platform.runLater(() -> snapshotChoiceBox.setItems(FXCollections.observableArrayList(files)));
    }

    public List<Line> getTableContent() {
        ObservableList<Line> items = tableView.getItems();
        return new ArrayList<>(items);
    }

    public void setKeys(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPress);
    }
}