package ua.dscorp.poessence.windows;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringJoiner;

import static ua.dscorp.poessence.Application.APP_DATA_FOLDER;

public class NoteController {

    @FXML
    private TextArea notepad;
    @FXML
    private Button saveButton;

    private String detailsId;

    @FXML
    protected void initialize() {
        saveButton.setDisable(true);
    }

    public void loadFile() throws IOException {
        StringJoiner result = new StringJoiner("\n");
        Files.createDirectories(Paths.get(APP_DATA_FOLDER + "/notes/"));
        File file = new File(APP_DATA_FOLDER, "notes/" + detailsId + ".txt");
        notepad.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(false);
        });
        if (!file.exists()) {
            return;
        }
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null && !line.equals("\n")) {
                result.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        notepad.setText(result.toString());
    }

    @FXML
    protected void onSaveButtonClick() {
        File file = new File(APP_DATA_FOLDER, "notes/" + detailsId + ".txt");
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(notepad.getText());
            saveButton.setDisable(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDetailsId(String detailsId) {
        this.detailsId = detailsId;
    }
}