package ua.dscorp.poessence;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import ua.dscorp.poessence.loader.PoeNinjaLoader;
import ua.dscorp.poessence.util.PersistenceHandler;
import ua.dscorp.poessence.windows.MainWindowController;

import java.io.*;
import java.net.*;
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
    public static final String VERSION_FILE = "PoEssence_version.conf";
    public static final String TOOL_NAME = "PoEssence";
    private static final String DOWNLOAD_URL = "https://github.com/Dscorp42/PoEssence/releases/latest/download/PoEssence.jar";
    private static final String TAGS_URL = "https://api.github.com/repos/Dscorp42/PoEssence/tags";

    private ScheduledExecutorService scheduler;

    public static boolean isStyleApplied = false;

    @Override
    public void start(Stage stage) throws IOException {

        checkForUpdates();

        Font.loadFont(getClass().getResourceAsStream("/ua/dscorp/poessence/fonts/Fontin-Italic.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/ua/dscorp/poessence/fonts/Fontin-Regular.ttf"), 12);

        InputStream icon1 = Application.class.getResourceAsStream("/ua/dscorp/poessence/icons/Regal_Orb_16.png");
        InputStream icon2 = Application.class.getResourceAsStream("/ua/dscorp/poessence/icons/Regal_Orb_32.png");
        InputStream icon3 = Application.class.getResourceAsStream("/ua/dscorp/poessence/icons/Regal_Orb_64.png");
        InputStream icon4 = Application.class.getResourceAsStream("/ua/dscorp/poessence/icons/Regal_Orb_128.png");
        stage.getIcons().addAll(new Image(icon1), new Image(icon2), new Image(icon3), new Image(icon4));

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

    private void checkForUpdates() {
        File file = new File(VERSION_FILE);
        Integer lastVersion;
        try {
            lastVersion = getLastVersion();
        }
        catch (IOException e) {
            System.out.println("can't get last version " + e.getMessage());
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        if (file.exists()) {
            Integer currentVersion;
            try {
                currentVersion = mapper.readValue(file, mapper.getTypeFactory().constructType(Integer.class));
            }
            catch (IOException e) {
                System.out.println("can't get current version " + e.getMessage());
                return;
            }
            if (currentVersion < lastVersion) {
                update(mapper, file, lastVersion);
            }
        }
        else {
            update(mapper, file, lastVersion);
        }
    }

    private static void update(ObjectMapper mapper, File file, Integer lastVersion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Launch download and update? To finish update app will close.", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            try {
            mapper.writeValue(file, lastVersion);
            downloadAndReplace();
            }
            catch (IOException e) {
                System.out.println("can't download and update " + e.getMessage());
                return;
            }
            Platform.exit();
            try {
                Thread.sleep(2000); // Wait for 2 seconds
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            System.exit(0);
        }
    }

    private int getLastVersion() throws IOException {

        HttpURLConnection connection = (HttpURLConnection) new URL(TAGS_URL).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONArray tags = new JSONArray(response.toString());
        if (!tags.isEmpty()) {
            // Assuming the first tag is the most recent one
            JSONObject latestTag = tags.getJSONObject(0);
            return Integer.parseInt(latestTag.getString("name").replace("v", "").replace(".", ""));
        }

        return 0;
    }

    private static void downloadAndReplace() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(DOWNLOAD_URL).openConnection();
        connection.setRequestMethod("GET");

        File tempFile = new File("PoEssence.jar");
        try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        launchUpdater();
    }

    private static void launchUpdater() throws IOException {

            String batchFilePath = "/ua/dscorp/poessence/update.bat";
            extractResource(batchFilePath, "update.bat");

            Runtime.getRuntime().exec("cmd /c start update.bat");
    }

    private static void extractResource(String resourcePath, String fileName) throws IOException {
        // Create a temporary file
        File tempFile = new File(fileName);

        try (InputStream inputStream = Application.class.getResourceAsStream(resourcePath);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
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
        shutdownAll();
    }

    private void shutdownAll() throws IOException {
        String batchFilePath = "/ua/dscorp/poessence/shutdown.bat";
        extractResource(batchFilePath, "shutdown.bat");

        Runtime.getRuntime().exec("cmd /c start shutdown.bat");
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