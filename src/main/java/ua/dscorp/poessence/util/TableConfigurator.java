package ua.dscorp.poessence.util;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import ua.dscorp.poessence.cells.BulkItemNameCell;
import ua.dscorp.poessence.cells.DiffCell;
import ua.dscorp.poessence.cells.IconTableCell;
import ua.dscorp.poessence.cells.TableColumnTableCellCallback;
import ua.dscorp.poessence.data.BulkItem;
import ua.dscorp.poessence.data.Line;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static ua.dscorp.poessence.loader.PoeTradeParser.MAX_RESULTS;
import static ua.dscorp.poessence.util.UtilClass.parseOrElse;

public final class TableConfigurator {

    public static final int DEFAULT_THRESOLD = 75;

    public static void configureTable(TableView<Line> tableView, String threshold) {
        TableColumn<Line, String> iconColumn = new TableColumn<>("Icon");
        iconColumn.setPrefWidth(30);
        iconColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIcon()));
        iconColumn.setCellFactory(a -> new IconTableCell<>());

        // Create a column with a button in each cell
        TableColumn<Line, Void> actionColumn = new TableColumn<>("Note");
        actionColumn.setPrefWidth(60);
        Callback<TableColumn<Line, Void>, TableCell<Line, Void>> cellFactory = new TableColumnTableCellCallback();
        actionColumn.setCellFactory(cellFactory);

        TableColumn<Line, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(220);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Line, Double> chaosValueColumn = new TableColumn<>("Chaos Value");
        chaosValueColumn.setCellValueFactory(cellData -> {
            float divVal = cellData.getValue().getChaosModdedValue();
            if (divVal != 0) {
                double res = BigDecimal.valueOf(divVal).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();

                return new SimpleDoubleProperty(res).asObject();
            } else {
                return new SimpleDoubleProperty(0).asObject();
            }
        });

        TableColumn<Line, String> divineValueColumn = new TableColumn<>("Div Value");

        divineValueColumn.setCellValueFactory(cellData -> {
            float divVal = cellData.getValue().getDivineModdedValue();
            if (divVal != 0) {
                String res = BigDecimal.valueOf(divVal).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();

                return new SimpleStringProperty(res);
            } else {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<Line, String> divineBulkValueColumn = new TableColumn<>("Bulk Div");

        divineBulkValueColumn.setCellValueFactory(cellData -> {
            List<BulkItem> bulkItems = cellData.getValue().getBulkItems();
            if (bulkItems != null && !bulkItems.isEmpty()) {
                String res = BigDecimal.valueOf(bulkItems.getFirst().getExchangeAmount()
                        / bulkItems.getFirst().getItemAmount()).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();

                return new SimpleStringProperty(res);
            } else {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<Line, String> divineStackValueColumn = new TableColumn<>("Stack Div");

        divineStackValueColumn.setCellValueFactory(cellData -> {
            List<BulkItem> bulkItems = cellData.getValue().getBulkItems();
            if (bulkItems != null && !bulkItems.isEmpty()) {
                double res = bulkItems.getFirst().getExchangeAmount()
                        / bulkItems.getFirst().getItemAmount();
                String resStr = BigDecimal.valueOf(res * cellData.getValue().getStackSize()).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                return new SimpleStringProperty(resStr);
            } else {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<Line, Double> diffValueColumn = new TableColumn<>("Diff %");

        diffValueColumn.setCellValueFactory(cellData -> {
            List<BulkItem> bulkItems = cellData.getValue().getBulkItems();
            float divVal = cellData.getValue().getDivineModdedValue();
            if (bulkItems != null && !bulkItems.isEmpty()) {
                double res = ((bulkItems.getFirst().getExchangeAmount() / bulkItems.getFirst().getItemAmount()) / divVal) * 100 - 100;

                return new SimpleDoubleProperty(res).asObject();
            } else {
                return new SimpleDoubleProperty(0).asObject();
            }
        });
        diffValueColumn.setCellFactory(a -> new DiffCell(parseOrElse(threshold, DEFAULT_THRESOLD)));

        tableView.getColumns().add(iconColumn);
        tableView.getColumns().add(actionColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(chaosValueColumn);
        tableView.getColumns().add(divineValueColumn);
        tableView.getColumns().add(divineBulkValueColumn);
        tableView.getColumns().add(divineStackValueColumn);
        tableView.getColumns().add(diffValueColumn);

        // Add bulk item columns
        for (int i = 0; i < MAX_RESULTS; i++) {
            int index = i;
            TableColumn<Line, String> bulkItemNameColumn = new TableColumn<>("Account");
            bulkItemNameColumn.setCellValueFactory(cellData -> {
                List<BulkItem> bulkItems = cellData.getValue().getBulkItems();
                if (bulkItems != null && bulkItems.size() > index) {
                    return new SimpleStringProperty(bulkItems.get(index).getName());
                } else {
                    return new SimpleStringProperty("");
                }
            });
            bulkItemNameColumn.setCellFactory(a -> new BulkItemNameCell<>(index, true, bulkItemNameColumn));


            TableColumn<Line, String> bulkItemExchangeAmountColumn = new TableColumn<>("Div Price");
            bulkItemExchangeAmountColumn.setCellValueFactory(cellData -> {
                List<BulkItem> bulkItems = cellData.getValue().getBulkItems();
                if (bulkItems != null && bulkItems.size() > index) {
                    double exchangeAmount = bulkItems.get(index).getExchangeAmount();
                    String ratio = BigDecimal.valueOf(exchangeAmount).stripTrailingZeros().toPlainString() + "/" + bulkItems.get(index).getItemAmount();
                    if (exchangeAmount != 1) {
                        String newBottom = BigDecimal.valueOf(bulkItems.get(index).getItemAmount() / exchangeAmount)
                                .setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                        ratio = "1/" + newBottom + "(" + ratio + ")";
                    }
                    return new SimpleStringProperty(ratio);
                } else {
                    return new SimpleStringProperty("");
                }
            });
            bulkItemExchangeAmountColumn.setCellFactory(a -> new BulkItemNameCell<>(index, false, bulkItemExchangeAmountColumn));

            TableColumn<Line, String> bulkItemStockAmountColumn = new TableColumn<>("Stock");
            bulkItemStockAmountColumn.setPrefWidth(40);
            bulkItemStockAmountColumn.setCellValueFactory(cellData -> {
                List<BulkItem> bulkItems = cellData.getValue().getBulkItems();
                if (bulkItems != null && bulkItems.size() > index) {
                    return new SimpleStringProperty("" + bulkItems.get(index).getStockAmount());
                } else {
                    return new SimpleStringProperty("");
                }
            });
            bulkItemStockAmountColumn.setCellFactory(a -> new BulkItemNameCell<>(index, false, bulkItemStockAmountColumn));

            tableView.getColumns().addAll(bulkItemNameColumn, bulkItemExchangeAmountColumn, bulkItemStockAmountColumn);
        }

        TableColumn<Line, String> totalOffersColumn = new TableColumn<>("Offers");
        totalOffersColumn.setCellValueFactory(new PropertyValueFactory<>("offers"));
        tableView.getColumns().add(totalOffersColumn);
    }
}
