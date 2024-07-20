package ua.dscorp.poessence.cells;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import javafx.util.Callback;
import ua.dscorp.poessence.Application;
import ua.dscorp.poessence.data.Line;
import ua.dscorp.poessence.windows.NoteController;

import java.io.IOException;

public class TableColumnTableCellCallback implements Callback<TableColumn<Line, Void>, TableCell<Line, Void>> {
    @Override
    public TableCell<Line, Void> call(final TableColumn<Line, Void> param) {
        return new TableCell<>() {
            private final Button btn = new Button("Note");

            {
                btn.setOnAction(event -> {
                    Line line = getTableView().getItems().get(getIndex());
                    FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("note-view.fxml"));
                    Scene scene = null;
                    try {
                        Parent load = fxmlLoader.load();
                        NoteController controller = fxmlLoader.<NoteController>getController();
                        controller.setDetailsId(line.getDetailsId());
                        controller.loadFile();
                        scene = new Scene(load, 500, 400);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Stage stage = new Stage();
                    stage.setTitle(line.getDetailsId() + " - Note");
                    stage.setScene(scene);
                    stage.setMaximized(false);
                    stage.show();
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
