package ua.dscorp.poessence.windows;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import ua.dscorp.poessence.cells.IconTableCell;
import ua.dscorp.poessence.data.Line;
import ua.dscorp.poessence.data.SimpleLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static ua.dscorp.poessence.Application.APP_DATA_FOLDER;

public class FilterController {

    @FXML
    private TableView<SimpleLine> tableView;
    @FXML
    private Button saveButton;

    private String itemType;

    List<Line> input;
    Set<SimpleLine> inputSimple;

    List<Line> filtered;

    @FXML
    protected void initialize() {
        TableColumn<SimpleLine, String> iconColumn = new TableColumn<>("Icon");
        iconColumn.setPrefWidth(30);
        iconColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIcon()));
        iconColumn.setCellFactory(a -> new IconTableCell<>());
        TableColumn<SimpleLine, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(220);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SimpleLine, Boolean> isIncludedColumn = new TableColumn<>("Include");
        isIncludedColumn.setCellValueFactory(new PropertyValueFactory<>("isIncluded"));
        isIncludedColumn.setCellFactory(new Callback<TableColumn<SimpleLine, Boolean>, TableCell<SimpleLine, Boolean>>() {
            @Override
            public TableCell<SimpleLine, Boolean> call(TableColumn<SimpleLine, Boolean> param) {
                return new TableCell<SimpleLine, Boolean>() {

                    private final CheckBox checkBox = new CheckBox();

                    {
                        checkBox.setOnAction(event -> {
                            SimpleLine person = getTableView().getItems().get(getIndex());
                            if (person != null) {
                                person.setIsIncluded(checkBox.isSelected());
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            checkBox.setSelected(item);
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });
        isIncludedColumn.setEditable(true);

        // Set table to be editable
        tableView.setEditable(true);

        tableView.getColumns().add(iconColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(isIncludedColumn);
    }

    public void loadFile() throws IOException {
        Files.createDirectories(Paths.get(APP_DATA_FOLDER + "/filter/"));

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(APP_DATA_FOLDER, "./filter/" + itemType + ".json");
        if (!file.exists()) {
            return;
        }
        List<SimpleLine> lines = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, SimpleLine.class));
        long count = lines.stream().filter(inputSimple::contains).count();

        if ((count * 100.0) / lines.size() < 80) {
            lines.clear();
        }
        lines.removeIf(simpleLine -> simpleLine.getDetailsId() == null);
        lines.forEach(inputSimple::remove);
        inputSimple.addAll(lines);
        ObservableList<SimpleLine> inputSimpleObs = FXCollections.observableArrayList(inputSimple);
        tableView.setItems(inputSimpleObs);
    }

    @FXML
    protected void onSaveButtonClick() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(APP_DATA_FOLDER, "./filter/" + itemType + ".json");
        List<SimpleLine> tableContent = getTableContent();
        mapper.writeValue(file, tableContent);

        input.stream()
                .filter(item -> tableContent.stream().anyMatch(simple -> simple.getDetailsId().equals(item.getDetailsId()) && simple.getIsIncluded()))
                .forEach(filteredItem -> filtered.add(filteredItem));

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onBackButtonClick() {

        filtered = null;
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onSelectButtonClick() {
        tableView.getItems().forEach(simpleLine -> simpleLine.setIsIncluded(true));
        tableView.refresh();
    }

    @FXML
    protected void onUnselectButtonClick() {
        tableView.getItems().forEach(simpleLine -> simpleLine.setIsIncluded(false));
        tableView.refresh();
    }

    public List<SimpleLine> getTableContent() {
        ObservableList<SimpleLine> items = tableView.getItems();
        return new ArrayList<>(items);
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public void setItems(List<Line> items) {
        input = items;
        inputSimple = new HashSet<>();
        filtered = new ArrayList<>();
        input.forEach(item -> inputSimple.add(new SimpleLine(item.getDetailsId(), item.getName(), item.getIcon(), true)));
        ObservableList<SimpleLine> inputSimpleObs = FXCollections.observableArrayList(inputSimple);
        tableView.setItems(inputSimpleObs);
    }

    public List<Line> getFilteredList() {
        return filtered;
    }
}