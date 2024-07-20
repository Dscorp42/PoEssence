package ua.dscorp.poessence.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import ua.dscorp.poessence.data.BulkItem;
import ua.dscorp.poessence.data.Line;

import java.util.List;

public class BulkItemNameCell<T> extends TableCell<Line, T> {
    private final int index;
    private final boolean withBorder;
    private final TableColumn<Line, String> column;

    public BulkItemNameCell(int index, boolean withBorder, TableColumn<Line, String> column) {
        this.index = index;
        this.withBorder = withBorder;
        this.column = column;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setStyle("");
        } else {
            setText(item.toString());
            Line line = getTableView().getItems().get(getIndex());
            List<BulkItem> bulkItems = line.getBulkItems();

            if (bulkItems != null && bulkItems.size() > index) {
                BulkItem bulkItem = bulkItems.get(index);
                if (bulkItem.isYou()) {
                    if (withBorder) {
                        column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                        setStyle("-fx-background-color: CORNFLOWERBLUE;-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                    }
                    else
                        setStyle("-fx-background-color: CORNFLOWERBLUE;");
                }
                else if (bulkItem.isAfk()) {
                    if (withBorder) {
                        column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                        setStyle("-fx-background-color: GOLD;-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                    }
                    else
                        setStyle("-fx-background-color: GOLD;");
                } else {
                    if (withBorder) {
                        column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                        setStyle("-fx-background-color: LIGHTGREEN;-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                    }
                    else
                        setStyle("-fx-background-color: LIGHTGREEN;");
                }
            } else {
                if (withBorder) {
                    column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                    setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #AAAAAA;");
                }
                else
                    setStyle("");
            }
        }
    }
}