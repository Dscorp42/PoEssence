package ua.dscorp.poessence.windows;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import ua.dscorp.poessence.data.Line;
import ua.dscorp.poessence.util.ItemType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static ua.dscorp.poessence.util.UtilClass.SNAPSHOTS_FOLDER;
import static ua.dscorp.poessence.util.UtilClass.getTableContentFileNames;

public class GraphsController {

    @FXML
    private LineChart<String, Double> chart;
    @FXML
    private VBox forChecks;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ChoiceBox<String> itemChoiceBox;

    private Map<String, List<Pair<Double, String>>> divSingleValue = new HashMap<>();
    private Map<String, List<Pair<Double, String>>> divBulkValue = new HashMap<>();
    private Set<Pair<Float, String>> allDetails = new TreeSet<>((o1, o2) -> Float.compare(o2.getKey(), o1.getKey()));

    @FXML
    protected void initialize() {

        itemChoiceBox.setItems(FXCollections.observableArrayList("ESSENCE", "FOSSIL", "FRAGMENT", "CURRENCY"));
        itemChoiceBox.setValue("ESSENCE");

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        chart.getXAxis().setLabel("File names");
        chart.setTitle("Line Chart from Multiple Files");
        chart.setAnimated(false);
    }

    @FXML
    protected void onCreateGraphButtonClick() throws IOException {

        divSingleValue.clear();
        divBulkValue.clear();
        allDetails.clear();
        List<File> files = getTableContentFileNames(ItemType.valueOf(itemChoiceBox.getValue())).stream().map(s -> new File(SNAPSHOTS_FOLDER + s)).toList();
        List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();

        for (File file : files) {
            loadDataFromFile(file);
        }

        for (Pair<Float, String> detailId : allDetails) {
            XYChart.Series<String, Double> seriesSingle = new XYChart.Series<>();
            if (divSingleValue.containsKey(detailId.getValue())) {
                seriesSingle.setName(detailId.getValue() + " single");
                for (Pair<Double, String> priceToDate : divSingleValue.get(detailId.getValue())) {
                    seriesSingle.getData().add(new XYChart.Data<>(priceToDate.getValue(), priceToDate.getKey()));
                }
                seriesList.add(seriesSingle);
            }
            XYChart.Series<String, Double> seriesBulk = new XYChart.Series<>();
            if (divBulkValue.containsKey(detailId.getValue())) {
                seriesBulk.setName(detailId.getValue() + " bulk");
                for (Pair<Double, String> priceToDate : divBulkValue.get(detailId.getValue())) {
                    seriesBulk.getData().add(new XYChart.Data<>(priceToDate.getValue(), priceToDate.getKey()));
                }
                seriesList.add(seriesBulk);
            }
        }
        chart.getData().clear();
        forChecks.getChildren().clear();
        int count = 0;
        for (XYChart.Series<String, Double> series : seriesList) {
            chart.getData().add(series);
            CheckBox checkBox = new CheckBox(series.getName());

            checkBox.setOnAction(e -> {
                series.getNode().setVisible(checkBox.isSelected());
            });
            if (count < 10) {
                checkBox.setSelected(true);
            }
            else {
                checkBox.setSelected(false);
                series.getNode().setVisible(checkBox.isSelected());
            }
            count++;
            forChecks.getChildren().add(checkBox);
        }
        seriesList.forEach(this::addTooltipsToSeries);
    }

    private void addTooltipsToSeries(XYChart.Series<String, Double> series) {
        for (XYChart.Data<String, Double> data : series.getData()) {
            Tooltip tooltip = new Tooltip(data.getYValue() + " Div");
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().setStyle("-fx-background-color: #ff6347, white;");
        }
    }

    private void loadDataFromFile(File file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        List<Line> lines = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Line.class));

        for (Line line : lines) {
            if (allDetails.stream().noneMatch(floatStringPair -> floatStringPair.getValue().equals(line.getDetailsId()))) {
                allDetails.add(new Pair<>(line.getChaosValue(), line.getDetailsId()));
            }
            divSingleValue.computeIfAbsent(line.getDetailsId(), d -> new ArrayList<>())
                    .add(new Pair<>((double) line.getDivineValue(), getRefinedName(file)));
            if (line.getBulkItems() != null && !line.getBulkItems().isEmpty()) {
                divBulkValue.computeIfAbsent(line.getDetailsId(), d -> new ArrayList<>())
                        .add(new Pair<>(line.getBulkItems().getFirst().getExchangeAmount()/line.getBulkItems().getFirst().getItemAmount(), getRefinedName(file)));
            }
        }
    }

    private static String getRefinedName(File file) {
        String[] split = file.getName().substring(file.getName().indexOf("_") + 1).replace("_save.json", "").split("_");
        String date = split[0];
        String time = split[1];
        return date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6,8)+" " + time.substring(0,2)+":"+time.substring(2,4);
    }
}