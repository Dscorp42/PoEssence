package ua.dscorp.poessence;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ua.dscorp.poessence.loader.PoeNinjaLoader;
import ua.dscorp.poessence.util.PersistenceHandler;
import ua.dscorp.poessence.windows.MainWindowController;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Launch GUI, persistence of variables.
 */
public class Application extends javafx.application.Application {

    public static final String SETTINGS_FILE = "PoEssence_prefs.conf";
    public static final String TOOL_NAME = "PoEssence";

    private ScheduledExecutorService scheduler;

    public static boolean isStyleApplied = false;

    @Override
    public void start(Stage stage) throws IOException {

        Font.loadFont(getClass().getResourceAsStream("/ua/dscorp/poessence/fonts/Fontin-Italic.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/ua/dscorp/poessence/fonts/Fontin-Regular.ttf"), 12);

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-window-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        MainWindowController controller = fxmlLoader.getController();
        loadPresets();

        if (isStyleApplied) {
            scene.getStylesheets().add(getClass().getResource("/ua/dscorp/poessence/styles.css").toExternalForm());
        }

        controller.setKeys(scene);
        stage.setTitle(TOOL_NAME);
        stage.setScene(scene);
        stage.setMaximized(true);

        scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> {
            if (controller.hourlyUpdate.isSelected() && MainWindowController.task == null)
            {
                controller.constantUpdate.setSelected(false);
                try {
                    controller.onRefreshAllButtonClick();
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 60, 60 * 60, TimeUnit.SECONDS); // Every 1 hour
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        System.out.println("Shutting down, saving settings.");
        if (MainWindowController.task != null)
            MainWindowController.task.cancel();

        if (PoeNinjaLoader.executorService != null)
            PoeNinjaLoader.executorService.shutdown();

        if (scheduler != null) {
            scheduler.shutdown();
        }
        savePresets();
    }

    private void loadPresets() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return;
        }
        String prefs = mapper.readValue(file, mapper.getTypeFactory().constructType(String.class));
        Map<String, String> prefsMap = new HashMap<>();
        for (String pref : prefs.split(";")) {
            String[] split = pref.split("=");
            String value = split.length > 1 ? split[1] : "";
            prefsMap.put(split[0], value);
        }
        PersistenceHandler.leagueChoiceBoxPers.setValue(prefsMap.get("leagueChoiceBox"));
        PersistenceHandler.POESESSIDPers.setText(prefsMap.get("POESESSID"));
        PersistenceHandler.accountNamePers.setText(prefsMap.get("accountName"));
        PersistenceHandler.thresholdPers.setText(prefsMap.get("threshold"));
        PersistenceHandler.ninjaPriceMultiplierPers.setText(prefsMap.get("ninjaPriceMultiplier"));
        PersistenceHandler.minEssenceTierPers.setText(prefsMap.get("minEssenceTier"));
        PersistenceHandler.constantUpdatePers.setSelected(Boolean.parseBoolean(prefsMap.get("constantUpdate")));
        isStyleApplied = Boolean.parseBoolean(prefsMap.get("isStyleApplied"));
    }

    private void savePresets() throws IOException {
        String prefs = "leagueChoiceBox=" + PersistenceHandler.leagueChoiceBoxPers.getValue()
                + ";POESESSID=" + PersistenceHandler.POESESSIDPers.getText()
                + ";accountName=" + PersistenceHandler.accountNamePers.getText()
                + ";threshold=" + PersistenceHandler.thresholdPers.getText()
                + ";ninjaPriceMultiplier=" + PersistenceHandler.ninjaPriceMultiplierPers.getText()
                + ";minEssenceTier=" + PersistenceHandler.minEssenceTierPers.getText()
                + ";constantUpdate=" + PersistenceHandler.constantUpdatePers.isSelected()
                + ";isStyleApplied=" + isStyleApplied;
        File file = new File(SETTINGS_FILE);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, prefs);
    }

    public static void main(String[] args) {
        launch();
    }
}