package ua.dscorp.poessence.windows;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AboutController {

    @FXML
    Label about;

    @FXML
    protected void initialize() {
       // about.setFitToWidth(true); // Make the ScrollPane fit the width of the Label
       // about.setPannable(true); // Allow the content to be panned (scrollable)
    }
}