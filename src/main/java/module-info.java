module ua.dscorp.poessence {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.fasterxml.jackson.databind;

    opens ua.dscorp.poessence to javafx.fxml;
    exports ua.dscorp.poessence;
    exports ua.dscorp.poessence.cells;
    opens ua.dscorp.poessence.cells to javafx.fxml;
    exports ua.dscorp.poessence.data;
    opens ua.dscorp.poessence.data to javafx.fxml;
    exports ua.dscorp.poessence.data.containers;
    opens ua.dscorp.poessence.data.containers to javafx.fxml;
    exports ua.dscorp.poessence.util;
    opens ua.dscorp.poessence.util to javafx.fxml;
    exports ua.dscorp.poessence.loader;
    opens ua.dscorp.poessence.loader to javafx.fxml;
    exports ua.dscorp.poessence.windows;
    opens ua.dscorp.poessence.windows to javafx.fxml;
}