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
                        column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                        setStyle("-fx-background-color: #144a9c;-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                    }
                    else
                        setStyle("-fx-background-color: #144a9c;");
                }
                else if (bulkItem.isAfk()) {
                    if (withBorder) {
                        column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                        setStyle("-fx-background-color: #C26915;-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                    }
                    else
                        setStyle("-fx-background-color: #C26915;");
                } else {
                    if (withBorder) {
                        column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                        setStyle("-fx-background-color: #00825a;-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                    }
                    else
                        setStyle("-fx-background-color: #00825a;");
                }
            } else {
                if (withBorder) {
                    column.setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                    setStyle("-fx-border-width: 0px 0px 0px 5px;-fx-border-color: #555555;");
                }
                else
                    setStyle("");
            }
        }
    }
}