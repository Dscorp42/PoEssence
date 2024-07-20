package ua.dscorp.poessence.cells;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ua.dscorp.poessence.data.Line;

public class IconTableCell<T> extends TableCell<T, String> {

    private final ImageView imageView;

    public IconTableCell() {
        imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            Image image = new Image(item, true);
            imageView.setImage(image);
            setGraphic(imageView);
        }
    }
}