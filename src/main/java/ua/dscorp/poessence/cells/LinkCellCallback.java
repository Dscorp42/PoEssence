package ua.dscorp.poessence.cells;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.*;
import javafx.util.Callback;
import ua.dscorp.poessence.data.Line;
import ua.dscorp.poessence.loader.PoeTradeParser;
import ua.dscorp.poessence.util.HostServicesContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static ua.dscorp.poessence.loader.PoeTradeLoaderTask.getHttpURLConnection;

public class LinkCellCallback implements Callback<TableColumn<Line, Void>, TableCell<Line, Void>> {

    private HostServicesContainer hostServices;
    private ChoiceBox<String> leagueChoiceBox;
    private TextField minBulkAmount;

    public LinkCellCallback(HostServicesContainer hostServices, ChoiceBox<String> leagueChoiceBox, TextField minBulkAmount) {
        super();
        this.hostServices = hostServices;
        this.leagueChoiceBox = leagueChoiceBox;
        this.minBulkAmount = minBulkAmount;
    }

    @Override
    public TableCell<Line, Void> call(final TableColumn<Line, Void> param) {
        return new TableCell<>() {
            private final Button btn = new Button("->");

            {
                btn.setOnAction(event -> {
                    String tradeId = null;
                    String url = "https://www.pathofexile.com/api/trade/exchange/" + leagueChoiceBox.getValue();
                    Line line = getTableView().getItems().get(getIndex());
                    String jsonInputString =
                            "{\"query\":{\"status\":{\"option\":\"online\"},\"have\":[\"chaos\",\"divine\"],\"want\":[\""
                                    + line.getDetailsId()
                                    + "\"],\"stock\":{\"min\": " + minBulkAmount.getText() + ",\"max\":null}},\"sort\":{\"have\":\"asc\"},\"engine\":\"new\"}";
                    try {
                        HttpURLConnection connection = getHttpURLConnection(url, jsonInputString, "");
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String inputLine;
                            StringBuilder response = new StringBuilder();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            ObjectMapper objectMapper = new ObjectMapper();
                            PoeTradeParser.Result result = objectMapper.readValue(response.toString(), PoeTradeParser.Result.class);

                            tradeId = result.getId();
                        }
                    } catch (IOException e) {
                        System.out.println("Error in connection: " + e.getMessage());
                    }

                    String resUrl = "https://www.pathofexile.com/trade/exchange/" + leagueChoiceBox.getValue() + "/" + tradeId;

                    hostServices.getServices().showDocument(resUrl);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };
    }
}
