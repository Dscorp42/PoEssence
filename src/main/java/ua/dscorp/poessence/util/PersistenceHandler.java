package ua.dscorp.poessence.util;

import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import ua.dscorp.poessence.windows.MainWindowController;

public final class PersistenceHandler {

    // To sync between app and controller during persistence of settings.
    public static ChoiceBox<String> leagueChoiceBoxPers;
    public static ChoiceBox<String> snapshotChoiceBoxPers;
    public static TextField POESESSIDPers;
    public static TextField accountNamePers;
    public static TextField thresholdPers;
    public static TextField ninjaPriceMultiplierPers;
    public static TextField minEssenceTierPers;
    public static TextField minBulkAmountPers;
    public static CheckBox constantUpdatePers;
    public static CheckBox fastUpdatePers;

    public static void loadPersistenceSettings(MainWindowController mainWindow) {
        leagueChoiceBoxPers = mainWindow.leagueChoiceBox;
        snapshotChoiceBoxPers = mainWindow.snapshotChoiceBox;
        POESESSIDPers = mainWindow.POESESSID;
        accountNamePers = mainWindow.accountName;
        thresholdPers = mainWindow.threshold;
        ninjaPriceMultiplierPers = mainWindow.ninjaPriceMultiplier;
        minEssenceTierPers = mainWindow.minEssenceTier;
        minBulkAmountPers = mainWindow.minBulkAmount;
        constantUpdatePers = mainWindow.constantUpdate;
        fastUpdatePers = mainWindow.fastUpdate;

        setDefaultValues(mainWindow);
    }

    private static void setDefaultValues(MainWindowController mainWindow) {
        mainWindow.leagueChoiceBox.setItems(FXCollections.observableArrayList("Standard", "Settlers"));
        mainWindow.leagueChoiceBox.setValue("Settlers");
        mainWindow.minEssenceTier.setText("6");
        mainWindow.minBulkAmount.setText("10");
        mainWindow.ninjaPriceMultiplier.setText("100");
        mainWindow.threshold.setText("75");
    }
}
