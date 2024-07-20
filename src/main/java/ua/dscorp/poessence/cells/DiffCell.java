package ua.dscorp.poessence.cells;

import javafx.scene.control.TableCell;
import ua.dscorp.poessence.data.Line;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiffCell extends TableCell<Line, Double> {

    private int threshold;

    public DiffCell(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setStyle("");
        } else {
            if (item > 1000)
            {
                setText("?");
            }
            else {
                setText(BigDecimal.valueOf(item).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
            }

            if (item >= threshold) {
                setStyle("-fx-font-weight: bold");
            }
            else
                setStyle("");
            }
        }
    }